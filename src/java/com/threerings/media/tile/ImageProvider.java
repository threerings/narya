//
// $Id: ImageProvider.java,v 1.3 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.threerings.media.image.Colorization;
import com.threerings.media.image.Mirage;

/**
 * Provides a generic interface via which tileset images may be loaded. In
 * most cases, a running application will want to obtain images via the
 * {@link ImageManager}, but in some circumstances a simpler image
 * provider may be desirable to avoid the overhead of the image manager
 * infrastructure when simple image loading is all that is desired.
 */
public interface ImageProvider
{
    /**
     * Returns the raw tileset image with the specified path.
     *
     * @param path the path that identifies the desired image (corresponds
     * to the image path from the tileset).
     * @param zations if non-null, colorizations to apply to the source
     * image before returning it.
     */
    public BufferedImage getTileSetImage (String path, Colorization[] zations);

    /**
     * Obtains the tile image with the specified path in the form of a
     * {@link Mirage}. It should be cropped from the tileset image
     * identified by the supplied path.
     *
     * @param path the path that identifies the desired image (corresponds
     * to the image path from the tileset).
     * @param bounds if non-null, the region of the image to be returned
     * as a mirage. If null, the entire image should be returned.
     * @param zations if non-null, colorizations to apply to the image
     * before converting it into a mirage.
     */
    public Mirage getTileImage (String path, Rectangle bounds,
                                Colorization[] zations);
}
