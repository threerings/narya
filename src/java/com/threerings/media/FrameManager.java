//
// $Id: FrameManager.java,v 1.48 2003/12/13 02:59:05 mdb Exp $

package com.threerings.media;

import java.applet.Applet;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.awt.EventQueue;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.RepaintManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.samskivert.swing.Label;
import com.samskivert.util.DebugChords;
import com.samskivert.util.ObserverList;
import com.samskivert.util.RunAnywhere;
import com.samskivert.util.RuntimeAdjust;
import com.samskivert.util.StringUtil;

import com.threerings.media.timer.MediaTimer;
import com.threerings.media.timer.SystemMediaTimer;
import com.threerings.media.util.TrailingAverage;
import com.threerings.util.unsafe.Unsafe;

/**
 * Provides a central point from which the computation for each "frame" or
 * tick can be dispatched. This assumed that the application structures
 * its activity around the rendering of each frame, which is a common
 * architecture for games. The animation and sprite support provided by
 * other classes in this package are structured for use in an application
 * that uses a frame manager to tick everything once per frame.
 *
 * <p> The frame manager goes through a simple two part procedure every
 * frame:
 *
 * <ul>
 * <li> Ticking all of the frame participants: in {@link
 * FrameParticipant#tick}, any processing that need be performed during
 * this frame should be performed. Care should be taken not to execute
 * code that will take unduly long, instead such processing should be
 * broken up so that it can be performed in small pieces every frame (or
 * performed on a separate thread with the results safely communicated
 * back to the frame participants for incorporation into the rendering
 * loop).
 *
 * <li> Painting the user interface hierarchy: the top-level component
 * (the frame) is painted (via a call to {@link JFrame#paint}) into a flip
 * buffer (if supported, an off-screen buffer if not). Updates that were
 * computed during the tick should be rendered in this call to paint. The
 * paint call will propagate down to all components in the UI hierarchy,
 * some of which may be {@link FrameParticipant}s and will have prepared
 * themselves for their upcoming painting in the previous call to {@link
 * FrameParticipant#tick}. When the call to paint completes, the flip
 * buffer is flipped and the process starts all over again.  </ul>
 *
 * <p> The ticking and rendering takes place on the AWT thread so as to
 * avoid the need for complicated coordination between AWT event handler
 * code and frame code. However, this means that all AWT (and Swing) event
 * handlers <em>must not</em> perform any complicated processing. After
 * each frame, control of the AWT thread is given back to the AWT which
 * processes all pending AWT events before giving the frame manager an
 * opportunity to process the next frame. Thus the convenience of
 * everything running on the AWT thread comes with the price of requiring
 * that AWT event handlers not block or perform any intensive processing.
 * In general, this is a sensible structure for an application anyhow, so
 * this organization tends to be preferable to an organization where the
 * AWT and frame threads are separate and must tread lightly so as not to
 * collide.
 *
 * <p> Note: the way that <code>JScrollPane</code> goes about improving
 * performance when scrolling complicated contents cannot work with active
 * rendering. If you use a <code>JScrollPane</code> in an application that
 * uses the frame manager, you should either use the provided {@link
 * SafeScrollPane} or set your scroll panes' viewports to
 * <code>SIMPLE_SCROLL_MODE</code>.
 */
public abstract class FrameManager
{
    /**
     * Creates a frame manager that will use a {@link SystemMediaTimer} to
     * obtain timing information, which is available on every platform,
     * but returns inaccurate time stamps on many platforms.
     *
     * @see #newInstance(JFrame, MediaTimer)
     */
    public static FrameManager newInstance (JFrame frame)
    {
        // first try creating a PerfTimer which is the best if we're using
        // JDK1.4.2
        MediaTimer timer = null;
        try {
            timer = (MediaTimer)Class.forName(PERF_TIMER).newInstance();
        } catch (Throwable t) {
            Log.info("Can't use PerfTimer (" + t + ") reverting to " +
                     "System.currentTimeMillis() based timer.");
            timer = new SystemMediaTimer();
        }
        return newInstance(frame, timer);
    }

    /**
     * Constructs a frame manager that will do its rendering to the
     * supplied frame. It is likely that the caller will want to have put
     * the frame into full-screen exclusive mode prior to providing it to
     * the frame manager so that the frame manager can take advantage of
     * optimizations available in that mode.
     *
     * @see GraphicsDevice#setFullScreenWindow
     */
    public static FrameManager newInstance (JFrame frame, MediaTimer timer)
    {
        FrameManager fmgr;
        if (_useFlip.getValue()) {
            Log.info("Creating flip frame manager.");
            fmgr = new FlipFrameManager();
        } else {
            Log.info("Creating back frame manager.");
            fmgr = new BackFrameManager();
        }
        fmgr.init(frame, timer);
        return fmgr;
    }

    /**
     * Initializes this frame manager and prepares it for operation.
     */
    protected void init (JFrame frame, MediaTimer timer)
    {
        _frame = frame;
        if (frame instanceof ManagedJFrame) {
            ((ManagedJFrame)_frame).init(this);
        }
        _timer = timer;

        // set up our custom repaint manager
        _remgr = new FrameRepaintManager(_frame);
        RepaintManager.setCurrentManager(_remgr);

        // turn off double buffering for the whole business because we
        // handle repaints
        _remgr.setDoubleBufferingEnabled(false);

        if (DEBUG_EVENTS) {
            addTestListeners();
        }
    }

    /**
     * Adds a variety of listeners to the frame in order to provide
     * visibility into the various events received by the frame.
     */
    protected void addTestListeners ()
    {
        // add a test window listener
        _frame.addWindowListener(new WindowListener() {
            public void windowActivated (WindowEvent e) {
                Log.info("Window activated [evt=" + e + "].");
            }

            public void windowClosed (WindowEvent e) {
                Log.info("Window closed [evt=" + e + "].");
            }

            public void windowClosing (WindowEvent e) {
                Log.info("Window closing [evt=" + e + "].");
            }

            public void windowDeactivated (WindowEvent e) {
                Log.info("Window deactivated [evt=" + e + "].");
            }

            public void windowDeiconified (WindowEvent e) {
                Log.info("Window deiconified [evt=" + e + "].");
            }

            public void windowIconified (WindowEvent e) {
                Log.info("Window iconified [evt=" + e + "].");
            }

            public void windowOpened (WindowEvent e) {
                Log.info("Window opened [evt=" + e + "].");
            }
        });

        // add a component listener
        _frame.addComponentListener(new ComponentListener() {
            public void componentHidden (ComponentEvent e) {
                Log.info("Window component hidden [evt=" + e + "].");
            }

            public void componentShown (ComponentEvent e) {
                Log.info("Window component shown [evt=" + e + "].");
            }

            public void componentMoved (ComponentEvent e) {
                Log.info("Window component moved [evt=" + e + "].");
            }

            public void componentResized (ComponentEvent e) {
                Log.info("Window component resized [evt=" + e + "].");
            }
        });

        // add test ancestor focus listener
        _frame.getRootPane().addAncestorListener(
            new AncestorListener() {
                public void ancestorAdded (AncestorEvent e) {
                    Log.info("Root pane ancestor added [e=" + e + "].");
                }

                public void ancestorRemoved (AncestorEvent e) {
                    Log.info("Root pane ancestor removed [e=" + e + "].");
                }

                public void ancestorMoved (AncestorEvent e) {
                    Log.info("Root pane ancestor moved [e=" + e + "].");
                }
            });

        // add test key event dispatcher
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            addKeyEventDispatcher(new KeyEventDispatcher() {
                public boolean dispatchKeyEvent (KeyEvent e) {
//                     if ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0 &&
//                         e.getKeyCode() == KeyEvent.VK_TAB) {
//                         Log.info("Detected alt-tab key event " +
//                                  "[e=" + e + "].");
//                         // attempt to eat the event so that windows
//                         // doesn't alt-tab into unhappy land
//                         e.consume();
//                         return true;
//                     }

                    return false;
                }
            });
    }

    /**
     * Instructs the frame manager to target the specified number of
     * frames per second. If the computation and rendering for a frame are
     * completed with time to spare, the frame manager will wait until the
     * proper time to begin processing for the next frame. If a frame
     * takes longer than its alotted time, the frame manager will
     * immediately begin processing on the next frame.
     */
    public void setTargetFrameRate (int fps)
    {
        // compute the number of milliseconds per frame
        _millisPerFrame = 1000/fps;
    }

    /**
     * Registers a frame participant. The participant will be given the
     * opportunity to do processing and rendering on each frame.
     */
    public void registerFrameParticipant (FrameParticipant participant)
    {
        _participants.add(participant);
    }

    /**
     * Returns true if the specified participant is registered.
     */
    public boolean isRegisteredFrameParticipant (FrameParticipant participant)
    {
        return _participants.contains(participant);
    }

    /**
     * Removes a frame participant.
     */
    public void removeFrameParticipant (FrameParticipant participant)
    {
        _participants.remove(participant);
    }

    /**
     * Returns the frame being managed.
     */
    public JFrame getFrame ()
    {
        return _frame;
    }

    /**
     * Returns a millisecond granularity time stamp using the {@link
     * MediaTimer} with which this frame manager was configured.
     * <em>Note:</em> this should only be called from the AWT thread.
     */
    public long getTimeStamp ()
    {
        return _timer.getElapsedMillis();
    }

    /**
     * Starts up the per-frame tick
     */
    public void start ()
    {
        if (_ticker == null) {
            _ticker = new Ticker();
            _ticker.start();
            _lastTickStamp = 0;
        }
    }

    /**
     * Stops the per-frame tick.
     */
    public synchronized void stop ()
    {
        if (_ticker != null) {
            _ticker.cancel();
            _ticker = null;
        }
    }

    /**
     * Returns true if the tick interval is be running (not necessarily at
     * that instant, but in general).
     */
    public synchronized boolean isRunning ()
    {
        return (_ticker != null);
    }

    /**
     * Returns the number of ticks executed in the last second.
     */
    public int getPerfTicks ()
    {
        return Math.round(_fps[1]);
    }

    /**
     * Returns the number of ticks requested in the last second.
     */
    public int getPerfTries ()
    {
        return Math.round(_fps[0]);
    }

    /**
     * Returns debug performance metrics.
     */
    public TrailingAverage[] getPerfMetrics ()
    {
        if (_metrics == null) {
            _metrics = new TrailingAverage[] {
                new TrailingAverage(150),
                new TrailingAverage(150),
                new TrailingAverage(150) };
        }
        return _metrics;
    }

    /**
     * Called to perform the frame processing and rendering.
     */
    protected void tick (long tickStamp)
    {
        long start = 0L, paint = 0L;
        if (MediaPanel._perfDebug.getValue()) {
            start = paint = System.currentTimeMillis();
        }
        // if our frame is not showing (or is impossibly sized), don't try
        // rendering anything
        if (_frame.isShowing() &&
            _frame.getWidth() > 0 && _frame.getHeight() > 0) {
            // tick our participants
            tickParticipants(tickStamp);
            paint = System.currentTimeMillis();
            // repaint our participants and components
            paint(tickStamp);
        }
        if (MediaPanel._perfDebug.getValue()) {
            long end = System.currentTimeMillis();
            getPerfMetrics()[1].record((int)(paint-start));
            getPerfMetrics()[2].record((int)(end-paint));
        }
    }

    /**
     * Called once per frame to invoke {@link FrameParticipant#tick} on
     * all of our frame participants.
     */
    protected void tickParticipants (long tickStamp)
    {
        long gap = tickStamp - _lastTickStamp;
        if (_lastTickStamp != 0 && gap > (HANG_DEBUG ? HANG_GAP : BIG_GAP)) {
            Log.debug("Long tick delay [delay=" + gap + "ms].");
        }
        _lastTickStamp = tickStamp;

        // validate any invalid components
        try {
            _remgr.validateComponents();
        } catch (Throwable t) {
            Log.warning("Failure validating components.");
            Log.logStackTrace(t);
        }

        // tick all of our frame participants
        _participantTickOp.setTickStamp(tickStamp);
        _participants.apply(_participantTickOp);
    }

    /**
     * Called once per frame to invoke {@link Component#paint} on all of
     * our frame participants' components and all dirty components managed
     * by our {@link FrameRepaintManager}.
     */
    protected abstract void paint (long tickStamp);

    /**
     * Paints our frame participants and any dirty components via the
     * repaint manager.
     *
     * @return true if anything was painted, false if not.
     */
    protected boolean paint (Graphics2D gfx)
    {
        // paint our frame participants (which want to be handled
        // specially)
        _participantPaintOp.init(gfx);
        _participants.apply(_participantPaintOp);
        boolean ppart = _participantPaintOp.paintedSomething();

        // repaint any widgets that have declared they need to be
        // repainted since the last tick
        boolean pcomp = _remgr.paintComponents(gfx, this);

        // let the caller know if anybody painted anything
        return (ppart || pcomp);
    }

    /**
     * Called by the {@link ManagedJFrame} when our window was hidden and
     * reexposed.
     */
    protected abstract void restoreFromBack (Rectangle dirty);

    /**
     * Renders all components in all {@link JLayeredPane} layers that
     * intersect the supplied bounds.
     */
    protected void renderLayers (Graphics2D g, Component pcomp,
                                 Rectangle bounds, boolean[] clipped)
    {
        JLayeredPane lpane =
            JLayeredPane.getLayeredPaneAbove(pcomp);
        if (lpane != null) {
            renderLayer(g, bounds, lpane, clipped, JLayeredPane.PALETTE_LAYER);
            renderLayer(g, bounds, lpane, clipped, JLayeredPane.MODAL_LAYER);
            renderLayer(g, bounds, lpane, clipped, JLayeredPane.POPUP_LAYER);
            renderLayer(g, bounds, lpane, clipped, JLayeredPane.DRAG_LAYER);
        }
    }

    /**
     * Renders all components in the specified layer of the supplied
     * layered pane that intersect the supplied bounds.
     */
    protected void renderLayer (Graphics2D g, Rectangle bounds,
                                JLayeredPane pane, boolean[] clipped,
                                Integer layer)
    {
        // stop now if there are no components in that layer
        int ccount = pane.getComponentCountInLayer(layer.intValue());
        if (ccount == 0) {
            return;
        }

        // render them up
        Component[] comps = pane.getComponentsInLayer(layer.intValue());
        for (int ii = 0; ii < ccount; ii++) {
            Component comp = comps[ii];
            _lbounds.setBounds(0, 0, comp.getWidth(), comp.getHeight());
            getRoot(comp, _lbounds);
            if (!_lbounds.intersects(bounds)) {
                continue;
            }

            // if the clipping region has not yet been set during this
            // render pass, the time has come to do so
            if (!clipped[0]) {
                g.setClip(bounds);
                clipped[0] = true;
            }

            // translate into the components coordinate system and render
            g.translate(_lbounds.x, _lbounds.y);
            comp.paint(g);
            g.translate(-_lbounds.x, -_lbounds.y);
        }
    }

    // documentation inherited
    public void checkpoint (String name, int ticks)
    {
        Log.info("Frames in last second: " + ticks);
    }

    /**
     * Returns the root component for the supplied component or null if it
     * is not part of a rooted hierarchy or if any parent along the way is
     * found to be hidden or without a peer. Along the way, it adjusts the
     * supplied component-relative rectangle to be relative to the
     * returned root component.
     */
    public static Component getRoot (Component comp, Rectangle rect)
    {
	for (Component c = comp; c != null; c = c.getParent()) {
	    if (!c.isVisible() || !c.isDisplayable()) {
		return null;
	    }
            if (c instanceof Window || c instanceof Applet) {
		return c;
	    }
            rect.x += c.getX();
            rect.y += c.getY();
	}
        return null;
    }

    /**
     * An observer operation that calls {@link FrameParticipant#tick} with
     * a specified tick stamp for all {@link FrameParticipant} objects in
     * the observer list to which this operation is applied.
     */
    protected class ParticipantTickOp
        implements ObserverList.ObserverOp
    {
        /**
         * Sets the tick stamp to be applied to the participants.
         */
        public void setTickStamp (long tickStamp)
        {
            _tickStamp = tickStamp;
        }

        // documentation inherited
        public boolean apply (Object observer)
        {
            try {
                long start = 0L;
                if (HANG_DEBUG) {
                    start = System.currentTimeMillis();
                }

                ((FrameParticipant)observer).tick(_tickStamp);

                if (HANG_DEBUG) {
                    long delay = (System.currentTimeMillis() - start);
                    if (delay > HANG_GAP) {
                        Log.info("Whoa nelly! Ticker took a long time " +
                                 "[part=" + observer +
                                 ", time=" + delay + "ms].");
                    }
                }

            } catch (Throwable t) {
                Log.warning("Frame participant choked during tick " +
                            "[part=" +
                            StringUtil.safeToString(observer) + "].");
                Log.logStackTrace(t);
            }
            return true;
        }

        /** The tick stamp to be applied to each frame participant. */
        protected long _tickStamp;
    }

    /**
     * An observer operation that paints the components associated with
     * all {@link FrameParticipant} objects in the observer list to which
     * this operation is applied.
     */
    protected class ParticipantPaintOp
        implements ObserverList.ObserverOp
    {
        /**
         * Sets the graphics context to which the frame participants
         * render themselves.
         */
        public void init (Graphics2D g)
        {
            _g = g;
            _painted = 0;
        }

        /**
         * Returns true if we painted at least one component in our last
         * application.
         */
        public boolean paintedSomething ()
        {
            return (_painted > 0);
        }

        // documentation inherited
        public boolean apply (Object observer)
        {
            FrameParticipant part = (FrameParticipant)observer;
            Component pcomp = part.getComponent();
            if (pcomp == null || !part.needsPaint()) {
                return true;
            }

            long start = 0L;
            if (HANG_DEBUG) {
                start = System.currentTimeMillis();
            }

            // get the bounds of this component
            pcomp.getBounds(_bounds);

            // the bounds adjustment we're about to call will add in the
            // components initial bounds offsets, so we remove them here
            _bounds.setLocation(0, 0);

            // convert them into top-level coordinates; also note that if
            // this component does not have a valid or visible root, we
            // don't want to paint it either
            if (getRoot(pcomp, _bounds) == null) {
                return true;
            }

            try {
                // render this participant; we don't set the clip because
                // frame participants are expected to handle clipping
                // themselves; otherwise we might pointlessly set the clip
                // here, creating a few Rectangle objects in the process,
                // only to have the frame participant immediately set the
                // clip to something more sensible
                _g.translate(_bounds.x, _bounds.y);
                pcomp.paint(_g);
                _g.translate(-_bounds.x, -_bounds.y);
                _painted++;

            } catch (Throwable t) {
                String ptos = StringUtil.safeToString(part);
                Log.warning("Frame participant choked during paint " +
                            "[part=" + ptos + "].");
                Log.logStackTrace(t);
            }

            // render any components in our layered pane that are not in
            // the default layer
            _clipped[0] = false;
            renderLayers(_g, pcomp, _bounds, _clipped);

            if (HANG_DEBUG) {
                long delay = (System.currentTimeMillis() - start);
                if (delay > HANG_GAP) {
                    Log.warning("Whoa nelly! Painter took a long time " +
                                "[part=" + observer +
                                ", time=" + delay + "ms].");
                }
            }

            return true;
        }

        /** The graphics context to which the participants render. */
        protected Graphics2D _g;

        /** The number of participants that were actually painted. */
        protected int _painted;

        /** A handy rectangle that we reuse time and again to avoid having
         * to instantiate a new rectangle in the midst of the core
         * rendering loop. */
        protected Rectangle _bounds = new Rectangle();
    }

    /** Used to effect periodic calls to {@link #tick}. */
    protected class Ticker extends Thread
    {
        public void run ()
        {
            Log.info("Frame manager ticker running " +
                     "[sleepGran=" + _sleepGranularity.getValue() + "].");
            while (_running) {
                long start = 0L;
                if (MediaPanel._perfDebug.getValue()) {
                    start = _timer.getElapsedMillis();
                }
                Unsafe.sleep(_sleepGranularity.getValue());

                long woke = _timer.getElapsedMillis();
                if (start > 0L) {
                    getPerfMetrics()[0].record((int)(woke-start));
                }

                // work around sketchy bug on WinXP that causes the clock
                // to leap into the past from time to time
                if (woke < _lastAttempt) {
                    Log.warning("Zoiks! We've leapt into the past, coping " +
                                "as best we can [dt=" +
                                (woke - _lastAttempt) + "].");
                    _lastAttempt = woke;
                }

                if (woke - _lastAttempt >= _millisPerFrame) {
                    _lastAttempt = woke;
                    if (testAndSet()) {
                        EventQueue.invokeLater(_awtTicker);
                    }
                    // else: drop the frame
                }
            }
        }

        public void cancel ()
        {
            _running = false;
        }

        protected final synchronized boolean testAndSet ()
        {
            _tries++;
            if (!_ticking) {
                _ticking = true;
                return true;
            }
            return false;
        }

        protected final synchronized void clearTicking (long elapsed)
        {
            if (++_ticks == 100) {
                long time = (elapsed - _lastTick);
                _fps[0] = _tries * 1000f / time;
                _fps[1] = _ticks * 1000f / time;
                _lastTick = elapsed;
                _ticks = _tries = 0;
            }
            _ticking = false;
        }

        /** Used to invoke the call to {@link #tick} on the AWT event
         * queue thread. */
        protected Runnable _awtTicker = new Runnable ()
        {
            public void run ()
            {
                long elapsed = _timer.getElapsedMillis();
                try {
                    tick(elapsed);
                } finally {
                    clearTicking(elapsed);
                }

                // call currentTimeMillis so that we are notified ASAP if
                // time leaps into the past as it's useful for debugging
                // other problems
                RunAnywhere.currentTimeMillis();
            }
        };

        /** Used to stick a fork in our ticker when desired. */
        protected transient boolean _running = true;

        /** Used to detect when we need to drop frames. */
        protected boolean _ticking;

        /** The time at which we last attempted to tick. */
        protected long _lastAttempt;

        /** Used to compute metrics. */
        protected int _tries, _ticks, _time;

        /** Used to compute metrics. */
        protected long _lastTick;
    };

    /** The frame into which we do our rendering. */
    protected JFrame _frame;

    /** Used to obtain timing measurements. */
    protected MediaTimer _timer;

    /** Our custom repaint manager. */
    protected FrameRepaintManager _remgr;

    /** The number of milliseconds per frame (14 by default, which gives
     * an fps of ~71). */
    protected long _millisPerFrame = 14;

    /** Used to track big delays in calls to our tick method. */
    protected long _lastTickStamp;

    /** The thread that dispatches our frame ticks. */
    protected Ticker _ticker;

    /** Used to track and report frames per second. */
    protected float[] _fps = new float[2];

    /** Used to track performance metrics. */
    protected TrailingAverage[] _metrics;

    /** Used to avoid creating rectangles when rendering layered
     * components. */
    protected Rectangle _lbounds = new Rectangle();

    /** Used to lazily set the clip when painting popups and other
     * "layered" components. */
    protected boolean[] _clipped = new boolean[1];

    /** The entites that are ticked each frame. */
    protected ObserverList _participants =
        new ObserverList(ObserverList.FAST_UNSAFE_NOTIFY);

    /** The observer operation applied to all frame participants each tick. */
    protected ParticipantTickOp _participantTickOp = new ParticipantTickOp();

    /** The observer operation applied to all frame participants each time
     * the frame is rendered. */
    protected ParticipantPaintOp _participantPaintOp = new ParticipantPaintOp();

    /** If we don't get ticked for 500ms, that's worth complaining about. */
    protected static final long BIG_GAP = 500L;

    /** If we don't get ticked for 100ms and we're hang debugging,
     * complain. */
    protected static final long HANG_GAP = 100L;

    /** Enable this to log warnings when ticking or painting takes too
     * long. */
    protected static final boolean HANG_DEBUG = false;

    /** A debug hook that toggles debug rendering of sprite paths. */
    protected static RuntimeAdjust.BooleanAdjust _useFlip =
        new RuntimeAdjust.BooleanAdjust(
            "When active a flip-buffer will be used to manage our " +
            "rendering, otherwise a volatile back buffer is used " +
            "[requires restart]", "narya.media.frame",
            // back buffer rendering doesn't work on the Mac, so we
            // default to flip buffer on that platform; we still allow it
            // to be toggled so that we can easily test things when they
            // release new JVMs
            MediaPrefs.config, RunAnywhere.isMacOS());

    /** Allows us to tweak the sleep granularity. */
    protected static RuntimeAdjust.IntAdjust _sleepGranularity =
        new RuntimeAdjust.IntAdjust(
            "The number of milliseconds slept before checking to see if " +
            "it's time to queue up a new frame tick.", "narya.media.sleep_gran",
            MediaPrefs.config, RunAnywhere.isWindows() ? 10 : 7);

    /** Whether to enable AWT event debugging for the frame. */
    protected static final boolean DEBUG_EVENTS = false;

    /** The name of the high-performance timer class we attempt to load. */
    protected static final String PERF_TIMER =
        "com.threerings.media.timer.PerfTimer";
}
