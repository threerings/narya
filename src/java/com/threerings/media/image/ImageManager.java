//
// $Id: ImageManager.java,v 1.31 2002/12/07 02:04:32 shaper Exp $

package com.threerings.media;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.Iterator;

import javax.imageio.ImageReader;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.samskivert.io.NestableIOException;
import com.samskivert.util.LRUHashMap;
import com.samskivert.util.Throttle;

import com.threerings.media.Log;
import com.threerings.media.util.ImageUtil;
import com.threerings.resource.ResourceManager;

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
     * Loads up the requested image from the specified resource set and
     * caches it for faster retrieval henceforth.
     */
    public Image getImage (String rset, String path)
    {
        if (rset == null || path == null) {
            String errmsg = "Must supply non-null resource set and path " +
                "[rset=" + rset + ", path=" + path + "]";
            throw new IllegalArgumentException(errmsg);
        }

        // periodically report our image cache performance
        reportCachePerformance();

        String key = rset + ":" + path;
	Image img = (Image)_imgs.get(key);
	if (img != null) {
	    // Log.info("Retrieved image from cache [path=" + path + "].");
	    return img;
	}

        InputStream imgin = null;
        try {
            // first attempt to load the image from the specified resource set
            try {
                imgin = _rmgr.getResource(rset, path);
            } catch (FileNotFoundException fnfe) {
                // fall through and try the classpath
            }

            // if that fails, attempt to load the image from the classpath
            if (imgin == null) {
                imgin = _rmgr.getResource(path);
//                 Log.info("Fell back to classpath [rset=" + rset +
//                          ", path=" + path + "].");
            }

            // now load up and optimize the image for display
            img = createImage(imgin);

        } catch (IOException ioe) {
            Log.warning("Failure loading image [rset=" + rset +
                        ", path=" + path + ", error=" + ioe + "].");
        }

        // Log.info("Loading image into cache [path=" + path + "].");
        if (img != null) {
            _imgs.put(key, img);
        } else {
            Log.warning("Unable to load image " +
                        "[rset=" + rset + ", path=" + path + "].");
            Thread.dumpStack();
            // fake it so that we don't crap out
            img = ImageUtil.createImage(1, 1);
        }
        return img;
    }

    /**
     * Loads the image via the resource manager using the specified path
     * and caches it for faster retrieval henceforth.
     */
    public Image getImage (String path)
    {
        // periodically report our image cache performance
        reportCachePerformance();

	Image img = (Image)_imgs.get(path);
	if (img != null) {
	    // Log.info("Retrieved image from cache [path=" + path + "].");
	    return img;
	}

        try {
            img = createImage(_rmgr.getResource(path));
        } catch (IOException ioe) {
            Log.warning("Failure loading image [path=" + path +
                        ", error=" + ioe + "].");
        }

        // Log.info("Loading image into cache [path=" + path + "].");
        if (img != null) {
            _imgs.put(path, img);
        } else {
            Log.warning("Unable to load image [path=" + path + "].");
            Thread.dumpStack();
            // fake it so that we don't crap out
            img = ImageUtil.createImage(1, 1);
        }
        return img;
    }

    /**
     * Reports statistics detailing the image manager cache performance
     * and the current size of the cached images.
     */
    protected void reportCachePerformance ()
    {
        if (!_cacheStatThrottle.throttleOp()) {
            long size = ImageUtil.getEstimatedMemoryUsage(
                _imgs.values().iterator());
            int[] eff = _imgs.getTrackedEffectiveness();
            Log.debug("ImageManager LRU [mem=" + (size / 1024) + "k" +
                      ", size=" + _imgs.size() +  ", hits=" + eff[0] +
                      ", misses=" + eff[1] + "].");
        }
    }

    /**
     * Loads the image via the resource manager using the specified
     * path. Does no caching and does not convert the image for optimized
     * display on the target graphics configuration. Instead the original
     * image as returned by the image loader is returned.
     */
    public Image loadImage (String path)
        throws IOException
    {
        try {
            InputStream imgin = _rmgr.getResource(path);
            BufferedInputStream bin = new BufferedInputStream(imgin);
            return _loader.loadImage(bin);
        } catch (IllegalArgumentException iae) {
            String errmsg = "Error loading image [path=" + path + "]";
            throw new NestableIOException(errmsg, iae);
        }
    }

    /**
     * Loads the image from the given resource set using the specified
     * path. Does no caching and does not convert the image for optimized
     * display on the target graphics configuration. Instead the original
     * image as returned by the image loader is returned.
     */
    public Image loadImage (String rset, String path)
        throws IOException
    {
        InputStream imgin = _rmgr.getResource(rset, path);

        // load up the image
        try {
            BufferedInputStream bin = new BufferedInputStream(imgin);
            return _loader.loadImage(bin);
        } catch (Throwable t) {
            String errmsg = "Error loading image " +
                "[rset=" + rset + ", path=" + path + "]";
            throw new NestableIOException(errmsg, t);
        }
    }

    /**
     * Loads the image from the supplied input stream. Does no caching and
     * does not convert the image for optimized display on the target
     * graphics configuration. Instead the original image as returned by
     * the image loader is returned.
     */
    public Image loadImage (InputStream source)
        throws IOException
    {
        try {
            return _loader.loadImage(source);
        } catch (IllegalArgumentException iae) {
            String errmsg = "Error loading image";
            throw new NestableIOException(errmsg, iae);
        }
    }

    /**
     * Creates an image that is optimized for rendering in the client's
     * environment and decodes the image data from the specified source
     * image into that target image. The resulting image is not cached.
     */
    public Image createImage (InputStream source)
        throws IOException
    {
        return createImage(loadImage(source));
    }

    /**
     * Creates an image that is optimized for rendering in the client's
     * environment and renders the supplied image into the optimized
     * image.  The resulting image is not cached.
     */
    public Image createImage (Image src)
    {
        // not to freak out if we get this far and have no image
        if (src == null) {
            return null;
        }

        int swidth = src.getWidth(null);
        int sheight = src.getHeight(null);
        BufferedImage dest = null;

        // use the same kind of transparency as the source image if we can
        // when converting the image to a format optimized for display
        if (src instanceof BufferedImage) {
            int trans = ((BufferedImage)src).getColorModel().getTransparency();
            dest = ImageUtil.createImage(swidth, sheight, trans);
        } else {
            dest = ImageUtil.createImage(swidth, sheight);
        }

        Graphics2D gfx = dest.createGraphics();
        gfx.drawImage(src, 0, 0, null);
        gfx.dispose();

        return dest;
    }

    /** A reference to the resource manager via which we load image data
     * by default. */
    protected ResourceManager _rmgr;

    /** The image loader via which we convert an input stream into an
     * image. */
    protected ImageLoader _loader;

    /** A cache of loaded images. */
    protected LRUHashMap _imgs = new LRUHashMap(IMAGE_CACHE_SIZE);

    /** Throttle our cache status logging to once every 30 seconds. */
    protected Throttle _cacheStatThrottle = new Throttle(1, 30000L);

    /** The classname of the ImageIO-based image loader which we attempt
     * to use but fallback from if we're not running a JVM that has
     * ImageIO support. */
    protected static final String IMAGEIO_LOADER =
        "com.threerings.media.ImageIOLoader";

    /** The maximum number of images that may be cached at any one time. */
    protected static final int IMAGE_CACHE_SIZE = 30;
}
