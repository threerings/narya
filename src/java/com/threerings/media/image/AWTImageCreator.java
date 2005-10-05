//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.media.image;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * If the user of the image manager services intends to create and display
 * images using the AWT, they can use this image creator which will use the
 * AWT to determine the optimal image format.
 */
public class AWTImageCreator
    implements ImageManager.OptimalImageCreator
{
     /**
      * Create an image creator that will rely on the AWT to determine the
      * optimal image format.
      *
      * @param context if non-null, the graphics configuration will be obtained
      * therefrom; otherwise {@link GraphicsDevice#getDefaultConfiguration}
      * will be used.
      */
    public AWTImageCreator (Component context)
    {
        // obtain our graphics configuration
        if (context != null) {
            _gc = context.getGraphicsConfiguration();
        } else {
            _gc = ImageUtil.getDefGC();
        }
    }

    // documentation inherited from interface ImageManager.OptimalImageCreator
    public BufferedImage createImage (int width, int height, int trans)
    {
        // DEBUG: override transparency for the moment on all images
        trans = Transparency.TRANSLUCENT;
        if (_gc != null) {
            return _gc.createCompatibleImage(width, height, trans);
        } else {
            // if we're running in headless mode, do everything in 24-bit
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
    }

    /** The graphics configuration for the default screen device. */
    protected GraphicsConfiguration _gc;
}
