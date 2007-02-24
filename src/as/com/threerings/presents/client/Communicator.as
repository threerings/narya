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

import flash.net.Socket;

import flash.utils.ByteArray;
import flash.utils.Endian;

import com.threerings.util.StringUtil;

import com.threerings.io.FrameAvailableEvent;
import com.threerings.io.FrameReader;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Translations;

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

        _portIdx = 0;
        logonToPort();
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
                _client.reportLogonTribulations(
                    new LogonError(AuthCodes.TRYING_NEXT_PORT, true));

                removeListeners();
            }

            // create the socket and set up listeners
            _socket = new Socket();
            _socket.endian = Endian.BIG_ENDIAN;
            _socket.addEventListener(Event.CONNECT, socketOpened);
            _socket.addEventListener(IOErrorEvent.IO_ERROR, socketError);
            _socket.addEventListener(Event.CLOSE, socketClosed);

            _frameReader = new FrameReader(_socket);
            _frameReader.addEventListener(FrameAvailableEvent.FRAME_AVAILABLE,
                inputFrameReceived);
        }

        var host :String = _client.getHostname();
        var pportKey :String = host + ".preferred_port";
        var pport :int =
            (PresentsPrefs.config.getValue(pportKey, ports[0]) as int);
        var ppidx :int = Math.max(0, ports.indexOf(pport));
        var port :int = (ports[(_portIdx + ppidx) % ports.length] as int);

        if (logonWasSuccessful) {
            _portIdx = -1; // indicate that we're no longer trying new ports
            PresentsPrefs.config.setValue(pportKey, port);

        } else {
            Log.getLog(this).info(
                "Connecting [host=" + host + ", port=" + port + "].");
            _socket.connect(host, port);
        }

        return true;
    }

    protected function removeListeners () :void
    {
        _socket.removeEventListener(Event.CONNECT, socketOpened);
        _socket.removeEventListener(IOErrorEvent.IO_ERROR, socketError);
        _socket.removeEventListener(Event.CLOSE, socketClosed);

        _frameReader.removeEventListener(FrameAvailableEvent.FRAME_AVAILABLE,
            inputFrameReceived);
    }

    public function logoff () :void
    {
        if (_socket == null) {
            return;
        }

        sendMessage(new LogoffRequest());

        shutdown(null);
    }

    public function postMessage (msg :UpstreamMessage) :void
    {
        sendMessage(msg); // send it now: we have no out queue
    }

    protected function shutdown (logonError :Error) :void
    {
        _client.notifyObservers(ClientEvent.CLIENT_DID_LOGOFF, null);

        if (_socket != null)  {
            try {
                _socket.close();
            } catch (err :Error) {
                Log.getLog(this).warning(
                    "Error closing failed socket [error=" + err + "].");
            }
            removeListeners();
            _socket = null;
            _outStream = null;
            _inStream = null;
            _frameReader = null;
            _outBuffer = null;
        }

        _client.cleanup(logonError);
    }

    protected function sendMessage (msg :UpstreamMessage) :void
    {
        if (_outStream == null) {
            Log.getLog(this).warning(
                "No socket, dropping msg [msg=" + msg + "].");
            return;
        }

        // write the message (ends up in _outBuffer)
        _outStream.writeObject(msg);

//        Log.debug("outBuffer: " + StringUtil.unhexlate(_outBuffer));

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

    /**
     * Called when a frame of data from the server is ready to be
     * decoded into a DownstreamMessage.
     */
    protected function inputFrameReceived (event :FrameAvailableEvent) :void
    {
        // convert the frame data into a message from the server
        var frameData :ByteArray = event.getFrameData();
        //Log.debug("length of in frame: " + frameData.length);
        //Log.debug("inBuffer: " + StringUtil.unhexlate(frameData));
        _inStream.setSource(frameData);
        var msg :DownstreamMessage;
        try {
            msg = (_inStream.readObject() as DownstreamMessage);
        } catch (e :Error) {
            var log :Log = Log.getLog(this);
            log.warning("Error processing downstream message: " + e);
            log.logStackTrace(e);
            return;
        }

        if (frameData.bytesAvailable > 0) {
            Log.getLog(this).warning(
                "Beans! We didn't fully read a frame, surely there's " +
                "a bug in some streaming code. " +
                "[bytesLeftOver=" + frameData.bytesAvailable + "].");
        }

        if (_omgr != null) {
            // if we're logged on, then just do the normal thing
            _omgr.processMessage(msg);
            return;
        }

        // Otherwise, this would be the AuthResponse to our logon attempt.
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
        var req :AuthRequest = new AuthRequest(
            _client.getCredentials(), _client.getVersion(), _client.getBootGroups());
        sendMessage(req);
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
        Log.getLog(this).warning("socket error: " + event);
        shutdown(new Error("socket closed unexpectedly."));
    }

    /**
     * Called when the connection to the server was closed.
     */
    protected function socketClosed (event :Event) :void
    {
        Log.getLog(this).warning("socket was closed: " + event);
        _client.notifyObservers(ClientEvent.CLIENT_CONNECTION_FAILED);
        logoff();
    }

    protected var _client :Client;
    protected var _omgr :ClientDObjectMgr;

    protected var _outBuffer :ByteArray;
    protected var _outStream :ObjectOutputStream;

    protected var _inStream :ObjectInputStream;
    protected var _frameReader :FrameReader;

    protected var _socket :Socket;

    protected var _lastWrite :uint;

    /** The current port we'll try to connect to. */
    protected var _portIdx :int = -1;
}
}
