//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.openal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;

import org.lwjgl.openal.AL10;

import com.jmex.sound.openAL.objects.util.OggInputStream;

/**
 * An audio stream read from an Ogg Vorbis file.
 */
public class OggFileStream extends Stream
{
    /**
     * Creates a new Ogg stream for the specified Ogg file.
     *
     * @param loop whether or not to play the file in a continuous loop
     */
    public OggFileStream (SoundManager soundmgr, File file, boolean loop)
        throws IOException
    {
        super(soundmgr);
        _file = file;
        _istream = new OggInputStream(new FileInputStream(_file));
        _loop = loop;
    }
    
    // documentation inherited
    protected int getFormat ()
    {
        return (_istream.getFormat() == OggInputStream.FORMAT_MONO16) ?
            AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
    }
    
    // documentation inherited
    protected int getFrequency ()
    {
        return _istream.getRate();
    }
    
    // documentation inherited
    protected int populateBuffer (ByteBuffer buf)
        throws IOException
    {
        int read = _istream.read(buf, buf.position(), buf.remaining());
        while (buf.hasRemaining() && _loop) {
            _istream = new OggInputStream(new FileInputStream(_file));
            read = Math.max(0, read);
            read += _istream.read(buf, buf.position(), buf.remaining());
        }
        return read;
    }
    
    /** The Ogg file from which to read. */
    protected File _file;
    
    /** The underlying Ogg input stream. */
    protected OggInputStream _istream;
    
    /** Whether or not to loop back to the beginning of the file when we've
     * reached the end. */
    protected boolean _loop;
}
