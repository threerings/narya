//
// $Id: ImageManager.java,v 1.7 2001/11/18 04:09:21 mdb Exp $

package com.threerings.media;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

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
        img = ImageIO.read(_rmgr.getResource(path));
        _imgs.put(path, img);
        return img;
    }

    /** A reference to the resource manager via which we load image data
     * by default. */
    protected ResourceManager _rmgr;

    /** A cache of loaded images. */
    protected HashMap _imgs = new HashMap();
}
