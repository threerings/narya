//
// $Id: BufferedMirage.java,v 1.2 2003/01/17 02:30:21 mdb Exp $

package com.threerings.media.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A simple mirage implementation that uses a buffered image.
 */
public class BufferedMirage implements Mirage
{
    public BufferedMirage (BufferedImage image)
    {
        _image = image;
    }

    // documentation inherited from interface
    public void paint (Graphics2D gfx, int x, int y)
    {
        gfx.drawImage(_image, x, y, null);
    }

    // documentation inherited from interface
    public int getWidth ()
    {
        return _image.getWidth();
    }

    // documentation inherited from interface
    public int getHeight ()
    {
        return _image.getHeight();
    }

    // documentation inherited from interface
    public boolean hitTest (int x, int y)
    {
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
        return _image;
    }

    protected BufferedImage _image;
}
