//
// $Id: ObjectTileSet.java,v 1.1 2001/11/08 03:04:44 mdb Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.Point;

import com.samskivert.util.HashIntMap;

import com.threerings.media.Log;
import com.threerings.media.ImageManager;

/**
 * The objcet tileset supports the specification of object information for
 * object tiles in addition to all of the features of the swiss army
 * tileset.
 */
public class ObjectTileSet extends SwissArmyTileSet
{
    /**
     * Constructs a tileset with all of the swiss army configuration
     * parameters, with the addition of object information for those tiles
     * in the set that are object tiles.
     *
     * @param objects object information for those tiles that are objects.
     *
     * @see SwissArmyTileSet#SwissArmyTileSet
     */
    public ObjectTileSet (
        ImageManager imgmgr, String imgPath, String name, int tsid,
        int[] tileCount, int[] rowWidth, int[] rowHeight,
        Point offsetPos, Point gapDist, HashIntMap objects)
    {
        super(imgmgr, imgPath, name, tsid, tileCount, rowWidth, rowHeight,
              offsetPos, gapDist);

        // keep this for later
        _objects = objects;
    }

    /**
     * Creates instances of {@link ObjectTile} which can span more than a
     * single tile's space in a display.
     */
    protected Tile createTile (int tileId)
    {
        // default object dimensions to (1, 1)
        int wid = 1, hei = 1;

        // retrieve object dimensions if known
        if (_objects != null) {
            int size[] = (int[])_objects.get(tileId);
            if (size != null) {
                wid = size[0];
                hei = size[1];
            }
        }

        return new ObjectTile(_tsid, tileId, wid, hei);
    }

    /** Mapping of object tile ids to object dimensions. */
    protected HashIntMap _objects;
}
