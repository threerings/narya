//
// $Id: ImageSprite.java,v 1.7 2002/06/05 23:38:18 ray Exp $

package com.threerings.media.sprite;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Iterator;

import com.threerings.media.Log;

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

    /**
     * Constructs an image sprite without any associated frames and with a
     * default initial location of <code>(0, 0)</code>.  The sprite should
     * be populated with a set of frames used to display it via a
     * subsequent call to {@link #setFrames}, and its location updated
     * with {@link #setLocation}.
     */
    public ImageSprite ()
    {
        this(0, 0, null);
    }

    /**
     * Constructs an image sprite without any associated frames. The
     * sprite should be populated with a set of frames used to display it
     * via a subsequent call to {@link #setFrames}.
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     */
    public ImageSprite (int x, int y)
    {
        this(x, y, null);
    }

    /**
     * Constructs an image sprite.
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     * @param frames the multi-frame image used to display the sprite.
     */
    public ImageSprite (int x, int y, MultiFrameImage frames)
    {
        super(x, y);

	// initialize frame animation member data
        _frames = frames;
        _frameIdx = 0;
        _animMode = NO_ANIMATION;
        _frameDelay = 1000L/DEFAULT_FRAME_RATE;
    }

    // documentation inherited
    protected void init (SpriteManager spritemgr)
    {
        super.init(spritemgr);

        // now that we have our spritemanager, we can initialize our frames
        initFrames();
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
        initFrames();
    }

    /**
     * Initialize our frames.
     */
    protected void initFrames ()
    {
        // start with our old bounds
        Rectangle dirty = new Rectangle(_bounds);

        _frameIdx %= _frames.getFrameCount();

        // determine our drawing offsets and rendered rectangle size
        accomodateFrame(_frames.getWidth(_frameIdx),
                        _frames.getHeight(_frameIdx));

        // add our new bounds
        dirty.add(_bounds);

        updateRenderOffset();
        updateRenderOrigin();

        // give the dirty rectangle to the region manager
        if (_spritemgr != null) {
            _spritemgr.getRegionManager().addDirtyRegion(dirty);
        }
    }

    /**
     * Must adjust the bounds to accomodate the new image. Called when a
     * new image has been set for this image sprite. If a derived class is
     * going to expand the bounds beyond the bounds of the image frame, it
     * will need to override this method and adjust bounds accordingly for
     * the new frame (which can be null).
     */
    protected void accomodateFrame (int width, int height)
    {
        _bounds.width = width;
        _bounds.height = height;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        if (_frames != null) {
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
        if (_path != null) {
            moved = _path.tick(this, timestamp);
        }

        // increment the display image if performing image animation
        int nfidx = _frameIdx;
        switch (_animMode) {
        case NO_ANIMATION:
            // nothing doing
            break;

        case TIME_BASED:
            nfidx = (int)((timestamp/_frameDelay) % fcount);
            break;

        case MOVEMENT_CUED:
            // update the frame if the sprite moved
            if (moved) {
                nfidx = (_frameIdx + 1) % fcount;
            }
            break;
        }

        // only update the sprite if our frame index changed
        if (nfidx != _frameIdx) {
            _frameIdx = nfidx;
            // dirty our rectangle since we've altered our display image
            invalidate();
        }
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
}
