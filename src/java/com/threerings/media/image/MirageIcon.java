//
// $Id: MirageIcon.java,v 1.1 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.image;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * Implements the Swing {@link Icon} interface with a mirage providing the
 * image information.
 */
public class MirageIcon implements Icon
{
    public MirageIcon (Mirage mirage)
    {
        _mirage = mirage;
    }

    // documentation inherited from interface
    public void paintIcon (Component c, Graphics g, int x, int y)
    {
        _mirage.paint((Graphics2D)g, x, y);
    }
    
    // documentation inherited from interface
    public int getIconWidth()
    {
        return _mirage.getWidth();
    }

    // documentation inherited from interface
    public int getIconHeight()
    {
        return _mirage.getHeight();
    }

    protected Mirage _mirage;
}
