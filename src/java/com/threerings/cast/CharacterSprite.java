//
// $Id: CharacterSprite.java,v 1.25 2002/03/16 03:15:04 shaper Exp $

package com.threerings.cast;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.sprite.Path;
import com.threerings.media.sprite.ImageSprite;

/**
 * A character sprite is a sprite that animates itself while walking
 * about in a scene.
 */
public class CharacterSprite extends ImageSprite
    implements StandardActions
{
    /**
     * Initializes this character sprite with the specified character
     * descriptor and character manager. It will obtain animation data
     * from the supplied character manager.
     */
    public void init (CharacterDescriptor descrip, CharacterManager charmgr)
    {
        // keep track of this stuff
        _descrip = descrip;
        _charmgr = charmgr;

        // assign an arbitrary starting orientation
        _orient = NORTH;
    }

    /**
     * Reconfigures this sprite to use the specified character descriptor.
     */
    public void setCharacterDescriptor (CharacterDescriptor descrip)
    {
        // keep the new descriptor
        _descrip = descrip;

        // reset our action which will reload our frames
        setActionSequence(_action);
    }

    /**
     * Specifies the action to use when the sprite is at rest. The default
     * is <code>STANDING</code>.
     */
    public void setRestingAction (String action)
    {
        _restingAction = action;
    }

    /**
     * Returns the action to be used when the sprite is at rest. Derived
     * classes may wish to override this method and vary the action based
     * on external parameters (or randomly).
     */
    public String getRestingAction ()
    {
        return _restingAction;
    }

    /**
     * Specifies the action to use when the sprite is following a path.
     * The default is <code>WALKING</code>.
     */
    public void setFollowingPathAction (String action)
    {
        _followingPathAction = action;
    }

    /**
     * Returns the action to be used when the sprite is following a path.
     * Derived classes may wish to override this method and vary the
     * action based on external parameters (or randomly).
     */
    public String getFollowingPathAction ()
    {
        return _followingPathAction;
    }

    /**
     * Sets the action sequence used when rendering the character, from
     * the set of available sequences.
     */
    public void setActionSequence (String action)
    {
        // keep track of our current action in case someone swaps out our
        // character description
        _action = action;

        // get a reference to the action sequence so that we can obtain
        // our animation frames and configure our frames per second
        ActionSequence actseq = _charmgr.getActionSequence(action);
        if (actseq == null) {
            String errmsg = "No such action '" + action + "'.";
            throw new IllegalArgumentException(errmsg);
        }

        try {
            // obtain our animation frames for this action sequence
            _frames = _charmgr.getActionFrames(_descrip, action);

            // update the sprite render attributes
            setOrigin(actseq.origin.x, actseq.origin.y);
            setFrameRate(actseq.framesPerSecond);
            setFrames(_frames[_orient]);

        } catch (NoSuchComponentException nsce) {
            Log.warning("Character sprite references non-existent " +
                        "component [sprite=" + this + ", err=" + nsce + "].");
        }
    }

    // documentation inherited
    public void setOrientation (int orient)
    {
        super.setOrientation(orient);

        // update the sprite frames to reflect the direction
        if (_frames != null) {
            setFrames(_frames[orient]);
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
        setActionSequence(getFollowingPathAction());
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
        // disable animation
        setAnimationMode(NO_ANIMATION);
        // come to a halt looking settled and at peace
        setActionSequence(getRestingAction());
    }

    /** The action to use when at rest. */
    protected String _restingAction = STANDING;

    /** The action to use when following a path. */
    protected String _followingPathAction = WALKING;

    /** A reference to the descriptor for the character that we're
     * visualizing. */
    protected CharacterDescriptor _descrip;

    /** A reference to the character manager that created us. */
    protected CharacterManager _charmgr;

    /** The action we are currently displaying. */
    protected String _action;

    /** The animation frames for the active action sequence in each
     * orientation. */
    protected MultiFrameImage[] _frames;

    /** The origin of the sprite. */
    protected int _xorigin, _yorigin;
}
