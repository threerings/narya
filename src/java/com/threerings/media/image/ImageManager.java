//
// $Id: ImageManager.java,v 1.4 2001/09/06 01:35:05 shaper Exp $

package com.threerings.media;

import com.threerings.miso.Log;
import com.threerings.resource.ResourceManager;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.Hashtable;

/**
 * The ImageManager class provides a single point of access for image
 * retrieval and caching.
 *
 * <p> <b>Note:</b> The ImageManager must be constructed with a root
 * AWT component, in the interest of allowing for proper preparation
 * of images for optimal storage and eventual display.
 */
public class ImageManager
{
    /**
     * Construct an ImageManager object with the ResourceManager from
     * which it will obtain its data, and a root component to which
     * images will be rendered.
     */
    public ImageManager (ResourceManager rmgr, Component root)
    {
	_rmgr = rmgr;
	_root = root;
	_tk = root.getToolkit();
    }

    /**
     * Load the image from the specified filename and cache it for
     * faster retrieval henceforth.
     */
    public Image getImage (String fname)
    {
	if (_root == null) {
	    Log.warning("Attempt to get image without valid root component.");
	    return null;
	}

	Image img = (Image)_imgs.get(fname);
	if (img != null) {
	    Log.info("Retrieved image from cache [fname=" + fname + "].");
	    return img;
	}

	Log.info("Loading image into cache [fname=" + fname + "].");

	try {
	    byte[] data = _rmgr.getResourceAsBytes(fname);
	    img = _tk.createImage(data);
	    MediaTracker tracker = new MediaTracker(_root);
	    tracker.addImage(img, 0);

	    tracker.waitForID(0);
	    if (tracker.isErrorAny()) {
		Log.warning("Error loading image [fname=" + fname + "].");
		return null;

	    } else {
		_imgs.put(fname, img);
	    }

	} catch (IOException ioe) {
	    Log.warning("Exception loading image [ioe=" + ioe + "].");

	} catch (InterruptedException ie) {
	    Log.warning("Interrupted loading image [ie=" + ie + "].");
	}

	return img;
    }

    /**
     * Creates a new image representing the specified rectangular
     * section cropped from the specified full image object.
     */
    public Image getImageCropped (Image fullImg, int x, int y,
				  int width, int height)
    {
	BufferedImage img =
	    new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

	Graphics2D gfx = img.createGraphics();
	gfx.drawImage(fullImg, -x, -y, null);

	return img;
    }

    protected ResourceManager _rmgr;
    protected Component _root;
    protected Hashtable _imgs = new Hashtable();
    protected Toolkit _tk = Toolkit.getDefaultToolkit();
}
