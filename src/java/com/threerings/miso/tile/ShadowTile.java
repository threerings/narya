//
// $Id: ShadowTile.java,v 1.1 2001/10/13 01:08:59 shaper Exp $

package com.threerings.miso.tile;

import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * The shadow tile extends miso tile to provide an always-impassable
 * tile that has no display image.  Shadow tiles are intended for
 * placement in the footprint of {@link ObjectTile} objects.
 */
public class ShadowTile extends MisoTile
{
    /** Single instantiation of shadow tile for re-use in scenes. */
    public static ShadowTile TILE = new ShadowTile();

    /**
     * Constructs a shadow tile.
     */
    public ShadowTile ()
    {
        super(SHADOW_TSID, SHADOW_TID);

        // shadow tiles are always impassable
        passable = false;
    }

    // documentation inherited
    public void paint (Graphics2D gfx, Shape dest)
    {
        // paint nothing as we're naught but a measly shadow of a tile
    }

    /** The shadow tile set id. */
    protected static final int SHADOW_TSID = -1;

    /** The shadow tile id. */
    protected static final int SHADOW_TID = -1;
}
