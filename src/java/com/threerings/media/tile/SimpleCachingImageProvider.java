//
// $Id: SimpleCachingImageProvider.java,v 1.3 2004/08/27 02:12:41 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.media.tile;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.samskivert.util.LRUHashMap;

import com.threerings.media.Log;
import com.threerings.media.image.BufferedMirage;
import com.threerings.media.image.Colorization;
import com.threerings.media.image.Mirage;

/**
 * An image provider that can be used by command line tools to load images
 * and provide them to tilesets when doing things like preprocessing
 * tileset images.
 */
public abstract class SimpleCachingImageProvider implements ImageProvider
{
    // documentation inherited from interface
    public BufferedImage getTileSetImage (String path, Colorization[] zations)
    {
        BufferedImage image = (BufferedImage)_cache.get(path);
        if (image == null) {
            try {
                image = loadImage(path);
                _cache.put(path, image);
            } catch (IOException ioe) {
                Log.warning("Failed to load image [path=" + path +
                            ", ioe=" + ioe + "].");
            }
        }
        return image;
    }

    // documentation inherited from interface
    public Mirage getTileImage (String path, Rectangle bounds,
                                Colorization[] zations)
    {
        // mostly fake it
        BufferedImage tsimg = getTileSetImage(path, zations);
        tsimg = tsimg.getSubimage(bounds.x, bounds.y,
                                  bounds.width, bounds.height);
        return new BufferedMirage(tsimg);
    }

    /**
     * Derived classes must implement this method to actually load the raw
     * source images.
     */
    protected abstract BufferedImage loadImage (String path)
        throws IOException;

    protected LRUHashMap _cache = new LRUHashMap(10);
}
