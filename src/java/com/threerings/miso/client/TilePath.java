//
// $Id: TilePath.java,v 1.6 2002/06/12 07:01:29 ray Exp $

package com.threerings.miso.scene;

import java.awt.*;
import java.util.List;

import com.threerings.util.DirectionCodes;

import com.threerings.media.util.LineSegmentPath;
import com.threerings.media.util.PathNode;
import com.threerings.media.util.Pathable;

import com.threerings.miso.Log;
import com.threerings.miso.scene.util.IsoUtil;

/**
 * The tile path represents a path of tiles through a scene.  The path is
 * traversed by treating each pair of connected tiles as a line segment.
 * Only ambulatory sprites can follow a tile path, and their tile
 * coordinates are updated as the path is traversed.
 */
public class TilePath extends LineSegmentPath
    implements DirectionCodes
{
    /**
     * Constructs a tile path.
     *
     * @param model the iso scene view model the path is associated with.
     * @param sprite the sprite to follow the path.
     * @param tiles the tiles to be traversed during the path.
     * @param destx the destination x-position in screen pixel
     * coordinates.
     * @param desty the destination y-position in screen pixel
     * coordinates.
     */
    public TilePath (
        IsoSceneViewModel model, MisoCharacterSprite sprite, List tiles,
        int destx, int desty)
    {
        _model = model;

        // set up the path nodes
        createPath(sprite, tiles, destx, desty);
    }

    // documentation inherited
    public boolean tick (Pathable pable, long timestamp)
    {
        boolean moved = super.tick(pable, timestamp);

        if (moved && (_dest instanceof TilePathNode)) {
            TilePathNode dest = (TilePathNode) _dest;

            MisoCharacterSprite mcs = (MisoCharacterSprite)pable;
            int sx = mcs.getX(), sy = mcs.getY();
            Point pos = new Point();

            // check whether we've arrived at the destination tile
            if (!_arrived) {
                // get the sprite's latest tile coordinates
                IsoUtil.screenToTile(_model, sx, sy, pos);

                // if the sprite has reached the destination tile,
                // update the sprite's tile location and remember
                // we've arrived
                int dtx = dest.getTileX(), dty = dest.getTileY();
                if (pos.x == dtx && pos.y == dty) {
                    mcs.setTileLocation(dtx, dty);
                    // Log.info("Sprite arrived [dtx=" + dtx +
                    // ", dty=" + dty + "].");
                    _arrived = true;
                }
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

    // documentation inherited
    protected PathNode getNextNode ()
    {
        // upgrade the path node to a tile path node
        _dest = super.getNextNode();

        // note that we've not yet arrived at the destination tile 
        _arrived = false;

        return _dest;
    }

    /**
     * Populate the path with the tile path nodes that lead the sprite
     * from its starting position to the given destination coordinates
     * following the given list of tile coordinates.
     */
    protected void createPath (
        MisoCharacterSprite sprite, List tiles, int destx, int desty)
    {
	// constrain destination pixels to fine coordinates
	Point fpos = new Point();
	IsoUtil.screenToFull(_model, destx, desty, fpos);

        // add the starting path node
        int stx = sprite.getTileX(), sty = sprite.getTileY();
        int sx = sprite.getX(), sy = sprite.getY();
        addNode(stx, sty, sx, sy, NORTH);

	// TODO: make more visually appealing path segments from start
	// to second tile, and penultimate to ultimate tile.

        // add all remaining path nodes excepting the last one
        Point prev = new Point(stx, sty);
        Point spos = new Point();
        int size = tiles.size();
        for (int ii = 1; ii < size - 1; ii++) {
            Point next = (Point)tiles.get(ii);

            // determine the direction from previous to next node
            int dir = IsoUtil.getIsoDirection(prev.x, prev.y, next.x, next.y);

            // determine the node's position in screen pixel coordinates
            IsoUtil.tileToScreen(_model, next.x, next.y, spos);

            // add the node to the path, wandering through the middle
            // of each tile in the path for now
            int dsx = spos.x + _model.tilehwid;
            int dsy = spos.y + _model.tilehhei;
            addNode(next.x, next.y, dsx, dsy, dir);

            prev = next;
        }

        // get the final destination point's screen coordinates
        // constrained to the closest full coordinate
        IsoUtil.fullToScreen(_model, fpos.x, fpos.y, spos);

        // get the tile coordinates for the final destination tile
        int tdestx = IsoUtil.fullToTile(fpos.x);
        int tdesty = IsoUtil.fullToTile(fpos.y);

        // get the facing direction for the final node
        int dir;
        if (prev.x == stx && prev.y == sty) {
            // if destination is within starting tile, direction is
            // determined by studying the fine coordinates
            dir = IsoUtil.getDirection(_model, sx, sy, spos.x, spos.y);

        } else {
            // else it's based on the last tile we traversed
            dir = IsoUtil.getIsoDirection(prev.x, prev.y, tdestx, tdesty);
        }

    	// add the final destination path node
	addNode(tdestx, tdesty, spos.x, spos.y, dir);
    }

    /**
     * Add a node to the path with the specified destination
     * coordinates and facing direction.
     *
     * @param tx the tile x-position.
     * @param ty the tile y-position.
     * @param x the x-position.
     * @param y the y-position.
     * @param dir the facing direction.
     */
    protected void addNode (int tx, int ty, int x, int y, int dir)
    {
        _nodes.add(new TilePathNode(tx, ty, x, y, dir));
    }

    /** Whether the sprite has arrived at the current destination tile. */
    protected boolean _arrived;

    /** The destination tile path node. */
    protected PathNode _dest;

    /** The iso scene view model. */
    protected IsoSceneViewModel _model;
}
