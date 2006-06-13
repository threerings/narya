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

package com.threerings.media.util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * A component that can be inserted into a user interface to fill a
 * particular area using a {@link BackgroundTiler}.
 */
public class TiledArea extends JComponent
{
    public TiledArea (BufferedImage imgsrc)
    {
        this(new BackgroundTiler(imgsrc));
    }

    public TiledArea (BackgroundTiler tiler)
    {
        _tiler = tiler;
        setOpaque(true);
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);
        _tiler.paint(g, 0, 0, getWidth(), getHeight());
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        return new Dimension(_tiler.getNaturalWidth(),
                             _tiler.getNaturalHeight());
    }

    protected BackgroundTiler _tiler;
}
