//
// $Id: ImageSprite.java,v 1.20 2004/08/27 02:12:41 mdb Exp $
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

package com.threerings.media.sprite;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.threerings.media.image.Mirage;
import com.threerings.media.util.MultiFrameImage;
import com.threerings.media.util.SingleFrameImageImpl;

/**
 * Extends the sprite class to support rendering the sprite with one or
 * more frames of image animation.  Overrides various methods to provide
 * correspondingly desirable functionality, e.g., {@link #hitTest} only
 * reports a hit if the specified point is within a non-transparent pixel
 * for the sprite's current image frame.
 */
public class ImageSprite extends Sprite
{
    /** Default frame rate. */
    public static final int DEFAULT_FRAME_RATE = 15;

    /** Animation mode indicating no animation. */
    public static final int NO_ANIMATION = 0;

    /** Animation mode indicating movement cued animation. */
    public static final int MOVEMENT_CUED = 1;

    /** Animation mode indicating time based animation. */
    public static final int TIME_BASED = 2;

    /** Animation mode indicating sequential progressive animation.
     * Frame 0 is guaranteed to be shown first for the full duration, and
     * so on. */
    public static final int TIME_SEQUENTIAL = 3;

    /**
     * Constructs an image sprite without any associated frames and with
     * an invalid default initial location. The sprite should be populated
     * with a set of frames used to display it via a subsequent call to
     * {@link #setFrames}, and its location updated with {@link
     * #setLocation}.
     */
    public ImageSprite ()
    {
        this((MultiFrameImage)null);
    }

    /**
     * Constructs an image sprite.
     *
     * @param frames the multi-frame image used to display the sprite.
     */
    public ImageSprite (MultiFrameImage frames)
    {
        // initialize frame animation member data
        _frames = frames;
        _frameIdx = 0;
        _animMode = NO_ANIMATION;
        _frameDelay = 1000L/DEFAULT_FRAME_RATE;
    }

    /**
     * Constructs an image sprite that will display the supplied single
     * image when rendering itself.
     */
    public ImageSprite (Mirage image)
    {
        this(new SingleFrameImageImpl(image));
    }

    // documentation inherited
    protected void init ()
    {
        super.init();

        // now that we have our sprite manager, we can lay ourselves out
        // and initialize our frames
        layout();
    }

    /**
     * Returns true if the sprite's bounds contain the specified point,
     * and if there is a non-transparent pixel in the sprite's image at
     * the specified point, false if not.
     */
    public boolean hitTest (int x, int y)
    {
        // first check to see that we're in the sprite's bounds and that
        // we've got a frame image (if we've got no image, there's nothing
        // to be hit)
        if (!super.hitTest(x, y) || _frames == null) {
            return false;
        }

        return _frames.hitTest(_frameIdx, x - _bounds.x, y - _bounds.y);
    }

    /**
     * Sets the animation mode for this sprite. The available modes are:
     *
     * <ul>
     * <li><code>TIME_BASED</code>: cues the animation based on a target
     * frame rate (specified via {@link #setFrameRate}).
     * <li><code>MOVEMENT_CUED</code>: ticks the animation to the next
     * frame every time the sprite is moved along its path.
     * <li><code>NO_ANIMATION</code>: disables animation.
     * </ul>
     *
     * @param mode the desired animation mode.
     */
    public void setAnimationMode (int mode)
    {
        _animMode = mode;
    }

    /**
     * Sets the number of frames per second desired for the sprite
     * animation. This is only used when the animation mode is
     * <code>TIME_BASED</code>.
     *
     * @param fps the desired frames per second.
     */
    public void setFrameRate (float fps)
    {
        _frameDelay = (long)(1000/fps);
    }

    /**
     * Set the image to be used for this sprite.
     */
    public void setMirage (Mirage mirage)
    {
        setFrames(new SingleFrameImageImpl(mirage));
    }

    /**
     * Set the image array used to render the sprite.
     *
     * @param frames the sprite images.
     */
    public void setFrames (MultiFrameImage frames)
    {
        if (frames == null) {
            // Log.warning("Someone set up us the null frames! " +
            // "[sprite=" + this + "].");
            return;
        }

        // if these are the same frames we already had, no need to do a
        // bunch of pointless business
        if (frames == _frames) {
            return;
        }

        // set and init our frames
        _frames = frames;
        _frameIdx = 0;
        layout();
    }

    /**
     * Instructs this sprite to lay out its current frame and any
     * accoutrements.
     */
    public void layout ()
    {
        if (_frames != null) {
            setFrameIndex(_frameIdx, true);
        }
    }

    /**
     * Instructs the sprite to display the specified frame index.
     */
    protected void setFrameIndex (int frameIdx, boolean forceUpdate)
    {
        // make sure we're displaying a valid frame
        frameIdx = (frameIdx % _frames.getFrameCount());

        // if this is the same frame we're already displaying and we're
        // not being forced to update, we can stop now
        if (frameIdx == _frameIdx && !forceUpdate) {
            return;
        } else {
            _frameIdx = frameIdx;
        }

        // start with our old bounds
        Rectangle dirty = new Rectangle(_bounds);

        // determine our drawing offsets and rendered rectangle size
        accomodateFrame(_frameIdx, _frames.getWidth(_frameIdx),
                        _frames.getHeight(_frameIdx));

        // add our new bounds
        dirty.add(_bounds);

        // give the dirty rectangle to the region manager
        if (_mgr != null) {
            _mgr.getRegionManager().addDirtyRegion(dirty);
        }
    }

    /**
     * Must adjust the bounds to accomodate the our new frame. This
     * includes changing the width and height to reflect the size of the
     * new frame and also updating the render origin (if necessary) and
     * calling {@link #updateRenderOrigin} to reflect those changes in the
     * sprite's bounds.
     *
     * @param frameIdx the index of our new frame.
     * @param width the width of the new frame.
     * @param height the height of the new frame.
     */
    protected void accomodateFrame (int frameIdx, int width, int height)
    {
        _bounds.width = width;
        _bounds.height = height;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        if (_frames != null) {
//             // DEBUG: fill our background with an alpha'd rectangle
//             Composite ocomp = gfx.getComposite();
//             gfx.setComposite(ALPHA_BOUNDS);
//             gfx.setColor(Color.blue);
//             gfx.fill(_bounds);
//             gfx.setComposite(ocomp);

            // render our frame
            _frames.paintFrame(gfx, _frameIdx, _bounds.x, _bounds.y);

        } else {
            super.paint(gfx);
        }
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        // if we have no frames, we're hosulated (to use a Greenwell term)
        if (_frames == null) {
            return;
        }
        
        int fcount = _frames.getFrameCount();
        boolean moved = false;

        // move the sprite along toward its destination, if any 
        moved = tickPath(timestamp);

        // increment the display image if performing image animation
        int nfidx = _frameIdx;
        switch (_animMode) {
        case NO_ANIMATION:
            // nothing doing
            break;

        case TIME_BASED:
            nfidx = (int)((timestamp/_frameDelay) % fcount);
            break;

        case TIME_SEQUENTIAL:
            if (_firstStamp == 0L) {
                _firstStamp = timestamp;
            }
            nfidx = (int) (((timestamp - _firstStamp) / _frameDelay) % fcount);
            break;

        case MOVEMENT_CUED:
            // update the frame if the sprite moved
            if (moved) {
                nfidx = (_frameIdx + 1) % fcount;
            }
            break;
        }

        // update our frame (which will do nothing if this is the same as
        // our existing frame index)
        setFrameIndex(nfidx, false);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", fidx=").append(_frameIdx);
    }

    /** The images used to render the sprite. */
    protected MultiFrameImage _frames;

    /** The current frame index to render. */
    protected int _frameIdx;

    /** What type of animation is desired for this sprite. */
    protected int _animMode;

    /** For how many milliseconds to display an animation frame. */
    protected long _frameDelay;

    /** The first timestamp seen (in TIME_SEQUENTIAL mode). */
    protected long _firstStamp = 0L;

//     /** DEBUG: The alpha level used when rendering our bounds. */
//     protected static final Composite ALPHA_BOUNDS =
//         AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
}
