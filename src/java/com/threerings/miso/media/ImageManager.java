//
// $Id: ImageManager.java,v 1.1 2001/07/12 22:38:03 shaper Exp $

package com.threerings.cocktail.miso.media;

import java.awt.*;
import java.awt.image.ImageObserver;

public class ImageManager
{
    public static Image getImage (String fname, ImageObserver obs)
    {
	Image img = tk.getImage(fname);
	tk.prepareImage(img, -1, -1, obs);
	return img;
    }

    public static Toolkit tk = Toolkit.getDefaultToolkit();
}
