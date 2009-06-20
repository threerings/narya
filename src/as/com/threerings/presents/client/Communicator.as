//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.client {

import flash.errors.IOError;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;
import flash.events.TimerEvent;

import flash.net.Socket;
import flash.utils.ByteArray;
import flash.utils.Endian;
import flash.utils.Timer;

import com.threerings.util.Log;

import com.threerings.io.FrameAvailableEvent;
import com.threerings.io.FrameReader;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.data.AuthCodes;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.LogoffRequest;
import com.threerings.presents.net.UpstreamMessage;
import com.threerings.presents.net.ThrottleUpdatedMessage;

public class Communicator
{
    public function Communicator (client :Client)
    {
        _client = client;
    }

    public function logon () :void
    {
        // create our input/output business
        _outBuffer = new ByteArray();
        _outBuffer.endian = Endian.BIG_ENDIAN;
        _outStream = new ObjectOutputStream(_outBuffer);
        _inStream = new ObjectInputStream();

        attemptLogon(0);
    }

    public function logoff () :void
    {
        if (_socket == null) {
            return;
        }
        postMessage(new LogoffRequest());
    }

    public function postMessage (msg :UpstreamMessage) :void
    {
        _outq.push(msg);
        if (_writer != null) {
            sendPendingMessages(null);

        } else {
            log.warning("Posting message prior to opening socket", "msg", msg);
        }
    }

    /**
     * Detects if the communicator currently has an open channel to the server. This is guaranteed
     * to return false while the logoff event is being dispatched. It also returns true during
     * authentication, but prior to the client appearing to be logged on.
     */
    public function isConnected () :Boolean
    {
        return _writer != null;
    }

    /**
     * Attempts to logon on using the port at the specified index.
     */
    protected function attemptLogon (portIdx :int) :Boolean
    {
        var ports :Array = _client.getPorts();
        _portIdx = portIdx; // note the port we're about to try
        if (_portIdx >= ports.length) {
            return false;
        }
        if (_portIdx != 0) {
            _client.reportLogonTribulations(new LogonError(AuthCodes.TRYING_NEXT_PORT, true));
            removeListeners();
        }

        // create the socket and set up listeners
        _socket = new Socket();
        _socket.endian = Endian.BIG_ENDIAN;
        _socket.addEventListener(Event.CONNECT, socketOpened);
        _socket.addEventListener(IOErrorEvent.IO_ERROR, socketError);
        _socket.addEventListener(SecurityErrorEvent.SECURITY_ERROR, socketError);
        _socket.addEventListener(Event.CLOSE, socketClosed);

        _frameReader = new FrameReader(_socket);
        _frameReader.addEventListener(FrameAvailableEvent.FRAME_AVAILABLE, inputFrameReceived);

        var host :String = _client.getHostname();
        var pport :int = ports[0];
        var ppidx :int = Math.max(0, ports.indexOf(pport));
        var port :int = (ports[(_portIdx + ppidx) % ports.length] as int);

        log.info("Connecting", "host", host, "port", port, "svcs", _client.getBootGroups());
        _socket.connect(host, port);

        return true;
    }

    protected function removeListeners () :void
    {
        _socket.removeEventListener(Event.CONNECT, socketOpened);
        _socket.removeEventListener(IOErrorEvent.IO_ERROR, socketError);
        _socket.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, socketError);
        _socket.removeEventListener(Event.CLOSE, socketClosed);

        _frameReader.removeEventListener(FrameAvailableEvent.FRAME_AVAILABLE, inputFrameReceived);
        _frameReader.shutdown();
    }

    protected function shutdown (logonError :Error) :void
    {
        if (_socket != null)  {
            if (_socket.connected) {
                try {
                    _socket.close();
                } catch (err :Error) {
                    log.warning("Error closing failed socket [error=" + err + "].");
                }
            }
            removeListeners();
            _socket = null;
            _outStream = null;
            _inStream = null;
            _frameReader = null;
            _outBuffer = null;
        }

        if (_writer != null) {
            _writer.stop();
            _writer = null;
        }

        // if we never got our client object, we never dispatched a DID_LOGON and therefore we
        // don't want to dispatch a DID_LOGOFF, the observers basically never know anything ever
        // happened
        if (_client.getClientObject() != null) {
            _client.notifyObservers(ClientEvent.CLIENT_DID_LOGOFF, null);
        }
        _client.cleanup(logonError);
    }

    /**
     * Sends all pending messages from our outgoing message queue. If we hit our throttle while
     * sending, we stop and wait for the next time around when we'll try sending them again.
     */
    protected function sendPendingMessages (event :TimerEvent) :void
    {
        while (_outq.length > 0) {
            // if we've exceeded our throttle, stop for now
            if (_client.getOutgoingMessageThrottle().throttleOp()) {
                if (!_notedThrottle) {
                    log.info("Throttling outgoing messages", "queue", _outq.length,
                             "throttle", _client.getOutgoingMessageThrottle());
                    _notedThrottle = true;
                }
                return;
            }
            _notedThrottle = false;

            // grab the next message from the queue and send it
            var msg :UpstreamMessage = (_outq.shift() as UpstreamMessage);
            sendMessage(msg);

            // if this was a logoff request, shutdown
            if (msg is LogoffRequest) {
                shutdown(null);
            } else if (msg is ThrottleUpdatedMessage) {
                _client.finalizeOutgoingMessageThrottle(ThrottleUpdatedMessage(msg).messagesPerSec);
            }
        }
    }

    /**
     * Writes a single message to our outgoing socket.
     */
    protected function sendMessage (msg :UpstreamMessage) :void
    {
        if (_outStream == null) {
            log.warning("No socket, dropping msg [msg=" + msg + "].");
            return;
        }

        // write the message (ends up in _outBuffer)
        _outStream.writeObject(msg);

//         Log.debug("outBuffer: " + StringUtil.unhexlate(_outBuffer));

        // Frame it by writing the length, then the bytes.
        // We add 4 to the length, because the length is of the entire frame
        // including the 4 bytes used to encode the length!
        _socket.writeInt(_outBuffer.length + 4);
        _socket.writeBytes(_outBuffer);
        _socket.flush();

        // clean up the output buffer
        _outBuffer.length = 0;
        _outBuffer.position = 0;

        // make a note of our most recent write time
        updateWriteStamp();
    }

    /**
     * Called when a frame of data from the server is ready to be decoded into a DownstreamMessage.
     */
    protected function inputFrameReceived (event :FrameAvailableEvent) :void
    {
        // convert the frame data into a message from the server
        var frameData :ByteArray = event.getFrameData();
        _inStream.setSource(frameData);
        var msg :DownstreamMessage;
        try {
            msg = (_inStream.readObject(DownstreamMessage) as DownstreamMessage);
        } catch (e :Error) {
            log.warning("Error processing downstream message: " + e);
            log.logStackTrace(e);
            return;
        }

        if (frameData.bytesAvailable > 0) {
            log.warning("Beans! We didn't fully read a frame, is there a bug in some streaming " +
                "code? [bytesLeftOver=" + frameData.bytesAvailable + ", msg=" + msg + "].");
        }

        if (_omgr != null) {
            // if we're logged on, then just do the normal thing
            _omgr.processMessage(msg);
            return;
        }

        // otherwise, this would be the AuthResponse to our logon attempt
        var rsp :AuthResponse = (msg as AuthResponse);
        var data :AuthResponseData = rsp.getData();
        if (data.code !== AuthResponseData.SUCCESS) {
            shutdown(new Error(data.code));
            return;
        }

        // logon success
        _omgr = new ClientDObjectMgr(this, _client);
        _client.setAuthResponseData(data);
    }

    /**
     * Called when the connection to the server was successfully opened.
     */
    protected function socketOpened (event :Event) :void
    {
        // reset our port index now that we're successfully logged on; this way if the socket
        // fails, we won't think that we're in the middle of trying to logon
        _portIdx = -1;

        // check for a logoff message
        for each (var message :UpstreamMessage in _outq) {
            if (message is LogoffRequest) {
                // don't bother authing, just bail
                log.info("Logged off prior to socket opening, shutting down");
                shutdown(null);
                return;
            }
        }

        // send our authentication request (do so directly rather than putting it on the outgoing
        // write queue)
        sendMessage(new AuthRequest(_client.getCredentials(), _client.getVersion(),
                                    _client.getBootGroups()));

        // kick off our writer thread now that we know we're ready to write
        _writer = new Timer(1);
        _writer.addEventListener(TimerEvent.TIMER, sendPendingMessages);
        _writer.start();

        // clear the queue, the server doesn't like anything sent prior to auth
        _outq.length = 0;
    }

    /**
     * Called when there is an io error with the socket.
     */
    protected function socketError (event :Event) :void
    {
        // if we're still trying ports, try the next one.
        if (_portIdx != -1) {
            if (attemptLogon(_portIdx+1)) {
                return;
            }
        }

        // total failure
        log.warning("Socket error: " + event, "target", event.target);
        Log.dumpStack();
        shutdown(new Error(AuthCodes.NETWORK_ERROR));
    }

    /**
     * Called when the connection to the server was closed.
     */
    protected function socketClosed (event :Event) :void
    {
        log.info("Socket was closed: " + event);
        _client.notifyObservers(ClientEvent.CLIENT_CONNECTION_FAILED);
        // if we hadn't loaded our client object yet, behave as if this was a logon failure because
        // we failed before we dispatched a DID_LOGON
        var wasLoggedOn :Boolean = (_client.getClientObject() != null);
        shutdown(wasLoggedOn ? null : new LogonError(AuthCodes.NETWORK_ERROR));
    }

    /**
     * Returns the time at which we last sent a packet to the server.
     */
    internal function getLastWrite () :uint
    {
        return _lastWrite;
    }

    /**
     * Makes a note of the time at which we last communicated with the server.
     */
    internal function updateWriteStamp () :void
    {
        _lastWrite = flash.utils.getTimer();
    }

    protected var _client :Client;
    protected var _omgr :ClientDObjectMgr;

    protected var _outBuffer :ByteArray;
    protected var _outStream :ObjectOutputStream;

    protected var _inStream :ObjectInputStream;
    protected var _frameReader :FrameReader;

    protected var _socket :Socket;
    protected var _lastWrite :uint;

    protected var _outq :Array = new Array();
    protected var _writer :Timer;
    protected var _notedThrottle :Boolean = false;

    protected const log :Log = Log.getLog(this);

    /** The current port we'll try to connect to. */
    protected var _portIdx :int = -1;
}
}
