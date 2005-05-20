//
// $Id: HourglassView.java 18697 2005-01-19 01:18:47Z tedv $

package com.threerings.media;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;

import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.util.ResultListener;

/**
 * A generic timer class that can be rendered on screen.
 *
 * NOTE: This base class doesn't actually render anything, but it's still
 * useful for triggering user supplied callback functions.  Derived classes
 * are more than welcome to setup their own rendering, of course.
 */
public class TimerView
    implements FrameParticipant, HierarchyListener
{
    /**
     * Constructs a timer view that fires at the default rate.
     */
    public TimerView (FrameManager fmgr, JComponent host, Rectangle bounds)
    {
        // Cache the input arguments
        _fmgr = fmgr;
        _host = host;
        _bounds = new Rectangle(bounds);

        // Watch for changes in the timer's state so it can effectively
        // register or unregister with the frame manager as necessary
        _host.addHierarchyListener(this);
        checkFrameParticipation();
    }

    /**
     * Sets whether this timer should be rendered.
     */
    public void setEnabled (boolean enabled)
    {
        if (_enabled != enabled)
        {
            _enabled = enabled;
            invalidate();
        }
    }

    /**
     * Get the amount of time it takes to render digital changes in the
     * completion state.
     */
    public long getTransitionTime ()
    {
        return _transitionTime;
    }

    /**
     * Set the amount of time it takes to render digital changes in the
     * completion state (probably due to a timer reset).
     */
    public void setTransitionTime (long time)
    {
        _transitionTime = Math.max(0, time);
    }

    /**
     * Setup a warning to trigger after the timer is "warnPercent"
     * or more completed.  If "warner" is non-null, it will be
     * called at that time.
     */
    public void setWarning (float warnPercent, ResultListener warner)
    {
        // This warning hasn't triggered yet
        _warned = false;

        // Here are the details
        _warnPercent = warnPercent;
        _warner = warner;
    }

    /**
     * Remove any warning this timer might have had.
     */
    public void removeWarning ()
    {
        // Don't trigger any warnings
        _warned = false;
        _warnPercent = -1;
        _warner = null;
    }

    /** Test if the timer is running right now. */
    public boolean running ()
    {
        return _running;
    }

    /**
     * Start the timer running from the specified percentage complete,
     * to expire at the specified time.
     *
     * @param startPercent a value in [0f, 1f) indicating how much
     * hourglass time has already elapsed when the timer starts.
     * @param duration The time interval over which the timer would
     * run if it started at 0%.
     * @param finisher a listener that will be notified when the timer
     * finishes, or null if nothing should be notified.
     */
    public void start (float startPercent, long duration,
                       ResultListener finisher)
    {
        // Sanity check input arguments
        if (startPercent < 0.0f || startPercent >= 1.0f) {
            throw new IllegalArgumentException(
                "Invalid starting percent " + startPercent);
        }
        if (duration < 0) {
            throw new IllegalArgumentException("Invalid duration " + duration);
        }

        // Stop any current processing
        stop();
        
        // Record the timer's full duration and effective start time
        _duration = duration;

        // Change the completion percent and make sure the starting
        // time gets updated on the next tick
        changeComplete(startPercent);
        _start = Long.MIN_VALUE;

        // Thank you sir; would you kindly take a chair in the waiting room?
        _finisher = finisher;

        // The warning and completion handlers haven't been triggered yet
        _warned = false;
        _completed = false;

        // Start things running
        _running = true;
    }

    /**
     * Reset the timer.
     */
    public void reset ()
    {
        // Stop processing permanently
        stop();

        // Reset the completion percent
        changeComplete(0f);
    }

    /**
     * Stop the timer.
     */
    public void stop ()
    {
        // Stop processing
        pause();

        // Prevent it from being unpaused
        _lastUpdate = Long.MIN_VALUE;
    }        

    /**
     * Pause the timer from processing.
     */
    public void pause ()
    {
        // Stop processing
        _running = false;
    }

    /**
     * Unpause the timer.
     */
    public void unpause ()
    {
        // Don't unpause the timer if it wasn't paused to begin with
        if (_lastUpdate == Long.MIN_VALUE || _start == Long.MIN_VALUE) {
            return;
        }

        // Adjust the starting time when the timer next ticks
        _processUnpause = true;

        // Start things running again
        _running = true;
    }

    /**
     * Generate an unexpected change in the timer's completion and
     * set up the necessary details to render interpolation from the old
     * to new states
     */
    public void changeComplete (float complete)
    {
        // Store the new state
        _complete = complete;

        // Determine the percentage difference between the "actual"
        // completion state and the completion amount to render during
        // the smooth interpolation
        _renderOffset = _renderComplete - _complete;

        // When the timer next ticks, find out when this interpolation
        // should start
        _renderOffsetTime = Long.MIN_VALUE;
    }

    /**
     * Updates the timer for the current inputted time.
     */
    protected void update (long now)
    {
    }

    /**
     * Test if the warning was triggered and needs to be processed.
     */
    protected boolean triggeredWarning ()
    {
        // Trigger a warning if it exists, it hasn't occured yet,
        // and the timer has passed the warning threshold.
        return ((_warnPercent >= 0f) &&
                !_warned && (_warnPercent <= _complete));
    }

    /**
     * Test if the completion was triggered and needs to be processed.
     */
    protected boolean triggeredCompleted ()
    {
        return (!_completed && (1.0 <= _complete));
    }

    /**
     * Handle the trigger of the warning.
     */
    protected void handleWarning ()
    {
        // Remember that this warning was handled
        _warned = true;

        // Execute the warning listener if one was supplied
        if (_warner != null) {
            _warner.requestCompleted(this);
        }
    }

    /**
     * Handle the trigger of the timer's completion.
     */
    protected void handleCompleted ()
    {
        // Remember that the completion was handled
        _completed = true;

        // Stop the timer
        stop();

        // Handle the trigger function if one was supplied
        if (_finisher != null) {
            _finisher.requestCompleted(this);
        }
    }

    /**
     * Invalidates this view's bounds via the host component.
     */
    protected void invalidate ()
    {
        // Schedule the timer's location on screen to get repainted
        _invalidated = true;
        _host.repaint(_bounds.x, _bounds.y, _bounds.width, _bounds.height);
    }

    /**
     * Renders the timer to the given graphics context if enabled.
     */
    public void render (Graphics2D gfx)
    {
        // Paint the timer if its enabled
        if (_enabled) {
            paint(gfx, _renderComplete);
        }
        _invalidated = false;
    }

    /**
     * Paint the timer into the given graphics context at the inputted
     * percent complete (0f means just started, 1f means just finished).
     */
    public void paint (Graphics2D gfx, float complete)
    {
        // Inheriting classes will want to implement their own
        // version of this function.  Remember to call this one though!

        // Remember the completion level the last time the timer was painted
        _paintComplete = complete;
    }

    // documentation inherited
    public void tick (long now)
    {
        if (!_enabled) {
            return;
        }

        // Initialize the starting time if necessary
        if (_start == Long.MIN_VALUE) {
            _start = now - Math.round(_duration * _complete);
        }

        // Initialize the timestamp of the rendering error if necessary
        if (_renderOffsetTime == Long.MIN_VALUE) {
            _renderOffsetTime = now;
        }

        // If the timer was just unpaused, handle the updates that
        // need a timestamp
        if (_processUnpause) {
            _processUnpause = false;
            _start += now - _lastUpdate;
        }

        // Update the completion and triggers when the timer is running
        if (running())
        {
            // Figure out how what percent the timer has completed
            _lastUpdate = now;
            _complete = ((float) (_lastUpdate - _start)) / _duration;

            // Handle warning trigger if necessary
            if (triggeredWarning()) {
                handleWarning();
            }

            // Handle completion trigger if necessary
            if (triggeredCompleted()) {
                handleCompleted();
            }
        }

        // Add error to the render completion percent if the interpolation
        // hasn't finished yet
        _renderComplete = _complete;
        float error = _renderOffset;
        if (_renderOffsetTime + _transitionTime > now) {

            _renderComplete += _renderOffset *
                (1f - (now - _renderOffsetTime) / (float) _transitionTime);
            _renderComplete = Math.max(0f, Math.min(1f, _renderComplete));
        }

        // Possibly orce a repaint if the render level changed (highly
        // probable but not guaranteed)
        if (_renderComplete != _paintComplete)
        {
            // Always force a repaint when changing to or from a boundary
            if (_renderComplete <= 0f || _paintComplete <= 0f ||
                _renderComplete >= 1f || _paintComplete >= 1f)
            {
                invalidate();
            }

            // Also force a repaint if the completion state sufficiently
            // changed
            else if (Math.abs(_renderComplete - _paintComplete) >
                     _changeThreshold)
            {
                invalidate();
            }
        }
    }

    // documentation inherited
    public boolean needsPaint ()
    {
        // Always paint if the timer was invalidated
        return _invalidated;
    }

    // documentation inherited
    public Component getComponent ()
    {
        return null;
    }

    // documentation inherited
    public void hierarchyChanged (HierarchyEvent e)
    {
        checkFrameParticipation();
    }

    /** Check that the frame knows about the timer. */
    public void checkFrameParticipation ()
    {
        // Determine whether or not the timer should participate in
        // media ticks
        boolean participate = _host.isShowing();

        // Start participating if necessary
        if (participate && !_participating)
        {
            _fmgr.registerFrameParticipant(this);
            _participating = true;
        }

        // Stop participating if necessary
        else if (!participate && _participating)
        {
            _fmgr.removeFrameParticipant(this);
            _participating = false;
        }
    }

    /** The frame manager that manages our animated view. */
    protected FrameManager _fmgr;

    /** The media panel containing the view. */
    protected JComponent _host;

    /** The screen coordinates of the timer's bounding box. */
    protected Rectangle _bounds;

    /** Whether to render the timer. */
    protected boolean _enabled = true;

    /** Whether the timer is running. */
    protected boolean _running = false;

    /** Whether the timer is participating in media tick updates. */
    protected boolean _participating = false;

    /** The last time the timer updated while running. */
    protected long _lastUpdate = Long.MIN_VALUE;

    /** The total amount of time in the timer when it was 0% done. */
    protected long _duration;

    /** The timestamp when the timer effectively started. */
    protected long _start;

    /** The percent of the duration time completed. */
    protected float _complete = 0f;

    /** The difference between the old rendered completion percent and the
     * new completion percent the last time the completion percent had
     * an unexpected change.  In other words, this is the greatest amount
     * that _renderComplete will differ from _complete during an
     * state interpolation.
     */
    protected float _renderOffset = 0f;

    /** The timestamp of _renderOffset. */
    protected long _renderOffsetTime = Long.MIN_VALUE;

    /** The amount of time it takes to fully interpolation a render
     * transition from completion 0.0 to 1.0. */
    protected long _transitionTime = 0;

    /** The completion percent at which to render the timer. */
    protected float _renderComplete = 0f;

    /** The completion percent last time the timer was repainted. */
    protected float _paintComplete = -1;

    /**
     * The timer will not invalidate itself until the completion level
     * to render changes by at least this much from the previous time it
     * was invalidated.
     */
    protected float _changeThreshold = 0.0f;

    /** True if the timer has been invalidated since its last repaint. */
    protected boolean _invalidated;

    /** Trigger the warning when at least this much time has elapsed,
     * or -1 for no warning. */
    protected float _warnPercent = -1;

    /** True if the warning has already been processed. */
    protected boolean _warned;

    /** True if the completion has already been processed. */
    protected boolean _completed;

    /** True if the code should process everything required for an unpause
     * on the next tick(). */
    protected boolean _processUnpause = false;

    /** A listener to be notified when the timer finishes. */
    protected ResultListener _finisher;

    /** A listener to be notified when the warning time occurs. */
    protected ResultListener _warner;

    /** The default update date. */
    protected static final long DEFAULT_RATE = 100L;
}
