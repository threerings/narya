//
// $Id: FramingOutputStream.java,v 1.3 2002/11/18 18:51:33 mdb Exp $

package com.threerings.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.nio.BufferOverflowException;
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
public class FramingOutputStream extends OutputStream
{
    public FramingOutputStream ()
    {
        _buffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
        _buffer.put(HEADER_PAD);
    }

    /**
     * Writes the specified byte to this framing output stream. 
     *
     * @param b the byte to be written.
     */
    public void write (int b)
    {
        try {
            _buffer.put((byte)b);
        } catch (BufferOverflowException boe) {
            expand(1);
            _buffer.put((byte)b);
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this framing output stream.
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     */
    public void write (byte[] b, int off, int len)
    {
        // sanity check the arguments
	if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return;
	}

        try {
            _buffer.put(b, off, len);
        } catch (BufferOverflowException boe) {
            expand(len);
            _buffer.put(b, off, len);
        }
    }

    /**
     * Expands our buffer to accomodate the specified capacity.
     */
    protected final void expand (int needed)
    {
        int ocapacity = _buffer.capacity();
        int ncapacity = _buffer.position() + needed;
	if (ncapacity > ocapacity) {
            // increase the buffer size in large increments
            ncapacity = Math.max(ocapacity << 1, ncapacity);
            ByteBuffer newbuf = ByteBuffer.allocate(ncapacity);
            newbuf.put((ByteBuffer)_buffer.flip());
	    _buffer = newbuf;
	}
    }

    /**
     * Writes the frame length to the beginning of our buffer and returns
     * it for writing to the appropriate channel. This should be followed
     * by a call to {@link #reset} when the frame has been written.
     */
    public ByteBuffer frameAndReturnBuffer ()
    {
        // flip the buffer which will limit it to it's current position
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

    /** The buffer in which we store our frame data. */
    protected ByteBuffer _buffer;

    /** The default initial size of the internal buffer. */
    protected static final int INITIAL_BUFFER_SIZE = 32;

    /** We pad the beginning of our buffer so that we can write the frame
     * length when the time comes. */
    protected static final byte[] HEADER_PAD = new byte[4];
}
