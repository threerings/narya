//
// $Id: ImageDataProvider.java,v 1.1 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.image;

import java.io.IOException;
import javax.imageio.stream.ImageInputStream;

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
     * Returns an input stream from which the image data for the specified
     * image may be loaded.
     */
    public ImageInputStream loadImageData (String path) throws IOException;
}
