//
// $Id: ImageManager.java,v 1.1 2001/07/18 21:45:42 shaper Exp $

package com.threerings.media;

import com.threerings.cocktail.miso.Log;

import java.awt.*;
import java.awt.image.*;
import java.util.Hashtable;

/**
 * The ImageManager class provides a single point of access for image
 * retrieval and caching.
 *
 * <p> <b>Note:</b> The ImageManager must be initialized with a root
 * AWT component before images can be retrieved, in the interest of
 * allowing for proper preparation of images for optimal storage and
 * eventual display.
 */
public class ImageManager
{
    public static Toolkit tk = Toolkit.getDefaultToolkit();

    /**
     * Initialize the ImageManager with a root component to which
     * images will later be rendered.
     */
    public static void init (Component root)
    {
	_root = root;
	tk = root.getToolkit();
    }

    /**
     * Load the image from the specified filename and cache it for
     * faster retrieval henceforth.
     */
    public static Image getImage (String fname)
    {
	if (_root == null) {
	    Log.warning("Attempt to get image without valid root component.");
	    return null;
	}

	// TODO: fix this to properly find the file within the
	// classpath.  getResourceAsStream() returns an InputStream,
	// but java.awt.Toolkit.createImage() can only take one of a
	// byte[], String, URL, or ImageProducer.
	fname = "/home/shaper/workspace/cocktail/" + fname;

	Image img = (Image)_imgs.get(fname);
	if (img != null) {
	    Log.info("Retrieved image from cache [fname=" + fname + "].");
	    return img;
	}

	Log.info("Loading image into cache [fname=" + fname + "].");

	img = tk.createImage(fname);
	MediaTracker tracker = new MediaTracker(_root);
	tracker.addImage(img, 0);

	try {
	    tracker.waitForID(0);
	    if (tracker.isErrorAny()) {
		Log.warning("Error loading image [fname=" + fname + "].");
		return null;

	    } else {
		_imgs.put(fname, img);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return img;
    }

    /**
     * Creates a new image representing the specified rectangular
     * section cropped from the specified full image object.
     */
    public static BufferedImage getImageCropped (Image fullImg, int x, int y,
						 int width, int height)
    {
	BufferedImage img =
	    new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

	Graphics2D g2 = img.createGraphics();
	g2.drawImage(fullImg, -x, -y, null);

	return img;
    }

    protected static Component _root;
    protected static Hashtable _imgs = new Hashtable();
}
