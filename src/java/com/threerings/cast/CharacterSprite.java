//
// $Id: CharacterSprite.java,v 1.13 2001/10/22 18:21:41 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.media.sprite.*;

import com.threerings.miso.Log;
import com.threerings.miso.tile.MisoTile;

/**
 * An ambulatory sprite is a sprite that animates itself while walking
 * about in a scene.
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

    /**
     * Sets the origin coordinates representing the "base" of the
     * sprite, which in most cases corresponds to the center of the
     * bottom of the sprite image.
     */
    public void setOrigin (int x, int y)
    {
        _xorigin = x;
        _yorigin = y;

        updateRenderOffset();
        updateRenderOrigin();
    }

    // documentation inherited
    protected void updateRenderOffset ()
    {
        super.updateRenderOffset();

        if (_frame != null) {
            // our location is based on the character origin coordinates
            _rxoff = -_xorigin;
            _ryoff = -_yorigin;
        }
    }

    // documentation inherited
    public void cancelMove ()
    {
        super.cancelMove();
        halt();
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
        halt();
    }

    /**
     * Updates the sprite animation frame to reflect the cessation of
     * movement and disables any further animation.
     */
    protected void halt ()
    {
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

    /** The origin of the sprite. */
    protected int _xorigin, _yorigin;
}
