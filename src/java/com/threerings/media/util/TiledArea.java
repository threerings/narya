//
// $Id: TiledArea.java,v 1.1 2004/05/21 07:36:17 mdb Exp $

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
