//
// $Id: ScreenTilePath.java,v 1.1 2002/06/18 21:22:34 ray Exp $

package com.threerings.miso.scene;

import java.awt.Point;
import java.util.List;

import com.threerings.media.util.LineSegmentPath;
import com.threerings.media.util.Pathable;

import com.threerings.miso.Log;
import com.threerings.miso.scene.util.IsoUtil;

/**
 * A line segment path that attempts to update the sprite's tile coordinates
 * as it walks it along.
 */
public class ScreenTilePath extends LineSegmentPath
{
    /**
     * Constructs a screen tile path.
     *
     * @param model the iso scene view model the path is associated with.
     */
    public ScreenTilePath (IsoSceneViewModel model)
    {
        _model = model;
    }

    // documentation inherited
    public boolean tick (Pathable pable, long timestamp)
    {
        boolean moved = super.tick(pable, timestamp);

        if (moved) {
            MisoCharacterSprite mcs = (MisoCharacterSprite)pable;
            int sx = mcs.getX(), sy = mcs.getY();
            Point pos = new Point();

            // check whether we've arrived at the destination tile
            if (shouldComputeTileCoords()) {
                // get the sprite's latest tile coordinates
                IsoUtil.screenToTile(_model, sx, sy, pos);

                setTileCoords(mcs, pos);
            }

            // get the sprite's latest fine coordinates
            IsoUtil.tileToScreen(_model, mcs.getTileX(), mcs.getTileY(), pos);
            Point fpos = new Point();
            IsoUtil.pixelToFine(_model, sx - pos.x, sy - pos.y, fpos);

            // inform the sprite
            mcs.setFineLocation(fpos.x, fpos.y);

            // Log.info("Sprite moved [s=" + mcs + "].");
        }

        return moved;
    }

    /**
     * Should we compute and attempt to set the tile coordinates during
     * this tick?
     */
    protected boolean shouldComputeTileCoords ()
    {
        return true;
    }

    /**
     * Set the tile coordinates.
     */
    protected void setTileCoords (MisoCharacterSprite mcs, Point pos)
    {
        mcs.setTileLocation(pos.x, pos.y);
    }

    /** The iso scene view model. */
    protected IsoSceneViewModel _model;
}
