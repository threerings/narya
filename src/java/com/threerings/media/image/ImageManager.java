//
// $Id: ImageManager.java,v 1.8 2001/11/30 02:33:34 mdb Exp $

package com.threerings.media;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import java.io.InputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.threerings.resource.ResourceManager;
import com.threerings.media.Log;

/**
 * Provides a single point of access for image retrieval and caching.
 */
public class ImageManager
{
    /**
     * Construct an image manager with the specified {@link
     * ResourceManager} from which it will obtain its data.
     */
    public ImageManager (ResourceManager rmgr)
    {
	_rmgr = rmgr;

        // obtain information on our graphics environment
        GraphicsEnvironment genv =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gdev = genv.getDefaultScreenDevice();
        _gconf = gdev.getDefaultConfiguration();
    }

    /**
     * Loads the image via the resource manager using the specified path
     * and caches it for faster retrieval henceforth.
     */
    public BufferedImage getImage (String path)
        throws IOException
    {
	BufferedImage img = (BufferedImage)_imgs.get(path);
	if (img != null) {
	    // Log.info("Retrieved image from cache [path=" + path + "].");
	    return img;
	}

	// Log.info("Loading image into cache [path=" + path + "].");
        img = createImage(_rmgr.getResource(path));
        _imgs.put(path, img);
        return img;
    }

    /**
     * Creates an image that is optimized for rendering in the client's
     * environment and decodes the image data from the specified source
     * image into that target image. The resulting image is not cached.
     */
    public BufferedImage createImage (InputStream source)
        throws IOException
    {
        // this seems to choke when decoding the compressed image data
        // which may mean it's a JDK bug or something, but I'd like to see
        // it resolved so that the image manager will work on applets
        // 
        // ImageInputStream iis = new MemoryCacheImageInputStream(source);
        // BufferedImage src = ImageIO.read(iis);

        BufferedImage src = ImageIO.read(source);
        int swidth = src.getWidth();
        int sheight = src.getHeight();

        // now convert the image to a format optimized for display
        BufferedImage dest = _gconf.createCompatibleImage(
            swidth, sheight, Transparency.BITMASK);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        return dest;
    }

    /** A reference to the resource manager via which we load image data
     * by default. */
    protected ResourceManager _rmgr;

    /** A cache of loaded images. */
    protected HashMap _imgs = new HashMap();

    /** The graphics configuration of our default display device. */
    protected GraphicsConfiguration _gconf;
}
