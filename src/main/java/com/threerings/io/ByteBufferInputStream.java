//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.io;

import java.io.IOException;
import java.io.InputStream;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

/**
 * Reads input from a {@link ByteBuffer}.
 */
public class ByteBufferInputStream extends InputStream
{
    /**
     * Creates a new input stream to read from the specified buffer.
     */
    public ByteBufferInputStream (ByteBuffer buffer)
    {
        _buffer = buffer;
    }

    /**
     * Returns a reference to the underlying buffer.
     */
    public ByteBuffer getBuffer ()
    {
        return _buffer;
    }

    @Override
    public int read ()
    {
        try {
            return (_buffer.get() & 0xFF);
        } catch (BufferUnderflowException e) {
            return -1;
        }
    }

    @Override
    public int read (byte[] b, int offset, int length)
        throws IOException
    {
        length = Math.min(length, _buffer.remaining());
        if (length <= 0) {
            return -1;
        }
        _buffer.get(b, offset, length);
        return length;
    }

    @Override
    public long skip (long n)
        throws IOException
    {
        n = Math.min(n, _buffer.remaining());
        _buffer.position((int)(_buffer.position() + n));
        return n;
    }

    @Override
    public int available ()
    {
        return _buffer.remaining();
    }

    @Override
    public boolean markSupported ()
    {
        return true;
    }

    @Override
    public void mark (int readLimit)
    {
        _buffer.mark();
    }

    @Override
    public void reset ()
        throws IOException
    {
        try {
            _buffer.reset();
        } catch (InvalidMarkException e) {
            throw new IOException("No mark set.");
        }
    }

    /** The buffer from which we read. */
    protected ByteBuffer _buffer;
}

