package com.threerings.io {

import flash.events.EventDispatcher;
import flash.events.ProgressEvent;

import flash.net.Socket;

import flash.util.ByteArray;
import flash.util.Endian;

/**
 * Reads socket data until a complete frame is available.
 * This dispatches a FrameAvailableEvent.FRAME_AVAILABLE once a frame
 * has been fully read off the socket and is ready for decoding.
 */
public class FrameReader extends EventDispatcher
{
    public function FrameReader (socket :Socket)
    {
        _socket = socket;
        _socket.addEventListener(ProgressEvent.SOCKET_DATA, socketHasData);
    }

    /**
     * Called when our socket has data that we can read.
     */
    protected function socketHasData (event :ProgressEvent) :void
    {
        if (_curData == null) {
            if (_socket.bytesAvailable < HEADER_SIZE) {
                // if there are less bytes available than a header, let's
                // just leave them on the socket until we can read the length
                // all at once
                return;
            }
            _length = _socket.readInt();
            _curData = new ByteArray();
            _curData.endian = Endian.BIG_ENDIAN;
        }

        // read bytes: either as much as possible or up to the end of the frame
        var toRead :int = Math.min(_length - _curData.length,
            _socket.bytesAvailable);
        _socket.readBytes(_curData, _curData.length, toRead);

        if (_length === _curData.length) {
            // we have now read a complete frame, let us dispatch the data
            _curData.position = 0; // move the read pointer to the beginning
            dispatchEvent(new FrameAvailableEvent(_curData));
            _curData = null; // clear, so we know we need to first read length

            // there's a good chance there's more on the socket, recurse
            // now to read it
            socketHasData(event);
        }
    }

    protected var _socket :Socket;
    protected var _curData :ByteArray;
    protected var _length :int;

    /** The number of bytes in the frame header (a 32-bit integer). */
    protected const HEADER_SIZE :int = 4;
}
}
