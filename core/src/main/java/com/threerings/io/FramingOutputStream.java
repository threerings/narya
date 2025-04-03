//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.io;

import java.nio.ByteBuffer;

/**
 * The framing output stream accumulates output into a byte array just
 * like the byte array output stream, but can then be instructed to send
 * its contents over a channel, prefixed by the length (written as an
 * integer) of the entire frame (contents plus length prefix). It does
 * this efficiently so that data is copied as little as possible and so
 * that the output stream to which the data is written need not be
 * buffered because the framed output is written in a single call to
 * <code>write()</code>.
 *
 * <p><em>Note:</em> The framing output stream does not synchronize writes
 * to its internal buffer. It is intended to only be accessed from a
 * single thread.
 */
public class FramingOutputStream extends ByteBufferOutputStream
{
    public FramingOutputStream ()
    {
        _buffer.put(HEADER_PAD);
    }

    @Override
    public ByteBuffer flip ()
    {
        throw new UnsupportedOperationException("Use frameAndReturnBuffer() instead.");
    }

    @Override
    public void reset ()
    {
        throw new UnsupportedOperationException("Use resetFrame() instead.");
    }

    /**
     * Writes the frame length to the beginning of our buffer and returns
     * it for writing to the appropriate channel. This should be followed
     * by a call to {@link #resetFrame} when the frame has been written.
     */
    public ByteBuffer frameAndReturnBuffer ()
    {
        // flip the buffer which will limit it to its current position
        _buffer.flip();

        // then write the frame length and rewind back to the start of the
        // buffer so that all the data is available
        int count = _buffer.limit();
        _buffer.put((byte)((count >>> 24) & 0xFF));
        _buffer.put((byte)((count >>> 16) & 0xFF));
        _buffer.put((byte)((count >>>  8) & 0xFF));
        _buffer.put((byte)((count >>>  0) & 0xFF));
        _buffer.rewind();

        return _buffer;
    }

    /**
     * Resets our internal buffer and prepares to write a new frame.
     */
    public void resetFrame ()
    {
        _buffer.clear();
        _buffer.put(HEADER_PAD);
    }

    /** We pad the beginning of our buffer so that we can write the frame
     * length when the time comes. */
    protected static final byte[] HEADER_PAD = new byte[4];
}
