//
// $Id: ImageProvider.java,v 1.1 2001/11/18 04:09:21 mdb Exp $

package com.threerings.media.tile;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * A tileset needs to load its images from some location. That will
 * generally either be via the {@link TileManager} that constructed it or
 * the {@link TileSetRepository} that constructed it. The tile manager
 * loads images via the resource manager, whereas the tileset repository
 * will likely obtain images via its own resource bundles.
 */
public interface ImageProvider
{
    /**
     * Loads the image with the specified path.
     *
     * @exception IOException thrown if an error occurs loading the image
     * data.
     */
    public BufferedImage loadImage (String path)
        throws IOException;
}
