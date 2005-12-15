//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.cast;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.SwingUtilities;

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

        // sanity check our values
        sanityCheckDescrip();

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

        // sanity check our values
        sanityCheckDescrip();

        // update our action frames
        updateActionFrames();
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
        // sanity check
        if (action == null) {
            Log.warning("Refusing to set null action sequence " + this + ".");
            Thread.dumpStack();
            return;
        }

        // no need to noop
        if (action.equals(_action)) {
            return;
        }
        _action = action;
        updateActionFrames();
    }

    // documentation inherited
    public void setOrientation (int orient)
    {
        if (orient < 0 || orient >= FINE_DIRECTION_COUNT) {
            Log.info("Refusing to set invalid orientation [sprite=" + this +
                     ", orient=" + orient + "].");
            Thread.dumpStack();
            return;
        }

        int oorient = _orient;
        super.setOrientation(orient);
        if (_orient != oorient) {
            _frames = null;
        }
    }

    // documentation inherited
    public boolean hitTest (int x, int y)
    {
        // the irect adjustments are to account for our decorations
        return (_frames != null && _ibounds.contains(x, y) &&
                _frames.hitTest(_frameIdx, x - _ibounds.x, y - _ibounds.y));
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        // composite our action frames if something since the last call to
        // tick caused them to become invalid
        compositeActionFrames();
        super.tick(tickStamp);
        // composite our action frames if something during tick() caused
        // them to become invalid
        compositeActionFrames();
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
        setAnimationMode(TIME_BASED);
        setActionSequence(getFollowingPathAction());
    }

    // documentation inherited
    public void pathCompleted (long timestamp)
    {
        super.pathCompleted(timestamp);
        halt();
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

    /**
     * Rebuilds our action frames given our current character descriptor
     * and action sequence. This is called when either of those two things
     * changes.
     */
    protected void updateActionFrames ()
    {
        // get a reference to the action sequence so that we can obtain
        // our animation frames and configure our frames per second
        ActionSequence actseq = _charmgr.getActionSequence(_action);
        if (actseq == null) {
            String errmsg = "No such action '" + _action + "'.";
            throw new IllegalArgumentException(errmsg);
        }

        try {
            // obtain our animation frames for this action sequence
            _aframes = _charmgr.getActionFrames(_descrip, _action);

            // clear out our frames so that we recomposite on next tick
            _frames = null;

            // update the sprite render attributes
            setFrameRate(actseq.framesPerSecond);

        } catch (NoSuchComponentException nsce) {
            Log.warning("Character sprite references non-existent " +
                        "component [sprite=" + this + ", err=" + nsce + "].");

        } catch (Exception e) {
            Log.warning("Failed to obtain action frames [sprite=" + this +
                        ", descrip=" + _descrip + ", action=" + _action + "].");
            Log.logStackTrace(e);
        }
    }

    /** Called to recomposite our action frames if needed. */
    protected final void compositeActionFrames ()
    {
        if (_frames == null && _aframes != null) {
            setFrames(_aframes.getFrames(_orient));
        }
    }

    /**
     * Makes it easier to track down problems with bogus character descriptors.
     */
    protected void sanityCheckDescrip ()
    {
        if (_descrip.getComponentIds() == null ||
            _descrip.getComponentIds().length == 0) {
            Log.warning("Invalid character descriptor [sprite=" + this +
                        ", descrip=" + _descrip + "].");
            Thread.dumpStack();
        }
    }

    // documentation inherited
    protected boolean tickPath (long tickStamp)
    {
        boolean moved = super.tickPath(tickStamp);
        // composite our action frames if our path caused them to become
        // invalid
        compositeActionFrames();
        return moved;
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
        if (_aframes == null) {
            Log.warning("Have no action frames! " + _aframes + ".");
        } else {
            _oxoff = _aframes.getXOrigin(_orient, frameIdx);
            _oyoff = _aframes.getYOrigin(_orient, frameIdx);
        }

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
