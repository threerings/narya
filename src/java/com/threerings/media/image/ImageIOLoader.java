//
// $Id: ImageIOLoader.java,v 1.1 2001/12/07 01:33:29 mdb Exp $

package com.threerings.media;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Loads images using the <code>ImageIO</code> services provided by J2SE
 * 1.4 (and, presumably, above).
 */
public class ImageIOLoader
{
    // documentation inherited
    public Image loadImage (InputStream source)
        throws IOException
    {
        // this seems to choke when decoding the compressed image data
        // which may mean it's a JDK bug or something, but I'd like to see
        // it resolved so that the image manager will work on applets
        // 
        // ImageInputStream iis = new MemoryCacheImageInputStream(source);
        // return ImageIO.read(iis);

        return ImageIO.read(source);
    }
}
