//
// $Id: MobileSprite.java,v 1.1 2001/07/30 15:38:52 shaper Exp $

package com.threerings.miso.sprite;

import com.threerings.miso.tile.Tile;
import com.threerings.miso.tile.TileManager;

/**
 * A MobileSprite is a sprite that can face in one of eight compass
 * directions and that can be animated moving from one location to
 * another (e.g., a human's legs move and arms swing.)
 */
public class MobileSprite extends Sprite
{
    /**
     * Construct a MobileSprite object, loading the tiles used to
     * display the sprite from specified tileset via the given tile
     * manager.
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     * @param tilemgr the tile manager to retrieve tiles from.
     * @param tsid the tileset id containing the sprite tiles.
     */
    public MobileSprite (int x, int y, TileManager tilemgr, int tsid)
    {
        super(x, y);
        _charTiles = getTiles(tilemgr, tsid);
        _dir = DIR_SOUTH;
        setTiles(_charTiles[0]);
    }

    /**
     * Returns a two-dimensional array of tiles corresponding to the
     * frames of animation used to render the mobile sprite in each of
     * the directions it may face.  The tileset id referenced must
     * contain <code>NUM_DIRECTIONS</code> rows of tiles, with each
     * row containing <code>NUM_DIR_FRAMES</code> tiles.
     *
     * @param tilemgr the tile manager to retrieve tiles from.
     * @param tsid the tileset id containing the sprite tiles.
     *
     * @return the two-dimensional array of sprite tiles.
     */
    protected Tile[][] getTiles (TileManager tilemgr, int tsid)
    {
        Tile[][] tiles =
            new Tile[NUM_DIRECTIONS][NUM_DIR_FRAMES];

        for (int ii = 0; ii < NUM_DIRECTIONS; ii++) {
            for (int jj = 0; jj < NUM_DIR_FRAMES; jj++) {
                int idx = (ii * NUM_DIR_FRAMES) + jj;
                tiles[ii][jj] = tilemgr.getTile(tsid, idx);
            }
        }

        return tiles;
    }

    /** The number of distinct directions the character may face. */
    protected static final int NUM_DIRECTIONS = 8;

    // Direction constants
    protected static final int DIR_SOUTH = 0;
    protected static final int DIR_SOUTHWEST = 1;
    protected static final int DIR_WEST = 2;
    protected static final int DIR_NORTHWEST = 3;
    protected static final int DIR_NORTH = 4;
    protected static final int DIR_NORTHEAST = 5;
    protected static final int DIR_EAST = 6;
    protected static final int DIR_SOUTHEAST = 7;

    /** The number of frames of animation for each direction. */
    protected static final int NUM_DIR_FRAMES = 8;

    /** The animation frames for the sprite facing each direction. */
    protected Tile[][] _charTiles;

    /** The direction the sprite is currently facing. */
    protected int _dir;
}
