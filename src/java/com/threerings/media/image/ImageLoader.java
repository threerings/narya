//
// $Id: ImageLoader.java,v 1.2 2003/01/08 04:09:02 mdb Exp $

package com.threerings.media.image;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

/**
 * In order to take advantage of the new J2SE 1.4 image loading facilites
 * (in the form of <code>ImageIO</code> and friends), while preserving the
 * ability to operate on J2SE 1.3 and previous, we have the image loader
 * interface. It allows us to attempt to load the ImageIO based loader and
 * fall back if the class is not available.
 */
public interface ImageLoader
{
    /**
     * Load up the image from the supplied input stream.
     */
    public Image loadImage (InputStream source)
        throws IOException;
}
