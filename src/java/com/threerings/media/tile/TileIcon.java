//
// $Id: TileIcon.java,v 1.2 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * Implements the icon interface, using a {@link Tile} to render the icon
 * image.
 */
public class TileIcon implements Icon
{
    /**
     * Creates a tile icon that will use the supplied tile to render itself.
     */
    public TileIcon (Tile tile)
    {
        _tile = tile;
    }

    // documentation inherited from interface
    public void paintIcon (Component c, Graphics g, int x, int y)
    {
        _tile.paint((Graphics2D)g, x, y);
    }
    
    // documentation inherited from interface
    public int getIconWidth ()
    {
        return _tile.getWidth();
    }

    // documentation inherited from interface
    public int getIconHeight ()
    {
        return _tile.getHeight();
    }

    /** The tile used to render this icon. */
    protected Tile _tile;
}
