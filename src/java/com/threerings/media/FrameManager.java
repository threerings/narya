//
// $Id: FrameManager.java,v 1.23 2002/11/22 01:53:39 mdb Exp $

package com.threerings.media;

import java.applet.Applet;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.image.BufferStrategy;
import java.awt.image.VolatileImage;

import java.awt.EventQueue;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.RepaintManager;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;
import com.samskivert.util.ObserverList;
import com.samskivert.util.StringUtil;

import com.threerings.media.timer.MediaTimer;
import com.threerings.media.timer.SystemMediaTimer;

import com.threerings.media.util.PerformanceMonitor;
import com.threerings.media.util.PerformanceObserver;

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
 * (the frame) is painted (via a call to {@link Frame#paint}) into a flip
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
public class FrameManager
    implements PerformanceObserver
{
    /**
     * Creates a frame manager that will use a {@link SystemMediaTimer} to
     * obtain timing information, which is available on every platform,
     * but returns inaccurate time stamps on many platforms.
     *
     * @see #FrameManager(Frame)
     */
    public FrameManager (Frame frame)
    {
        this(frame, new SystemMediaTimer());
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
    public FrameManager (Frame frame, MediaTimer timer)
    {
        _frame = frame;
        _frame.setIgnoreRepaint(true);
        _timer = timer;

        // set up our custom repaint manager
        _remgr = new FrameRepaintManager(_frame);
        RepaintManager.setCurrentManager(_remgr);

        // turn off double buffering for the whole business because we
        // handle repaints
        _remgr.setDoubleBufferingEnabled(false);

        // register with the performance monitor
        PerformanceMonitor.register(this, "frame-rate", 1000l);
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
     * Removes a frame participant.
     */
    public void removeFrameParticipant (FrameParticipant participant)
    {
        _participants.remove(participant);
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
            _ticker = new Timer(true);
            _ticker.scheduleAtFixedRate(_callTick, new Date(), _millisPerFrame);
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
     * Returns true if we are in the middle of a call to {@link #tick}.
     */
    protected synchronized boolean isTicking ()
    {
        return _ticking;
    }

    /**
     * Called to perform the frame processing and rendering.
     */
    protected void tick (long tickStamp)
    {
        try {
            synchronized (this) {
                _ticking = true;
            }

            // if our frame is not showing (or is impossibly sized), don't try
            // rendering anything
            if (_frame.isShowing() &&
                _frame.getWidth() > 0 && _frame.getHeight() > 0) {
                // tick our participants
                tickParticipants(tickStamp);
                // repaint our participants
                paintParticipants(tickStamp);
            }

            // note that we've done a frame
//             PerformanceMonitor.tick(this, "frame-rate");

        } finally {
            synchronized (this) {
                _ticking = false;
            }
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
     * our frame participants' components.
     */
    protected void paintParticipants (long tickStamp)
    {
//         // create our buffer strategy if we don't already have one
//         if (_bufstrat == null) {
//             _frame.createBufferStrategy(2);
//             _bufstrat = _frame.getBufferStrategy();
//         }

        // start out assuming we can do an incremental render
        boolean incremental = true;

        do {
            GraphicsConfiguration gc = _frame.getGraphicsConfiguration();

            // create our off-screen buffer if necessary
            if (_backimg == null) {
                createBackBuffer(gc);
            }

            // make sure our back buffer hasn't disappeared
            int valres = _backimg.validate(gc);

            // if we've changed resolutions, recreate the buffer
            if (valres == VolatileImage.IMAGE_INCOMPATIBLE) {
//                 Log.info("Back buffer incompatible, recreating.");
                createBackBuffer(gc);
            }

            // if the image wasn't A-OK, we need to rerender the whole
            // business rather than just the dirty parts
            if (valres != VolatileImage.IMAGE_OK) {
//                 Log.info("Lost back buffer, redrawing.");
                incremental = false;
            }

//             g = _bufstrat.getDrawGraphics();

            // dirty everything if we're not incrementally rendering
            if (!incremental) {
                _frame.update(_bgfx);
            }

            // paint our frame participants (which want to be handled
            // specially)
            _participantPaintOp.setGraphics(_bgfx);
            _participants.apply(_participantPaintOp);

            // repaint any widgets that have declared there need to be
            // repainted since the last tick
            _remgr.paintComponents(_bgfx, this);

            // we cache our frame's graphics object so that we can avoid
            // instantiating a new one on every tick
            if (_fgfx == null) {
                _fgfx = _frame.getGraphics();
            }
            _fgfx.drawImage(_backimg, 0, 0, null);

//             _bufstrat.show();

            // if we loop through a second time, we'll need to rerender
            // everything
            incremental = false;

        } while (_backimg.contentsLost());
    }

    /**
     * Renders all components in all {@link JLayeredPane} layers that
     * intersect the supplied bounds.
     */
    protected void renderLayers (Graphics g, Component pcomp, Rectangle bounds, 
                                 boolean[] clipped)
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
    protected void renderLayer (Graphics g, Rectangle bounds, JLayeredPane pane,
                                boolean[] clipped, Integer layer)
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
     * Creates the off-screen buffer used to perform double buffered
     * rendering of the animated panel.
     */
    protected void createBackBuffer (GraphicsConfiguration gc)
    {
        // if we have an old image, clear it out
        if (_backimg != null) {
            _backimg.flush();
            _bgfx.dispose();
        }

        // create the offscreen buffer
        int width = _frame.getWidth(), height = _frame.getHeight();
        _backimg = gc.createCompatibleVolatileImage(width, height);

        // fill the back buffer with white
        _bgfx = _backimg.getGraphics();
        _bgfx.fillRect(0, 0, width, height);

        // clear out our frame graphics in case that became invalid for
        // the same reasons our back buffer became invalid
        _fgfx = null;
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
        public void setGraphics (Graphics g)
        {
            _g = g;
        }

        // documentation inherited
        public boolean apply (Object observer)
        {
            FrameParticipant part = (FrameParticipant)observer;
            Component pcomp = part.getComponent();
            if (pcomp == null) {
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
        protected Graphics _g;

        /** A handy rectangle that we reuse time and again to avoid having
         * to instantiate a new rectangle in the midst of the core
         * rendering loop. */
        protected Rectangle _bounds = new Rectangle();
    }

    /** The frame into which we do our rendering. */
    protected Frame _frame;

    /** Used to obtain timing measurements. */
    protected MediaTimer _timer;

    /** Our custom repaint manager. */
    protected FrameRepaintManager _remgr;

//     /** The buffer strategy used to do our rendering. */
//     protected BufferStrategy _bufstrat;

    /** The image used to render off-screen. */
    protected VolatileImage _backimg;

    /** The number of milliseconds per frame (14 by default, which gives
     * an fps of ~71). */
    protected long _millisPerFrame = 14;

    /** Used to track big delays in calls to our tick method. */
    protected long _lastTickStamp;

    /** The timer that dispatches our frame ticks. */
    protected Timer _ticker;

    /** Used to detect when we need to drop frames. */
    protected boolean _ticking;

    /** The graphics object from our back buffer. */
    protected Graphics _bgfx;

    /** The graphics object from our frame. */
    protected Graphics _fgfx;

    /** Used to avoid creating rectangles when rendering layered
     * components. */
    protected Rectangle _lbounds = new Rectangle();

    /** Used to lazily set the clip when painting popups and other
     * "layered" components. */
    protected boolean[] _clipped = new boolean[1];

    /** Used to effect periodic calls to {@link #tick}. */
    protected TimerTask _callTick = new TimerTask () {
        public void run () {
            if (EventQueue.isDispatchThread()) {
                tick(_timer.getElapsedMillis());
            } else if (!isTicking()) {
                EventQueue.invokeLater(this);
            } else {
                // drop the frame
            }
        }
    };

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
}
