//
// $Id: BufferedMirage.java,v 1.3 2004/08/27 02:12:38 mdb Exp $
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
