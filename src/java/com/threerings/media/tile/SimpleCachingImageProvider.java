//
// $Id: SimpleCachingImageProvider.java,v 1.2 2003/01/14 00:23:19 mdb Exp $

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
