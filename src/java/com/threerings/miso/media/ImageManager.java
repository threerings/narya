//
// $Id: ImageManager.java,v 1.2 2001/07/14 00:02:10 shaper Exp $

package com.threerings.cocktail.miso.media;

import com.threerings.cocktail.miso.Log;

import java.awt.*;
import java.awt.image.*;
import java.util.Hashtable;

public class ImageManager
{
    public static Toolkit tk = Toolkit.getDefaultToolkit();

    public static void init (Component root)
    {
	_root = root;
	tk = root.getToolkit();
    }

    public static Image getImage (String fname)
    {
	if (_root == null) {
	    Log.warning("Attempt to get image without valid root component.");
	    return null;
	}

	Image img = (Image) _imgs.get(fname);
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
		Log.info("Loaded image into cache successfully.");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return img;
    }

    public static BufferedImage getImageCropped (Image fullImg, int x, int y,
						 int width, int height)
    {
	BufferedImage img =
	    new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

	Graphics2D g2 = img.createGraphics();
	g2.drawImage(fullImg, -x, -y, null);

	return img;
    }

    protected static Component _root;
    protected static Hashtable _imgs = new Hashtable();
}
