//
// $Id: CharacterSprite.java,v 1.16 2001/10/26 01:17:21 shaper Exp $

package com.threerings.cast;

import java.awt.Point;

import com.threerings.media.sprite.*;

import com.threerings.cast.Log;
import com.threerings.cast.CharacterComponent.ComponentFrames;

/**
 * A character sprite is a sprite that animates itself while walking
 * about in a scene.
 */
public class CharacterSprite extends Sprite
{
    /**
     * Constructs a character sprite.
     */
    public CharacterSprite ()
    {
        // assign an arbitrary starting orientation
        _orient = DIR_NORTH;
    }

    /**
     * Sets the walking and standing frames of animation used to
     * display this character.
     */
    public void setFrames (ComponentFrames frames)
    {
        _anims = frames;
        setFrames(_anims.walk[_orient]);
    }

    // documentation inherited
    public void setOrientation (int orient)
    {
        super.setOrientation(orient);

        // update the sprite frames to reflect the direction
        if (_path == null) {
            setFrames(_anims.stand[_orient]);
        } else {
            setFrames(_anims.walk[_orient]);
        }
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
        setFrames(_anims.stand[_orient]);

        // disable walking animation
        setAnimationMode(NO_ANIMATION);
    }

    /** The standing and walking animations for the sprite. */
    protected ComponentFrames _anims;

    /** The origin of the sprite. */
    protected int _xorigin, _yorigin;
}
