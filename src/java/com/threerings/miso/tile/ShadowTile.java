//
// $Id: ShadowTile.java,v 1.3 2001/11/18 04:09:22 mdb Exp $

package com.threerings.miso.tile;

import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * The shadow tile extends miso tile to provide an always-impassable tile
 * that has no display image.  Shadow tiles are intended for placement in
 * the footprint of {@link com.threerings.media.tile.ObjectTile} objects.
 */
public class ShadowTile extends MisoTile
{
    /** The scene coordinates of the shadow tile's parent object tile. */
    public int ox, oy;

    /**
     * Constructs a shadow tile.
     */
    public ShadowTile (int x, int y)
    {
        super(null);

        // save the coordinates of our parent object tile
        ox = x;
        oy = y;

        // shadow tiles are always impassable
        _passable = false;
    }

    // documentation inherited
    public void paint (Graphics2D gfx, Shape dest)
    {
        // paint nothing as we're naught but a measly shadow of a tile
    }
}
