//
// $Id: AnimationSequencer.java,v 1.1 2002/09/30 06:30:47 shaper Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.threerings.media.Log;

/**
 * An animation that provides facilities for adding a sequence of
 * animations with a standard or per-animation-specifiable delay between
 * each.
 */
public abstract class AnimationSequencer extends Animation
{
    /**
     * Constructs an animation sequencer that adds each of the supplied
     * animations after regular intervals of the specified time.  Note
     * that the first animation will be added immediately.
     */
    public AnimationSequencer (Animation[] anims, long delta)
    {
        super(new Rectangle());

        // save things off
        _anims = anims;
        _sdelta = delta;
    }

    /**
     * Constructs an animation sequencer that adds each of the supplied
     * animations after the time specified for each animation has elapsed.
     * Note that the first animation will be added immediately, though
     * (for ease of coding and array indexing) it should have a slot in
     * the <code>delta</code> array despite the fact that the value
     * therein will be ignored.
     */
    public AnimationSequencer (Animation[] anims, long[] delta)
    {
        super(new Rectangle());

        // save things off
        _anims = anims;
        _delta = delta;
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        if (_start == 0) {
            // initialize our starting time
            _start = tickStamp;
        }

        // TODO: revamp the below to keep track of the mark time for the
        // next animation to be added rather than recalculating it every
        // time we're ticked

        // add all animations whose time has come
        int acount = _anims.length;
        long mark = _start;
        for (int ii = 0; ii < acount; ii++) {
            // calculate the time at which this animation is to be added
            long delta = (_delta == null) ? _sdelta : _delta[ii];
            // add in the delta for this animation, or if it's the first
            // animation, we add it straightaway and ignore its delta
            mark += ((ii == 0) ? 0 : delta);

            if (_lastidx >= ii) {
                // we've already added this animation, so move on to the next
                continue;

            } else if (tickStamp >= mark) {
                // add the animation
                // Log.info("Adding animation [ii=" + ii +
                // ", tickStamp=" + tickStamp + ", mark=" + mark + "].");
                addAnimation(ii, _anims[ii], tickStamp);
                _lastidx = ii;

            } else {
                // it's not time to add this animation, and so all
                // subsequent animations must surely wait as well
                break;
            }
        }

        // we're finished when we've added the last animation
        _finished = (_lastidx == acount - 1);
    }

    /**
     * Called when the time comes to add an animation.  Derived classes
     * must implement this method and do whatever they deem necessary in
     * order to add the given animation to the animation manager and any
     * other interested parties.
     *
     * @param index the index number of the animation in the sequence of
     * animations to be added by this sequencer.
     * @param anim the animation to be added.
     * @param tickStamp the tick stamp provided by the animation manager
     * when the sequencer animation decided the time had come to add the
     * animation.
     */
    public abstract void addAnimation (
        int index, Animation anim, long tickStamp);

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        // don't care
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _start += timeDelta;
    }

    /** The animations to be added in sequence. */
    protected Animation[] _anims;

    /** The time deltas in milliseconds between adding each animation, if
     * the time deltas were specified on a per-animation basis. */
    protected long[] _delta;

    /** The single time delta in milliseconds between adding each
     * animation, if a single time delta was given for all animations. */
    protected long _sdelta;

    /** The time at which the sequencer animation started its business. */
    protected long _start;

    /** The index of the last animation that was added. */
    protected int _lastidx = -1;
}
