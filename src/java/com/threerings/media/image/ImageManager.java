//
// $Id: ImageManager.java,v 1.11 2001/12/13 05:15:16 mdb Exp $

package com.threerings.media;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import java.io.InputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
     * ResourceManager} from which it will obtain its data. A non-null
     * <code>context</code> must be provided if there is any expectation
     * that the image manager will not be able to load images via the
     * ImageIO services and will have to fallback to Toolkit-style
     * loading.
     */
    public ImageManager (ResourceManager rmgr, Component context)
    {
	_rmgr = rmgr;

        // obtain information on our graphics environment
        GraphicsEnvironment genv =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gdev = genv.getDefaultScreenDevice();
        _gconf = gdev.getDefaultConfiguration();

        // try to figure out which image loader we'll be using
        try {
            _loader = (ImageLoader)Class.forName(IMAGEIO_LOADER).newInstance();
        } catch (Throwable t) {
            Log.info("Unable to use ImageIO to load images. " +
                     "Falling back to Toolkit [error=" + t + "].");
            _loader = new ToolkitLoader(context);
        }
    }

    /**
     * Loads the image via the resource manager using the specified path
     * and caches it for faster retrieval henceforth.
     */
    public Image getImage (String path)
        throws IOException
    {
	Image img = (Image)_imgs.get(path);
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
    public Image createImage (InputStream source)
        throws IOException
    {
        Image src = _loader.loadImage(source);
        int swidth = src.getWidth(null);
        int sheight = src.getHeight(null);

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

    /** The image loader via which we convert an input stream into an
     * image. */
    protected ImageLoader _loader;

    /** A cache of loaded images. */
    protected HashMap _imgs = new HashMap();

    /** The graphics configuration of our default display device. */
    protected GraphicsConfiguration _gconf;

    /** The classname of the ImageIO-based image loader which we attempt
     * to use but fallback from if we're not running a JVM that has
     * ImageIO support. */
    protected static final String IMAGEIO_LOADER =
        "com.threerings.media.ImageIOLoader";
}
