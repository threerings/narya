//
// $Id: FramingOutputStream.java,v 1.4 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The framing output stream accumulates output into a byte array just
 * like the byte array output stream, but can then be instructed to send
 * its contents down another output stream, prefixed by the length
 * (written as an integer) of those contents. It does this efficiently so
 * that data is copied as little as possible and so that the output stream
 * to which the data is written need not be buffered because the framed
 * output is written in a single call to <code>write()</code>.
 *
 * <p><em>Note:</em> The framing output stream does not synchronize writes
 * to its internal buffer. It is intended to only be accessed from a
 * single thread.
 *
 * <p>Implementation note: maybe this should derive from
 * <code>FilterOutputStream</code> and be tied to a single
 * <code>OutputStream</code> for its lifetime.
 */
public class FramingOutputStream extends OutputStream
{
    public FramingOutputStream ()
    {
        _buffer = new byte[INITIAL_BUFFER_SIZE];
        _count = 4; // leave room for the frame size at the beginning
    }

    /**
     * Writes the specified byte to this framing output stream. 
     *
     * @param b the byte to be written.
     */
    public void write (int b)
    {
        // expand our buffer if necessary
	int newcount = _count + 1;
	if (newcount > _buffer.length) {
            // increase the buffer size in large increments
	    byte[] newbuf = new byte[Math.max(_buffer.length << 1, newcount)];
	    System.arraycopy(_buffer, 0, newbuf, 0, _count);
	    _buffer = newbuf;
	}

        // copy and advance
	_buffer[_count] = (byte)b;
	_count = newcount;
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

        // expand the buffer if necessary
        int newcount = _count + len;
        if (newcount > _buffer.length) {
            // increase the buffer size in large increments
            byte[] newbuf = new byte[Math.max(_buffer.length << 1, newcount)];
            System.arraycopy(_buffer, 0, newbuf, 0, _count);
            _buffer = newbuf;
        }

        // copy and advance
        System.arraycopy(b, off, _buffer, _count, len);
        _count = newcount;
    }

    /**
     * Writes the contents of this framing output stream to the target
     * output stream, prefixed by an integer with value equal to the
     * number of bytes written folling that integer. It then resets the
     * framing output stream to prepare for another framed message.
     */
    public void writeFrameAndReset (OutputStream target)
        throws IOException
    {
        // prefix the frame with the byte count in network byte order (the
        // format used by DataOutputStream)
        int count = _count - 4;
        _buffer[0] = (byte)((count >>> 24) & 0xFF);
	_buffer[1] = (byte)((count >>> 16) & 0xFF);
	_buffer[2] = (byte)((count >>>  8) & 0xFF);
	_buffer[3] = (byte)((count >>>  0) & 0xFF);

        // write the data
        target.write(_buffer, 0, _count);

        // reset our internal buffer
        reset();
    }

    public void reset ()
    {
        // leave room for the frame size at the beginning
        _count = 4;
    }

    protected byte[] _buffer;
    protected int _count;

    /** The default initial size of the internal buffer. */
    protected static final int INITIAL_BUFFER_SIZE = 32;
}
