//
// $Id: FramedInputStream.java,v 1.5 2001/08/03 03:10:15 mdb Exp $

package com.threerings.cocktail.cher.io;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.samskivert.util.StringUtil;

import com.threerings.cocktail.cher.Log;

/**
 * The framed input stream reads input that was framed by a framing output
 * stream. Framing in this case simply means writing the length of the
 * frame followed by the data associated with the frame so that an entire
 * frame can be loaded from the network layer before any higher layer
 * attempts to process it. Additionally, any failure in decoding a frame
 * won't result in the entire stream being skewed due to the remainder of
 * the undecoded frame remaining in the input stream.
 *
 * <p>The framed input stream reads an entire frame worth of data into its
 * internal buffer when <code>readFrame()</code> is called. It then
 * behaves as if this is the only data available on the stream (meaning
 * that when the data in the frame is exhausted, it will behave as if the
 * end of the stream has been reached). The buffer can only contain a
 * single frame at a time, so any data left over from a previous frame
 * will disappear when <code>readFrame()</code> is called again.
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
     * Reads a frame from the provided input stream, or appends to a
     * partially read frame. Appends the read data to the existing data
     * available via the framed input stream's read methods. If the entire
     * frame data is not yet available, <code>readFrame</code> will return
     * false, otherwise true.
     *
     * <p> The code assumes that it will be able to read the entire frame
     * header in a single read. The header is only four bytes and should
     * always arrive at the beginning of a packet, so unless something is
     * very funky with the networking layer, this should be a safe
     * assumption.
     *
     * @return true if the entire frame has been read, false if the buffer
     * contains only a partial frame.
     */
    public boolean readFrame (InputStream source)
        throws IOException
    {
        // if the buffer currently contains a complete frame, that means
        // we're not halfway through reading a frame and that we can start
        // anew.
        if (_count == _length) {
            // read in the frame length
            int got = source.read(_header, 0, HEADER_SIZE);
            if (got < 0) {
                throw new EOFException();

            } else if (got == 0) {
                Log.info("Woke up to read data, but there ain't none. Sigh.");
                return false;

            } else if (got < HEADER_SIZE) {
                String errmsg = "FramedInputStream does not support " +
                    "partially reading the header. Needed " + HEADER_SIZE +
                    " bytes, got " + got + " bytes.";
                throw new RuntimeException(errmsg);
            }

            // now that we've read our new frame length, we can clear out
            // any prior data
            _pos = 0;
            _count = 0;

            // decode the frame length
            _length = (_header[0] << 24) & 0xFF;
            _length += (_header[1] << 16) & 0xFF;
            _length += (_header[2] << 8) & 0xFF;
            _length += _header[3] & 0xFF;

            // if necessary, expand our buffer to accomodate the frame
            if (_length > _buffer.length) {
                // increase the buffer size in large increments
                _buffer = new byte[Math.max(_buffer.length << 1, _length)];
            }
        }

        // read the data into the buffer
        int got = source.read(_buffer, _count, _length-_count);
        if (got < 0) {
            throw new EOFException();
        }
        _count += got;

        return (_count == _length);
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
    protected int _length;

    protected byte[] _buffer;
    protected int _pos;
    protected int _count;

    /** The size of the frame header (a 32-bit integer). */
    protected static final int HEADER_SIZE = 4;

    /** The default initial size of the internal buffer. */
    protected static final int INITIAL_BUFFER_SIZE = 32;
}
