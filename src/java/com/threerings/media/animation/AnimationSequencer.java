//
// $Id: AnimationSequencer.java,v 1.8 2002/11/05 21:53:56 mdb Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.ArrayList;

import com.threerings.media.Log;

/**
 * An animation that provides facilities for adding a sequence of
 * animations that are fired after a fixed time interval has elapsed or
 * after previous animations in the sequence have completed. Facilities
 * are also provided for running code upon the completion of animations in
 * the sequence.
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
     * parameters. Note that care should be taken if this is called after
     * the animation sequence has begun firing animations.
     *
     * @param anim the animation to be sequenced, or null if the
     * completion action should be run immediately when this "animation"
     * is ready to fired.
     * @param delta the number of milliseconds following the
     * <em>start</em> of the previous animation in the queue that this
     * animation should be started; 0 if it should be started
     * simultaneously with its predecessor int the queue; -1 if it should
     * be started when its predecessor has completed.
     * @param completionAction a runnable to be executed when this
     * animation completes.
     */
    public void addAnimation (
        Animation anim, long delta, Runnable completionAction)
    {
        // sanity check
        if (_finished) {
            throw new IllegalStateException(
                "Animation added to finished sequencer");
        }

        AnimRecord arec = new AnimRecord(anim, delta, completionAction);
        if (delta == -1) {
            int size = _queued.size();
            if (size == 0) {
                // if there's no predecessor then this guy has nobody to
                // wait for, so we run him immediately
                arec.delta = 0;
            } else {
                ((AnimRecord)_queued.get(size - 1)).dependent = arec;
            }
        }
        _queued.add(arec);
    }

    /**
     * Clears out the animations being managed by this sequencer.
     */
    public void clear ()
    {
        _queued.clear();
        _lastStamp = 0;
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        if (_lastStamp == 0) {
            _lastStamp = tickStamp;
        }

        // add all animations whose time has come
        while (_queued.size() > 0) {
            AnimRecord arec = (AnimRecord)_queued.get(0);
            if (!arec.readyToFire(tickStamp, _lastStamp)) {
                // if it's not time to add this animation, all subsequent
                // animations must surely wait as well
                break;
            }

            // remove it from queued and put it on the running list
            _queued.remove(0);
            _running.add(arec);

            // note that we've advanced to the next animation
            _lastStamp = tickStamp;

            // fire in the hole!
            arec.fire(tickStamp);
        }

        // we're done when both lists are empty
//         boolean finished = _finished;
        _finished = ((_queued.size() + _running.size()) == 0);
//         if (!finished && _finished) {
//             Log.info("Finishing sequence  at " + (tickStamp%100000) + ".");
//         }
    }

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

    /**
     * Called when the time comes to start an animation.  Derived classes
     * must implement this method and pass the animation on to their
     * animation manager and do whatever else they need to do with
     * operating animations.
     *
     * @param anim the animation to be displayed.
     * @param tickStamp the timestamp at which this animation was fired.
     */
    protected abstract void startAnimation (Animation anim, long tickStamp);

    protected class AnimRecord
        implements AnimationObserver
    {
        public long delta;
        public AnimRecord dependent;

        public AnimRecord (
            Animation anim, long delta, Runnable completionAction)
        {
            _anim = anim;
            this.delta = delta;
            _completionAction = completionAction;
        }

        public boolean readyToFire (long now, long lastStamp)
        {
            return (delta != -1) && (lastStamp + delta >= now);
        }

        public void fire (long when)
        {
//             String aclass = (_anim == null) ? "<none>" :
//                 _anim.getClass().getName();
//             Log.info("Firing animation " + aclass + " at " +
//                      (when%100000) + ".");

            // if we have an animation, start it up and await its
            // completion
            if (_anim != null) {
                startAnimation(_anim, when);
                _anim.addAnimationObserver(this);

            } else {
                // since there's no animation, we need to fire our
                // completion routine immediately
                fireCompletion(when);
            }
        }

        public void fireCompletion (long when)
        {
            // call the completion action, if there is one
            if (_completionAction != null) {
                try {
                    _completionAction.run();
                } catch (Throwable t) {
                    Log.logStackTrace(t);
                }
            }

            // make a note that this animation is complete
            _running.remove(this);

            // if the next animation is triggered on the completion of
            // this animation...
            if (dependent != null) {
                // ...fiddle its delta so that it becomes immediately
                // ready to fire
                dependent.delta = when - _lastStamp;

                // kids, don't try this at home; we call tick()
                // immediately so that this dependent animation and
                // any simultaneous subsequent animations are fired
                // immediately rather than waiting for the next call
                // to tick
                tick(when);
            }
        }

        public void handleEvent (AnimationEvent event)
        {
            if (event instanceof AnimationCompletedEvent) {
                fireCompletion(event.getWhen());
            }
        }

        protected Animation _anim;
        protected Runnable _completionAction;
    }

    /** Animations that have not been fired. */
    protected ArrayList _queued = new ArrayList();

    /** Animations that are currently running. */
    protected ArrayList _running = new ArrayList();

    /** The timestamp at which we fired the last animation. */
    protected long _lastStamp;
}
