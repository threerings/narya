//
// $Id: CharacterSprite.java,v 1.17 2001/11/01 01:40:42 shaper Exp $

package com.threerings.cast;

import java.awt.Point;

import com.threerings.media.sprite.*;

import com.threerings.cast.Log;

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
     * Sets the action sequences available for this character sprite
     * and the animation frames that go along with each action.
     * Resets the character's currently selected action sequence to
     * the standing sequence.
     */
    public void setAnimations (ActionSequence[] seqs,
                               MultiFrameImage anims[][])
    {
        _seqs = seqs;
        _anims = anims;
        setActionSequence(WALKING);
    }

    /**
     * Sets the action sequence used when rendering the character,
     * from the set of available sequences.
     */
    public void setActionSequence (int seqidx)
    {
        // save off the action sequence index
        _seqidx = seqidx;

        // update the sprite render attributes
        ActionSequence seq = _seqs[_seqidx];
        setFrames(_anims[_seqidx][_orient]);
        setFrameRate(seq.fps);
        setOrigin(seq.origin.x, seq.origin.y);
    }

    // documentation inherited
    public void setOrientation (int orient)
    {
        super.setOrientation(orient);
        // update the sprite frames to reflect the direction
        setActionSequence((_path == null) ? STANDING : WALKING);
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
        setActionSequence(STANDING);
        // disable walking animation
        setAnimationMode(NO_ANIMATION);
    }

    /** The action sequence constant for standing. */ 
    protected static final int STANDING = 0;

    /** The action sequence constant for walking. */ 
    protected static final int WALKING = 1;

    /** The currently selected action sequence. */
    protected int _seqidx;

    /** The available action sequences. */
    protected ActionSequence _seqs[];

    /** The animation frames for each action sequence and orientation. */
    protected MultiFrameImage _anims[][];

    /** The origin of the sprite. */
    protected int _xorigin, _yorigin;
}
