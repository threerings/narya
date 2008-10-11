//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

        // start up our message writer
        _writer = new Timer(1);
        _writer.addEventListener(TimerEvent.TIMER, sendPendingMessages);
        _writer.start();

        _portIdx = 0;
        logonToPort();
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
    }

    /**
     * This method is strangely named, and it does two things which is
     * bad style. Either log on to the next port, or save that the port
     * we just logged on to was a good one.
     */
    protected function logonToPort (logonWasSuccessful :Boolean = false) :Boolean
    {
        var ports :Array = _client.getPorts();

        if (!logonWasSuccessful) {
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
            _socket.addEventListener(Event.CLOSE, socketClosed);

            _frameReader = new FrameReader(_socket);
            _frameReader.addEventListener(FrameAvailableEvent.FRAME_AVAILABLE, inputFrameReceived);
        }

        var host :String = _client.getHostname();
        var pportKey :String = host + ".preferred_port";
        var pport :int = ports[0];
        var ppidx :int = Math.max(0, ports.indexOf(pport));
        var port :int = (ports[(_portIdx + ppidx) % ports.length] as int);

        if (logonWasSuccessful) {
            _portIdx = -1; // indicate that we're no longer trying new ports

        } else {
            log.info("Connecting [host=" + host + ", port=" + port + "].");
            _socket.connect(host, port);
        }

        return true;
    }

    protected function removeListeners () :void
    {
        _socket.removeEventListener(Event.CONNECT, socketOpened);
        _socket.removeEventListener(IOErrorEvent.IO_ERROR, socketError);
        _socket.removeEventListener(Event.CLOSE, socketClosed);

        _frameReader.removeEventListener(FrameAvailableEvent.FRAME_AVAILABLE, inputFrameReceived);
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

        _client.notifyObservers(ClientEvent.CLIENT_DID_LOGOFF, null);
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
                if (_tqsize != _outq.length) {
                    // only log when our outq size changes
                    _tqsize = _outq.length;
                    log.info("Throttling outgoing messages", "queue", _outq.length,
                             "throttle", _client.getOutgoingMessageThrottle());
                }
                return;
            }
            _tqsize = 0;

            // grab the next message from the queue and send it
            var msg :UpstreamMessage = (_outq.shift() as UpstreamMessage);
            sendMessage(msg);

            // if this was a logoff request, shutdown
            if (msg is LogoffRequest) {
                shutdown(null);
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
        logonToPort(true);
        // well that's great! let's logon
        postMessage(new AuthRequest(_client.getCredentials(), _client.getVersion(),
                                    _client.getBootGroups()));
    }

    /**
     * Called when there is an io error with the socket.
     */
    protected function socketError (event :IOErrorEvent) :void
    {
        // if we're trying ports, try the next one.
        if (_portIdx != -1) {
            _portIdx++;
            if (logonToPort()) {
                return;
            }
        }

        // total failure
        log.warning("Socket error: " + event, "target", event.target);
        Log.dumpStack();
        shutdown(new Error("Socket closed unexpectedly."));
    }

    /**
     * Called when the connection to the server was closed.
     */
    protected function socketClosed (event :Event) :void
    {
        log.info("Socket was closed: " + event);
        _client.notifyObservers(ClientEvent.CLIENT_CONNECTION_FAILED);
        shutdown(null);
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
    protected var _tqsize :int = 0;

    protected const log :Log = Log.getLog(this);

    /** The current port we'll try to connect to. */
    protected var _portIdx :int = -1;
}
}
