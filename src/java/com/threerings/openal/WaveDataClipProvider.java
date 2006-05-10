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

import java.io.IOException;

import java.nio.ByteBuffer;

import org.lwjgl.util.WaveData;

/**
 * Loads clips via the class loader using LWJGL's {@link WaveData} utility
 * class.
 */
public class WaveDataClipProvider implements ClipProvider
{
    public Clip loadClip (String path) throws IOException
    {
        WaveDataClip clip = new WaveDataClip();
        WaveData file = WaveData.create(path);
        if (file == null) {
            throw new IOException("Error loading " + path);
        }
        clip.format = file.format;
        clip.frequency = file.samplerate;
        clip.data = file.data;
        return clip;
    }
}
