//
// $Id: CharacterSprite.java,v 1.9 2001/09/13 19:10:26 mdb Exp $

package com.threerings.miso.scene;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.Tile;

import com.threerings.miso.Log;

/**
 * An <code>AmbulatorySprite</code> is a sprite that can face in one of
 * the various compass directions and that can animate itself walking
 * along some chosen path.
 */
public class AmbulatorySprite extends Sprite implements Traverser
{
    /**
     * Construct an <code>AmbulatorySprite</code>, with a multi-frame
     * image associated with each of the eight compass directions. The
     * array should be in the order defined by the <code>Sprite</code>
     * direction constants (SW, W, NW, N, NE, E, SE, S).
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     * @param anims the set of multi-frame images to use when animating
     * the sprite in each of the compass directions.
     */
    public AmbulatorySprite (int x, int y, MultiFrameImage[] anims)
    {
        super(x, y);

        // keep track of these
        _anims = anims;

        // give ourselves an initial orientation
        setOrientation(DIR_NORTH);

        // we only animate when we're moving
        setAnimationMode(MOVEMENT_CUED);
    }

    public void setOrientation (int orient)
    {
        super.setOrientation(orient);

        // update the sprite frames to reflect the direction
        setFrames(_anims[_orient]);
    }

    public boolean canTraverse (Tile tile)
    {
	// by default, passability is solely the province of the tile
	return tile.passable;
    }

    /** The animation frames for the sprite facing each direction. */
    protected MultiFrameImage[] _anims;
}
