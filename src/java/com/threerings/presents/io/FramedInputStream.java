//
// $Id: FramedInputStream.java,v 1.1 2001/05/22 21:51:29 mdb Exp $

package com.samskivert.cocktail.cher.io;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.samskivert.util.StringUtil;

/**
 * The framed input stream reads input that was framed by a framing output
 * stream. Framing in this case simply means writing the length of the
 * frame followed by the data associated with the frame so that an entire
 * frame can be loaded from the network layer before any higher layer
 * attempts to process it. Additionally, any failure in decoding a frame
 * won't result in the entire stream being skewed due to the remainder of
 * the undecoded frame.
 *
 * <p>The framed input stream reads an entire frame worth of data into its
 * internal buffer when <code>readFrame()</code> is called. It then
 * behaves as if this is the only data available on the stream (meaning
 * that when the data in the frame is exhausted, it will behave as if the
 * end of the stream has been reached). A new frame can be read at any
 * time and will be appended to the data available (the frame length data
 * is never inserted into the stream data), but it is assumed that the
 * caller will want to read and process an entire frame before going on to
 * read the next frame (so that <code>clear()</code> can be called in the
 * event of a frame decoding failure without clearing out the data from
 * subsequent frames).
 *
 * <p><em>Note:</em> The framing input stream does not synchronize reads
 * from its internal buffer. It is intended to only be accessed from a
 * single thread.
 *
 * <p>Implementation note: maybe this should derive from
 * <code>FilterInputStream</code> and be tied to a single
 * <code>InputStream</code> for its lifetime.
 */
public class FramedInputStream extends InputStream
{
    public FramedInputStream ()
    {
        _header = new byte[HEADER_SIZE];
        _buffer = new byte[INITIAL_BUFFER_SIZE];
    }

    /**
     * Reads a single frame from the provided input stream and appends
     * that data to the existing data available via the framed input
     * stream's read methods.
     *
     * @return the length of the read frame in bytes.
     */
    public synchronized int readFrame (InputStream source)
        throws IOException
    {
        // first read in the frame length
        if (source.read(_header, 0, HEADER_SIZE) < HEADER_SIZE) {
            throw new EOFException();
        }

        // now decode the frame length
        int flength = (_header[0] << 24) & 0xFF;
        flength += (_header[1] << 16) & 0xFF;
        flength += (_header[2] << 8) & 0xFF;
        flength += _header[3] & 0xFF;

        // expand our buffer to accomodate the frame data
        int newcount = _count + flength;
        if (newcount > _buffer.length) {
            // increase the buffer size in large increments
            byte[] newbuf = new byte[Math.max(_buffer.length << 1, newcount)];
            System.arraycopy(_buffer, 0, newbuf, 0, _count);
            _buffer = newbuf;
        }

        // read the data into the buffer
        source.read(_buffer, _count, flength);
        _count = newcount;

        return flength;
    }

    /**
     * Clears out any previously read frame data and reads a new frame
     * into the buffer.
     *
     * @return the length of the read frame in bytes.
     */
    public int clearAndReadFrame (InputStream source)
        throws IOException
    {
        clear();
        return readFrame(source);
    }

    /**
     * Clears out any frame data already in the buffer including anything
     * that hasn't yet been read.
     */
    public void clear ()
    {
        _pos = 0;
        _count = 0;
    }

    /**
     * Reads the next byte of data from this input stream. The value byte
     * is returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the
     * stream has been reached, the value <code>-1</code> is returned.
     *
     * <p>This <code>read</code> method cannot block.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream has been reached.
     */
    public int read ()
    {
	return (_pos < _count) ? (_buffer[_pos++] & 0xff) : -1;
    }

    /**
     * Reads up to <code>len</code> bytes of data into an array of bytes
     * from this input stream. If <code>pos</code> equals
     * <code>count</code>, then <code>-1</code> is returned to indicate
     * end of file. Otherwise, the number <code>k</code> of bytes read is
     * equal to the smaller of <code>len</code> and
     * <code>count-pos</code>. If <code>k</code> is positive, then bytes
     * <code>buf[pos]</code> through <code>buf[pos+k-1]</code> are copied
     * into <code>b[off]</code> through <code>b[off+k-1]</code> in the
     * manner performed by <code>System.arraycopy</code>. The value
     * <code>k</code> is added into <code>pos</code> and <code>k</code> is
     * returned.
     *
     * <p>This <code>read</code> method cannot block.
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     *
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end of the
     * stream has been reached.
     */
    public int read (byte[] b, int off, int len)
    {
        // sanity check the arguments
	if (b == null) {
	    throw new NullPointerException();
	} else if ((off < 0) || (off > b.length) || (len < 0) ||
		   ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	}

        // figure out how much data we'll return
	if (_pos >= _count) {
	    return -1;
	}
	if (_pos + len > _count) {
	    len = _count - _pos;
	}
	if (len <= 0) {
	    return 0;
	}

        // copy and advance
	System.arraycopy(_buffer, _pos, b, off, len);
	_pos += len;

	return len;
    }

    /**
     * Skips <code>n</code> bytes of input from this input stream. Fewer
     * bytes might be skipped if the end of the input stream is reached.
     * The actual number <code>k</code> of bytes to be skipped is equal to
     * the smaller of <code>n</code> and <code>count-pos</code>. The value
     * <code>k</code> is added into <code>pos</code> and <code>k</code> is
     * returned.
     *
     * @param n the number of bytes to be skipped.
     *
     * @return the actual number of bytes skipped.
     */
    public long skip (long n)
    {
	if (_pos + n > _count) {
	    n = _count - _pos;
	}
	if (n <= 0) {
	    return 0;
	}
	_pos += n;
	return n;
    }

    /**
     * Returns the number of bytes that can be read from this input stream
     * without blocking. The value returned is <code>count - pos</code>,
     * which is the number of bytes remaining to be read from the input
     * buffer.
     *
     * @return the number of bytes remaining to be read from the buffered
     * frames.
     */
    public int available ()
    {
	return _count - _pos;
    }

    /**
     * Always returns false as framed input streams do not support
     * marking.
     */
    public boolean markSupported ()
    {
	return false;
    }

    /**
     * Does nothing, as marking is not supported.
     */
    public void mark (int readAheadLimit)
    {
        // not supported; do nothing
    }

    /**
     * Resets the buffer to the beginning of the buffered frames.
     */
    public void reset ()
    {
	_pos = 0;
    }

    protected byte[] _header;
    protected byte[] _buffer;
    protected int _pos;
    protected int _count;

    /** The size of the frame header (a 32-bit integer). */
    protected static final int HEADER_SIZE = 4;

    /** The default initial size of the internal buffer. */
    protected static final int INITIAL_BUFFER_SIZE = 32;
}
