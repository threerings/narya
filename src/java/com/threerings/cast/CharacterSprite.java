//
// $Id: CharacterSprite.java,v 1.38 2002/12/04 02:45:08 shaper Exp $

package com.threerings.cast;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.SwingUtilities;

import com.threerings.media.sprite.ImageSprite;
import com.threerings.media.util.Path;

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
        _orient = SOUTHWEST;

        // pass the buck to derived classes
        didInit();
    }

    /**
     * Called after this sprite has been initialized with its character
     * descriptor and character manager. Derived classes can do post-init
     * business here.
     */
    protected void didInit ()
    {
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
            _aframes = _charmgr.getActionFrames(_descrip, action);

            // update the sprite render attributes
            setFrameRate(actseq.framesPerSecond);
            setFrames(_aframes.getFrames(_orient));

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
        if (_aframes != null) {
            setFrames(_aframes.getFrames(orient));
        }
    }

//     // documentation inherited
//     public void paint (Graphics2D gfx)
//     {
//         // DEBUG: draw our origin
//         gfx.setColor(java.awt.Color.white);
//         gfx.drawOval(_x - 3, _y - 3, 6, 6);

//         super.paint(gfx);
//     }

    // documentation inherited
    public boolean hitTest (int x, int y)
    {
        // the irect adjustments are to account for our decorations
        return (_frames != null && _ibounds.contains(x, y) &&
                _frames.hitTest(_frameIdx, x - _ibounds.x, y - _ibounds.y));
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        if (_frames != null) {
            decorateBehind(gfx);
            // paint the image using _ibounds rather than _bounds which
            // has been modified to include the bounds of our decorations
            _frames.paintFrame(gfx, _frameIdx, _ibounds.x, _ibounds.y);
            decorateInFront(gfx);

        } else {
            super.paint(gfx);
        }
    }

    /**
     * Called to paint any decorations that should appear behind the
     * character sprite image.
     */
    protected void decorateBehind (Graphics2D gfx)
    {
    }

    /**
     * Called to paint any decorations that should appear in front of the
     * character sprite image.
     */
    protected void decorateInFront (Graphics2D gfx)
    {
    }

    // documentation inherited
    protected void updateRenderOrigin ()
    {
        super.updateRenderOrigin();

        // adjust our image bounds to reflect the new location
        _ibounds.x = _bounds.x + _ioff.x;
        _ibounds.y = _bounds.y + _ioff.y;
    }

    // documentation inherited
    protected void accomodateFrame (int frameIdx, int width, int height)
    {
        // this will update our width and height
        super.accomodateFrame(frameIdx, width, height);

        // we now need to update the render offset for this frame
        _oxoff = _aframes.getXOrigin(_orient, frameIdx);
        _oyoff = _aframes.getYOrigin(_orient, frameIdx);

        // and cause those changes to be reflected in our bounds
        updateRenderOrigin();

        // start out with our bounds the same as our image bounds
        _ibounds.setBounds(_bounds);

        // now we can call down and incorporate the dimensions of any
        // decorations that will be rendered along with our image
        unionDecorationBounds(_bounds);

        // compute our new render origin
        _oxoff = _ox - _bounds.x;
        _oyoff = _oy - _bounds.y;

        // track the offset from our expanded bounds to our image bounds
        _ioff.x = _ibounds.x - _bounds.x;
        _ioff.y = _ibounds.y - _bounds.y;
    }

    /**
     * Called by {@link #accomodateFrame} to give derived classes an
     * opportunity to incorporate the bounds of any decorations that will
     * be drawn along with this sprite. The {@link #_ibounds} rectangle
     * will contain the bounds of the image that comprises the undecorated
     * sprite. From that the position and size of decorations can be
     * computed and unioned with the supplied bounds rectangle (most
     * likely by using {@link SwingUtilities#computeUnion} which applies
     * the union in place rather than creating a new rectangle).
     */
    protected void unionDecorationBounds (Rectangle bounds)
    {
    }

    // documentation inherited
    public void cancelMove ()
    {
        super.cancelMove();
        halt();
    }

    // documentation inherited
    public void pathBeginning ()
    {
        super.pathBeginning();

        // enable walking animation
        setActionSequence(getFollowingPathAction());
        setAnimationMode(TIME_BASED);
    }

    // documentation inherited
    public void pathCompleted (long timestamp)
    {
        super.pathCompleted(timestamp);
        halt();
    }

    /**
     * Updates the sprite animation frame to reflect the cessation of
     * movement and disables any further animation.
     */
    protected void halt ()
    {
        // only do something if we're actually animating
        if (_animMode != NO_ANIMATION) {
            // disable animation
            setAnimationMode(NO_ANIMATION);
            // come to a halt looking settled and at peace
            String rest = getRestingAction();
            if (rest != null) {
                setActionSequence(rest);
            }
        }
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
    protected ActionFrames _aframes;

    /** The offset from the upper-left of the total sprite bounds to the
     * upper-left of the image within those bounds. */
    protected Point _ioff = new Point();

    /** The bounds of the current sprite image. */
    protected Rectangle _ibounds = new Rectangle();
}
