//
// $Id: TileUtil.java,v 1.3 2001/08/16 23:14:21 mdb Exp $

package com.threerings.miso.tile;

import java.awt.Image;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.sprite.Path;
import com.threerings.media.tile.*;

import com.threerings.miso.scene.AmbulatorySprite;

/**
 * Tile-related utility functions.
 */ 
public class TileUtil
{
    /**
     * Returns an array of multi-frame images corresponding to the frames
     * of animation used to render the mobile sprite in each of the
     * directions it may face.  The tileset id referenced must contain
     * <code>Path.NUM_DIRECTIONS</code> rows of tiles, with each row
     * containing <code>NUM_DIR_FRAMES</code> tiles.
     *
     * @param tilemgr the tile manager to retrieve tiles from.
     * @param tsid the tileset id containing the sprite tiles.
     *
     * @return the array of multi-frame sprite images.
     */
    public static MultiFrameImage[] getSpriteFrames (
        TileManager tilemgr, int tsid)
    {
        MultiFrameImage[] anims = new MultiFrameImage[Path.NUM_DIRECTIONS];

        for (int ii = 0; ii < Path.NUM_DIRECTIONS; ii++) {
            Tile[] tiles = new Tile[NUM_DIR_FRAMES];
            for (int jj = 0; jj < NUM_DIR_FRAMES; jj++) {
                int idx = (ii * NUM_DIR_FRAMES) + jj;
                tiles[jj] = tilemgr.getTile(tsid, idx);
            }
            anims[ii] = new MultiTileImage(tiles);
        }

        return anims;
    }

    /**
     * A class that treats an array of tiles as source images for a
     * multi-frame animation to be used by the sprite engine.
     */
    protected static class MultiTileImage implements MultiFrameImage
    {
        public MultiTileImage (Tile[] tiles)
        {
            _tiles = tiles;
        }

        public int getFrameCount ()
        {
            return _tiles.length;
        }

        public Image getFrame (int index)
        {
            return _tiles[index].img;
        }

        protected Tile[] _tiles;
    }

    /** The number of frames of animation for each direction. */
    protected static final int NUM_DIR_FRAMES = 8;
}
