//
// $Id: FramedInputStream.java,v 1.2 2002/11/18 18:51:33 mdb Exp $

package com.threerings.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
    /**
     * Creates a new framed input stream.
     */
    public FramedInputStream ()
    {
        _buffer = ByteBuffer.allocate(INITIAL_BUFFER_CAPACITY);
    }

    /**
     * Reads a frame from the provided channel, appending to any partially
     * read frame. If the entire frame data is not yet available,
     * <code>readFrame</code> will return false, otherwise true.
     *
     * <p> <em>Note:</em> when this method returns true, it is required
     * that the caller read <em>all</em> of the frame data from the stream
     * before again calling {@link #readFrame} as the previous frame's
     * data will be elimitated upon the subsequent call.
     *
     * @return true if the entire frame has been read, false if the buffer
     * contains only a partial frame.
     */
    public boolean readFrame (SocketChannel source)
        throws IOException
    {
        // flush data from any previous frame from the buffer
        if (_buffer.limit() == _length) {
            // this will remove the old frame's bytes from the buffer,
            // shift our old data to the start of the buffer, position the
            // buffer appropriately for appending new data onto the end of
            // our existing data, and set the limit to the capacity
            _buffer.limit(_have);
            _buffer.position(_length);
            _buffer.compact();
            _have -= _length;

            // we may have picked up the next frame in a previous read, so
            // try decoding the length straight away
            _length = decodeLength();
        }

        // we may already have the next frame entirely in the buffer from
        // a previous read
        if (checkForCompleteFrame()) {
            return true;
        }

        // read whatever data we can from the source
        do {
            int got = source.read(_buffer);
            if (got == -1) {
                throw new EOFException();
            }
            _have += got;

            // if there's room remaining in the buffer, that means we've
            // read all there is to read, so we can move on to inspecting
            // what we've got
            if (_buffer.remaining() > 0) {
                break;
            }

            // otherwise, we've filled up our buffer as a result of this
            // read, expand it and try reading some more
            ByteBuffer newbuf = ByteBuffer.allocate(_buffer.capacity() << 1);
            newbuf.put((ByteBuffer)_buffer.flip());
            _buffer = newbuf;

            // don't let things grow without bounds
        } while (_buffer.capacity() < MAX_BUFFER_CAPACITY);

        // if we didn't already have our length, see if we now have enough
        // data to obtain it
        if (_length == -1) {
            _length = decodeLength();
        }

        // finally check to see if there's a complete frame in the buffer
        // and prepare to serve it up if there is
        return checkForCompleteFrame();
    }

    /**
     * Decodes and returns the length of the current frame from the buffer
     * if possible. Returns -1 otherwise.
     */
    protected final int decodeLength ()
    {
        // if we don't have enough bytes to determine our frame size, stop
        // here and let the caller know that we're not ready
        if (_have < HEADER_SIZE) {
            return -1;
        }

        // decode the frame length
        _buffer.rewind();
        int length = (_buffer.get() & 0xFF) << 24;
        length += (_buffer.get() & 0xFF) << 16;
        length += (_buffer.get() & 0xFF) << 8;
        length += (_buffer.get() & 0xFF);
        _buffer.position(_have);

        return length;
    }

    /**
     * Returns true if a complete frame is in the buffer, false otherwise.
     * If a complete frame is in the buffer, the buffer will be prepared
     * to deliver that frame via our {@link InputStream} interface.
     */
    protected final boolean checkForCompleteFrame ()
    {
        if (_length == -1 || _have < _length) {
            return false;
        }

        // prepare the buffer such that this frame can be read
        _buffer.position(HEADER_SIZE);
        _buffer.limit(_length);
        return true;
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
        return (_buffer.remaining() > 0) ? (_buffer.get() & 0xFF) : -1;
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
        // if they want no bytes, we give them no bytes; this is
        // purportedly the right thing to do regardless of whether we're
        // at EOF or not
        if (len == 0) {
            return 0;
        }

        // trim the amount to be read to what is available; if they wanted
        // bytes and we have none, return -1 to indicate EOF
        if ((len = Math.min(len, _buffer.remaining())) == 0) {
            return -1;
        }

        _buffer.get(b, off, len);
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
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the number of bytes that can be read from this input stream
     * without blocking.
     *
     * @return the number of bytes remaining to be read from the buffered
     * frame.
     */
    public int available ()
    {
	return _buffer.remaining();
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
        // position our buffer at the beginning of the frame data
        _buffer.position(HEADER_SIZE);
    }

    /** The buffer in which we maintain our frame data. */
    protected ByteBuffer _buffer;

    /** The length of the current frame being read. */
    protected int _length = -1;

    /** The number of bytes total that we have in our buffer (these bytes
     * may comprise more than one frame. */
    protected int _have = 0;

    /** The size of the frame header (a 32-bit integer). */
    protected static final int HEADER_SIZE = 4;

    /** The default initial size of the internal buffer. */
    protected static final int INITIAL_BUFFER_CAPACITY = 32;

    /** No need to get out of hand. */
    protected static final int MAX_BUFFER_CAPACITY = 512 * 1024;
}
