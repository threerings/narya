//
// $Id: MultiFrameAnimation.java,v 1.1 2002/09/17 20:08:21 mdb Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;

import com.threerings.media.util.FrameSequencer;
import com.threerings.media.util.MultiFrameImage;
import java.awt.Rectangle;

/**
 * Animates a sequence of image frames in place with a particular frame
 * rate.
 */
public class MultiFrameAnimation extends Animation
{
    /**
     * Creates a multi-frame animation with the specified source image
     * frames and the specified target frame rate (in frames per second).
     *
     * @param frames the source frames of the animation.
     * @param fps the target number of frames per second.
     * @param loop whether the animation should loop indefinitely or
     * finish after one shot.
     */
    public MultiFrameAnimation (
        MultiFrameImage frames, double fps, boolean loop)
    {
        this(frames, new FrameSequencer.ConstantRate(fps, loop));
    }

    /**
     * Creates a multi-frame animation with the specified source image
     * frames and the specified target frame rate (in frames per second).
     */
    public MultiFrameAnimation (MultiFrameImage frames, FrameSequencer seeker)
    {
        // we'll set up our bounds via setLocation() and in reset()
        super(new Rectangle());

        _frames = frames;
        _seeker = seeker;

        // reset ourselves to start things off
        reset();
    }

    // documentation inherited
    public Rectangle getBounds ()
    {
        // fill in the bounds with our current animation frame's bounds
        return _bounds;
    }

    /**
     * If this animation has run to completion, it can be reset to prepare
     * it for another go.
     */
    public void reset ()
    {
        super.reset();

        // reset ourselves to frame zero
        setFrameIndex(0);

        // reset our frame sequencer
        _seeker.init(_frames);
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        int fidx = _seeker.tick(tickStamp);
        if (fidx != _fidx) {
            // update our frame index and bounds
            setFrameIndex(fidx);

            // and have ourselves repainted
            invalidate();
        }
    }

    /**
     * Sets the frame index and updates our dimensions.
     */
    protected void setFrameIndex (int fidx)
    {
        _fidx = fidx;
        _bounds.width = _frames.getWidth(_fidx);
        _bounds.height = _frames.getHeight(_fidx);
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        _frames.paintFrame(gfx, _fidx, _bounds.x, _bounds.y);
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _seeker.fastForward(timeDelta);
    }

    protected MultiFrameImage _frames;
    protected FrameSequencer _seeker;
    protected int _fidx;
}
