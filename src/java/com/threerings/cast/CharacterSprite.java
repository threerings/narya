//
// $Id: CharacterSprite.java,v 1.3 2001/08/14 22:54:45 mdb Exp $

package com.threerings.media.sprite;

import com.threerings.media.Log;
import com.threerings.miso.tile.Tile;
import com.threerings.miso.tile.TileManager;

/**
 * An <code>AmbulatorySprite</code> is a sprite that can face in one
 * of the various compass directions and that can animate itself
 * walking along some chosen path.
 */
public class AmbulatorySprite extends Sprite
{
    /**
     * Construct an <code>AmbulatorySprite</code>, loading the tiles
     * used to display the sprite from the given tileset.
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     * @param tilemgr the tile manager to retrieve tiles from.
     * @param tsid the tileset id containing the sprite tiles.
     */
    public AmbulatorySprite (SpriteManager spritemgr, int x, int y,
                             TileManager tilemgr, int tsid)
    {
        super(spritemgr, x, y);

        _dirTiles = getTiles(tilemgr, tsid);
        _dir = Path.DIR_SOUTH;

        setTiles(_dirTiles[0]);
    }

    /**
     * Returns a two-dimensional array of tiles corresponding to the
     * frames of animation used to render the mobile sprite in each of
     * the directions it may face.  The tileset id referenced must
     * contain <code>Path.NUM_DIRECTIONS</code> rows of tiles, with each
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
            new Tile[Path.NUM_DIRECTIONS][NUM_DIR_FRAMES];

        for (int ii = 0; ii < Path.NUM_DIRECTIONS; ii++) {
            for (int jj = 0; jj < NUM_DIR_FRAMES; jj++) {
                int idx = (ii * NUM_DIR_FRAMES) + jj;
                tiles[ii][jj] = tilemgr.getTile(tsid, idx);
            }
        }

        return tiles;
    }

    /**
     * Alter the sprite's direction to reflect the direction the
     * destination point lies in before calling the superclass's
     * <code>setDestination</code> method.
     *
     * @param x the destination x-position.
     * @param y the destination y-position.
     */
    protected void moveAlongPath ()
    {
        // select the new path node
        super.moveAlongPath();

        // bail if we're at the end of the path
        if (_dest == null) {
            // stop any walking animation
            setAnimationDelay(ANIM_NONE);
            return;
        }

        // update the sprite tiles to reflect the direction
        setTiles(_dirTiles[_dir = _dest.dir]);

        // start tile animation to show movement
        setAnimationDelay(0);
    }

    /** The number of frames of animation for each direction. */
    protected static final int NUM_DIR_FRAMES = 8;

    /** The animation frames for the sprite facing each direction. */
    protected Tile[][] _dirTiles;

    /** The direction the sprite is currently facing. */
    protected int _dir;
}
