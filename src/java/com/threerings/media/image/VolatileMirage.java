//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.samskivert.util.StringUtil;

/**
 * A mirage implementation which allows the image to be maintained in
 * video memory and rebuilt from some source image or images in the event
 * that our target screen resolution changes or we are flushed from video
 * memory for some other reason.
 */
public abstract class VolatileMirage implements Mirage
{
    /**
     * Informs the base class of its image manager and image bounds.
     */
    protected VolatileMirage (ImageManager imgr, Rectangle bounds)
    {
        _imgr = imgr;
        _bounds = bounds;
    }

    // documentation inherited from interface
    public void paint (Graphics2D gfx, int x, int y)
    {
        // create our volatile image for the first time if necessary
        if (_image == null) {
            createVolatileImage();
        }

//         int renders = 0;
//         do {
//             // validate that our image is compatible with the target GC
//             switch (_image.validate(_imgr.getGraphicsConfiguration())) {
//             case VolatileImage.IMAGE_RESTORED:
//                 refreshVolatileImage(); // need to rerender it
//                 break;
//             case VolatileImage.IMAGE_INCOMPATIBLE:
//                 createVolatileImage(); // need to recreate it
//                 break;
//             }

//             // now we can render it
//             gfx.drawImage(_image, x, y, null);
//             renders++;

//             // don't try forever
//         } while (_image.contentsLost() && (renders < 10));

        if (IMAGE_DEBUG) {
            gfx.setColor(new Color(_image.getRGB(_bounds.width/2,
                                                 _bounds.height/2)));
            gfx.fillRect(x, y, _bounds.width, _bounds.height);
        } else {
            gfx.drawImage(_image, x, y, null);
        }

        // TODO: note number of attempted renders for performance
    }

    /**
     * Returns the x offset into our source image, which is generally zero but
     * may be non-zero for a mirage that obtains its data from a region of its
     * source image.
     */
    public int getX ()
    {
        return _bounds.x;
    }

    /**
     * Returns the y offset into our source image, which is generally zero but
     * may be non-zero for a mirage that obtains its data from a region of its
     * source image.
     */
    public int getY ()
    {
        return _bounds.y;
    }

    // documentation inherited from interface
    public int getWidth ()
    {
        return _bounds.width;
    }

    // documentation inherited from interface
    public int getHeight ()
    {
        return _bounds.height;
    }

    // documentation inherited from interface
    public boolean hitTest (int x, int y)
    {
//         return ImageUtil.hitTest(_image.getSnapshot(), x, y);
        return ImageUtil.hitTest(_image, x, y);
    }

    // documentation inherited from interface
    public long getEstimatedMemoryUsage ()
    {
        return ImageUtil.getEstimatedMemoryUsage(_image.getRaster());
    }

    // documentation inherited from interface
    public BufferedImage getSnapshot ()
    {
//         return _image.getSnapshot();
        return _image;
    }

    /**
     * Creates our volatile image from the information in our source
     * image.
     */
    protected void createVolatileImage ()
    {
        // release any previous volatile image we might hold
        if (_image != null) {
            _image.flush();
        }

        // create a new, compatible, volatile image
//         _image = _imgr.createVolatileImage(
//             _bounds.width, _bounds.height, getTransparency());
        _image = _imgr.createImage(
            _bounds.width, _bounds.height, getTransparency());

        // render our source image into the volatile image
        refreshVolatileImage();
    }

    /**
     * Returns the transparency that should be used when creating our
     * volatile image.
     */
    protected abstract int getTransparency ();

    /**
     * Rerenders our volatile image from the its source image data.
     */
    protected abstract void refreshVolatileImage ();

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuilder buf = new StringBuilder("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * Generates a string representation of this instance.
     */
    protected void toString (StringBuilder buf)
    {
        buf.append("bounds=").append(StringUtil.toString(_bounds));
    }

    /** The image manager with whom we interoperate. */
    protected ImageManager _imgr;

    /** The bounds of the region of our source image which we desire for
     * this mirage (possibly the whole thing). */
    protected Rectangle _bounds;

    /** Our volatile image which lives in video memory and can go away at
     * any time. */
//     protected VolatileImage _image;
    protected BufferedImage _image;

    /** Turns off image rendering for testing. */
    protected static final boolean IMAGE_DEBUG = false;
}
