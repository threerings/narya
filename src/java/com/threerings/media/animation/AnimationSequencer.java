//
// $Id: AnimationSequencer.java,v 1.2 2002/11/05 20:53:53 mdb Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.ArrayList;

import com.threerings.media.Log;

/**
 * An animation that provides facilities for adding a sequence of
 * animations with a standard or per-animation-specifiable delay between
 * each.
 */
public abstract class AnimationSequencer extends Animation
{
    /**
     * Constructs an animation sequencer with the expectation that
     * animations will be added via subsequent calls to {@link
     * #addAnimation}.
     */
    public AnimationSequencer ()
    {
        super(new Rectangle());
    }

    /**
     * Adds the supplied animation to the sequence with the given
     * parameters.  Note that this cannot be called after the animation
     * sequence has begun executing without endangering your sanity and
     * the robustness of your code.
     *
     * @param anim the animation to be sequenced, or null if the
     * completion action should be run immediately when this animation is
     * ready to fired.
     * @param delta the number of milliseconds following the start of the last
     * animation currently in the sequence that this animation should be
     * started; or -1 which means that this animation should be started
     * when the last animation has completed.
     * @param completionAction a runnable to be executed when this
     * animation completes.
     */
    public void addAnimation (
        Animation anim, long delta, Runnable completionAction)
    {
        if (_finished) {
            throw new IllegalStateException("Ack! Animation added when we were finished.");
        }
        AnimRecord arec = new AnimRecord(anim, delta, completionAction);
        if (delta == -1) {
            int size = _anims.size();
            if (size == 0) {
                // if there's no predecessor then this guy has nobody to
                // wait for, so we run him immediately
                arec.delta = 0;
            } else {
                ((AnimRecord)_anims.get(size - 1)).dependent = arec;
            }
        }
        _anims.add(arec);
        _animsLeft++;
    }

    /**
     * Clears out the animations being managed by this sequencer.
     */
    public void clear ()
    {
        _anims.clear();
        _animsLeft = 0;
        _lastidx = -1;
        _lastStamp = 0;
    }

    /**
     * Returns the number of animations being managed by this sequencer.
     */
    public int getAnimationCount ()
    {
        return _anims.size();
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        if (_lastStamp == 0) {
            _lastStamp = tickStamp;
        }

        // add all animations whose time has come
        int acount = _anims.size();
        for (int ii = _lastidx + 1; ii < acount; ii++) {
            AnimRecord arec = (AnimRecord)_anims.get(ii);

            if (arec.readyToFire(tickStamp, _lastStamp)) {
                // note that we've advanced to the next animation
                _lastidx = ii;
                _lastStamp = tickStamp;

                // Log.info("Adding animation [ii=" + ii +
                // ", tickStamp=" + tickStamp + "].");
                if (arec.anim != null) {
                    addAnimation(ii, arec.anim, tickStamp);
                    arec.anim.addAnimationObserver(arec);

                } else if (arec.completionAction != null) {
                    try {
                        arec.completionAction.run();
                    } catch (Throwable t) {
                        Log.logStackTrace(t);
                    }

                    // if our last "animation" is not an animation at all,
                    // we need to finish now
                    animationDone();
                }

            } else {
                // it's not time to add this animation, and so all
                // subsequent animations must surely wait as well
                break;
            }
        }
    }

    protected void animationDone ()
    {
        _finished = (--_animsLeft == 0);
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
        _lastStamp += timeDelta;
    }

    protected class AnimRecord
        implements AnimationObserver
    {
        public Animation anim;
        public long delta;
        public Runnable completionAction;
        public AnimRecord dependent;

        public AnimRecord (
            Animation anim, long delta, Runnable completionAction)
        {
            this.anim = anim;
            this.delta = delta;
            this.completionAction = completionAction;
        }

        public boolean readyToFire (long now, long lastStamp)
        {
            return (delta != -1) && (lastStamp + delta >= now);
        }

        public void handleEvent (AnimationEvent event)
        {
            if (event instanceof AnimationCompletedEvent) {
                // if the next animation is triggered on the completion of
                // this animation, fiddle its delta so that it will claim
                // to be ready to fire
                if (dependent != null) {
                    dependent.delta = event.getWhen() - _lastStamp;
                    // kids, don't try this at home; we call tick()
                    // immediately so that this dependent animation and
                    // any simultaneous subsequent animations are fired
                    // immediately rather than waiting for the next call
                    // to tick
                    tick(event.getWhen());
                }

                // call the completion action, if there is one
                if (completionAction != null) {
                    completionAction.run();
                }

                // make a note that this animation is complete
                animationDone();
            }
        }
    }

    /** The animation records detailing the animations to be sequenced. */
    protected ArrayList _anims = new ArrayList();

    /** The number of animations remaining to be finished. */
    protected int _animsLeft;

    /** The index of the last animation that was added. */
    protected int _lastidx = -1;

    /** The timestamp at which we added the last animation. */
    protected long _lastStamp;
}
