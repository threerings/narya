//
// $Id: ImageDataProvider.java,v 1.2 2003/04/27 06:33:11 mdb Exp $

package com.threerings.media.image;

import java.io.IOException;
import java.awt.image.BufferedImage;

/**
 * Provides access to image data for the image with the specified
 * path. Images loaded from different data providers (which are
 * differentiated by reference equality) will be considered distinct
 * images with respect to caching.
 */
public interface ImageDataProvider
{
    /**
     * Returns a string identifier for this image data provider which wil
     * be used to differentiate it from other providers and thus should be
     * unique.
     */
    public String getIdent ();

    /**
     * Returns the image at the specified path.
     */
    public BufferedImage loadImage (String path) throws IOException;
}
