//
// $Id: ImageIOLoader.java,v 1.6 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.image;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.samskivert.util.StringUtil;
import java.io.RandomAccessFile;

import com.threerings.media.Log;

/**
 * Loads images using the <code>ImageIO</code> services provided by J2SE
 * 1.4 (and, presumably, above).
 */
public class ImageIOLoader implements ImageLoader
{
    public ImageIOLoader ()
    {
        // we need to reference ImageIO in the constructor to force the
        // classloader to attempt to load the ImageIO classes
        ImageIO.setUseCache(true);

//         String[] fmts = ImageIO.getReaderFormatNames();
//         Log.info("Got formats " + StringUtil.toString(fmts) + ".");
        
        Iterator iter = ImageIO.getImageReadersByFormatName("png");
        if (iter.hasNext()) {
            _reader = (ImageReader)iter.next();
        } else {
            Log.warning("Aiya! No reader.");
        }
    }

    // documentation inherited
    public Image loadImage (InputStream source)
        throws IOException
    {
        // this seems to choke when decoding the compressed image data
        // which may mean it's a JDK bug or something, but I'd like to see
        // it resolved so that the image manager will work on applets

//         ImageInputStream iis = null;
// // //         iis = ImageIO.createImageInputStream(source);
//         iis = new MemoryCacheImageInputStream(source);

// // //         Log.info("Created stream " + iis + "/" + source);
//         _reader.setInput(iis, true, false);
//         return _reader.read(0);

//         return ImageIO.read(new MemoryCacheImageInputStream(source));
        return ImageIO.read(source);
    }

    protected ImageReader _reader;
}
