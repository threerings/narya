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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.nio.ByteBuffer;

import org.lwjgl.openal.AL10;

import com.samskivert.io.ByteArrayOutInputStream;

import com.jmex.sound.openAL.objects.util.OggInputStream;

/**
 * A sound clip backed by Ogg Vorbis data loaded from a file.
 */
public class OggFileClip extends Clip
{
    /**
     * Loads the clip from the given file.
     */
    public OggFileClip (File file)
        throws IOException
    {
        // read the file in completely
        FileInputStream in = new FileInputStream(file);
        _data = new ByteArrayOutInputStream((int)file.length());
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) > 0) {
            _data.write(buffer, 0, read);
        }
        _istream = new OggInputStream(_data.getInputStream());
        format = (_istream.getFormat() == OggInputStream.FORMAT_MONO16) ?
            AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
        frequency = _istream.getRate();
    }
    
    // documentation inherited
    public ByteBuffer getData ()
        throws IOException
    {
        // we keep the Ogg stream from initialization, but reset it when we
        // re-read the data
        if (_istream == null) {
            _istream = new OggInputStream(_data.getInputStream());
        }
        // decompress the entire stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = _istream.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, read);
        }
        byte[] bytes = out.toByteArray();
        ByteBuffer data = ByteBuffer.allocateDirect(bytes.length);
        data.put(bytes);
        data.rewind();
        _istream = null;
        return data;
    }
    
    /** The entire encoded file. */
    protected ByteArrayOutInputStream _data;
    
    /** The Ogg input stream for decoding the data. */
    protected OggInputStream _istream;
}
