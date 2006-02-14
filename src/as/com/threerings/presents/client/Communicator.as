package com.threerings.presents.client {

import flash.net.Socket;

import flash.util.ByteArray;
import flash.util.Endian;

import com.threerings.io.FrameAvailableEvent;
import com.threerings.io.FrameReader;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class Communicator
{
    public function Communicator (client :Client)
    {
        _client = client;
    }

    public function logon () :void
    {
        // create the socket and set up listeners
        _socket = new Socket();
        _socket.addEventListener(Event.CONNECT, socketOpened);
        _socket.addEventListener(IOErrorEvent.IO_ERROR, socketError);
        _socket.addEventListener(Event.CLOSE, socketClosed);

        // create our input/output business
        _outBuffer = new ByteArray();
        _outBuffer.endian = Endian.BIG_ENDIAN;
        _outStream = new ObjectOutputStream(_outBuffer);

        _frameReader = new FrameReader(_socket);
        _frameReader.addEventListener(FrameAvailableEvent.FRAME_AVAILABLE,
            inputFrameReceived);
        _inStream = new ObjectInputStream();

        _socket.connect(_client.getHostname(), _client.getPort());
    }

    public function logoff () :void
    {
        if (_socket == null) {
            return;
        }

        sendMessage(new LogoffRequest());

        shutdown(null);
    }

    protected function shutdown (logonError :Error) :void
    {
        if (_socket != null)  {
            try {
                _socket.close();
            } catch (err :Error) {
                trace("Error closing failed socket: " + err);
            }
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
        // write the message (ends up in _outBuffer)
        _outStream.writeObject(msg);

        // frame it by writing the length, then the bytes
        _socket.writeInt(_outBuffer.length);
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
    protected function getLastWrite () :Number
    {
        return _lastWrite;
    }

    /**
     * Makes a note of the time at which we last communicated with the server.
     */
    protected function updateWriteStamp () :void
    {
        _lastWrite = new Date().getTime();
    }

    /**
     * Called when a frame of data from the server is ready to be
     * decoded into a DownstreamMessage.
     */
    protected function inputFrameReceived (event :FrameAvailableEvent) :void
    {
        // convert the frame data into a message from the server
        _inStream.setSource(event.getFrameData());
        var msg :DownstreamMessage = _inStream.readObject();

        if (_omgr != null) {
            // if we're logged on, then just do the normal thing
            _omgr.processMessage(msg);
            return;
        }

        // Otherwise, this would be the AuthResponse to our logon attempt.
        var rsp :AuthResponse = (msg as AuthResponse); // TODO: as correct?
        var data :AuthResponseData = rsp.getData();
        if (data.code !== AuthResponseData.SUCCESS) {
            shutdown(new Error(data.code));
            return;
        }

        // logon success
        _omgr = new ClientDObjectMgr(this, _client);
        _client._authData = data;
    }

    /**
     * Called when the connection to the server was successfully opened.
     */
    protected function socketOpened (event :Event) :void
    {
        // well that's great! let's logon
        var req :AuthRequest = new AuthRequest(_client.getCredentials(),
            _client.getVersion());
        sendMessage(req);
    }

    /**
     * Called when there is an io error with the socket.
     */
    protected function socketError (event :IOErrorEvent) :void
    {
        trace("socketError: " + event);
        shutdown(new Error("socket closed unexpectedly."));
    }

    /**
     * Called when the connection to the server was closed.
     */
    protected function socketClosed (event :Event) :void
    {
        _client.notifyObserver(ClientEvent.CLIENT_CONNECTION_FAILED);
        shutdown(null);
    }

    protected var _client :Client;
    protected var _omgr :ClientDObjectManager;

    protected var _outBuffer :ByteArray;
    protected var _outStream :ObjectOutputStream;

    protected var _inStream :ObjectInputStream;

    protected var _socket :Socket;

    protected var _lastWrite :Number;
}
}
