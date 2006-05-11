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

import java.util.ArrayList;

import org.lwjgl.openal.AL10;

import com.jmex.sound.openAL.objects.util.OggInputStream;

/**
 * An audio stream read from one or more Ogg Vorbis files.
 */
public class OggFileStream extends Stream
{
    /**
     * Creates a new Ogg stream for the specified file.
     *
     * @param loop whether or not to play the file in a continuous loop
     * if there's nothing on the queue
     */
    public OggFileStream (SoundManager soundmgr, File file, boolean loop)
        throws IOException
    {
        super(soundmgr);
        _file = new OggFile(file, loop);
        _istream = new OggInputStream(new FileInputStream(file));
    }
    
    /**
     * Adds a file to the queue of files to play.
     *
     * @param loop if true, play this file in a loop if there's nothing else
     * on the queue
     */
    public void queueFile (File file, boolean loop)
    {
        _queue.add(new OggFile(file, loop));
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
        while (buf.hasRemaining() && (!_queue.isEmpty() || _file.loop)) {
            if (!_queue.isEmpty()) {
                _file = _queue.remove(0);
            }
            _istream = new OggInputStream(new FileInputStream(_file.file));
            read = Math.max(0, read);
            read += _istream.read(buf, buf.position(), buf.remaining());
        }
        return read;
    }
    
    /** The file currently being played. */
    protected OggFile _file;
    
    /** The underlying Ogg input stream for the current file. */
    protected OggInputStream _istream;
    
    /** The queue of files to play after the current one. */
    protected ArrayList<OggFile> _queue = new ArrayList<OggFile>();
    
    /** A file queued for play. */
    protected class OggFile
    {
        /** The file to play. */
        public File file;
        
        /** Whether or not to play the file in a loop when there's nothing
         * in the queue. */
        public boolean loop;
        
        public OggFile (File file, boolean loop)
        {
            this.file = file;
            this.loop = loop;
        }
    }
}
