//
// $Id: CharacterSprite.java,v 1.11 2001/10/12 00:41:48 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.media.sprite.*;

import com.threerings.miso.Log;
import com.threerings.miso.tile.MisoTile;

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
    }

    // documentation inherited
    public void setOrientation (int orient)
    {
        super.setOrientation(orient);

        // update the sprite frames to reflect the direction
        setFrames(_anims[_orient]);
    }

    // documentation inherited
    protected void pathBeginning ()
    {
        super.pathBeginning();

        // enable walking animation
        setAnimationMode(TIME_BASED);
    }

    // documentation inherited
    protected void pathCompleted ()
    {
        super.pathCompleted();

        // come to a halt looking settled and at peace
        _frame = _frames.getFrame(_frameIdx = 0);
        invalidate();

        // disable walking animation
        setAnimationMode(NO_ANIMATION);
    }

    // documentation inherited
    public boolean canTraverse (MisoTile tile)
    {
	// by default, passability is solely the province of the tile
	return tile.passable;
    }

    /** The animation frames for the sprite facing each direction. */
    protected MultiFrameImage[] _anims;
}
