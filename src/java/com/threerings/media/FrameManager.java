//
// $Id: FrameManager.java,v 1.3 2002/04/27 02:33:14 mdb Exp $

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
import javax.swing.RepaintManager;

import java.util.ArrayList;

import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;
import com.samskivert.util.StringUtil;

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
 * some of which may be {@link FrameParticipants} and will have prepared
 * themselves for their upcoming painting in the previous call to {@link
 * FrameParticipant#tick}. When the call to paint completes, the flip
 * buffer is flipped and the process starts all over again.
 * </ul>
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
 */
public class FrameManager
    implements PerformanceObserver
{
    /**
     * Constructs a frame manager that will do its rendering to the
     * supplied frame. It is likely that the caller will want to have put
     * the frame into full-screen exclusive mode prior to providing it to
     * the frame manager so that the frame manager can take advantage of
     * optimizations available in that mode.
     *
     * @see GraphicsDevice#setFullScreenWindow
     */
    public FrameManager (Frame frame)
    {
        _frame = frame;
        _frame.setIgnoreRepaint(true);

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
     * Starts up the per-frame tick
     */
    public void start ()
    {
        if (_ticker == null) {
            // create ticker for queueing up tick requests on AWT thread
            _ticker = new Ticker();
            // and start it up
            _ticker.start();
            // and kick off our first frame
            _ticker.tickIn(_millisPerFrame, System.currentTimeMillis());
        }
    }

    /**
     * Stops the per-frame tick.
     */
    public synchronized void stop ()
    {
        if (_ticker != null) {
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
     * Called to perform the frame processing and rendering.
     */
    protected void tick ()
    {
        // if our frame is not showing (or is impossibly sized), don't try
        // rendering anything
        if (_frame.isShowing() &&
            _frame.getWidth() > 0 && _frame.getHeight() > 0) {
            long tickStamp = System.currentTimeMillis();
            // tick our participants
            tickParticipants(tickStamp);
            // repaint our participants
            paintParticipants(tickStamp);
        }

        // now determine how many milliseconds we have left before we need
        // to start the next frame (if any)
        long end = System.currentTimeMillis();
        long duration = end - _frameStart;
        long remaining = _millisPerFrame - duration;

        // note that we've done a frame
        PerformanceMonitor.tick(this, "frame-rate");

        // if we have no time remaining, queue up another tick immediately
        if (remaining <= 0) {
            // make a note that we're starting our next frame now
            _frameStart = end;
            EventQueue.invokeLater(_callTick);

        } else {
            // otherwise queue one up in the requisite number of millis
            _ticker.tickIn(remaining, end);
        }
    }

    /**
     * Called once per frame to invoke {@link FrameParticipant#tick} on
     * all of our frame participants.
     */
    protected void tickParticipants (long tickStamp)
    {
        // tick all of our frame participants
        int pcount = _participants.size();
        for (int ii = 0; ii < pcount; ii++) {
            FrameParticipant part = (FrameParticipant)
                _participants.get(ii);
            try {
                part.tick(tickStamp);
            } catch (Throwable t) {
                Log.warning("Frame participant choked during tick " +
                            "[part=" +
                            StringUtil.safeToString(part) + "].");
                Log.logStackTrace(t);
            }
        }

        // validate any invalid components
        try {
            _remgr.validateComponents();
        } catch (Throwable t) {
            Log.warning("Failure validating components.");
            Log.logStackTrace(t);
        }
    }        

    /**
     * Called once per frame to invoke {@link FrameParticipant#paint} on
     * all of our frame participants.
     */
    protected void paintParticipants (long tickStamp)
    {
        // create our buffer strategy if we don't already have one
        if (_bufstrat == null) {
            _frame.createBufferStrategy(2);
            _bufstrat = _frame.getBufferStrategy();
        }

        // create our off-screen buffer if necessary
        GraphicsConfiguration gc = _frame.getGraphicsConfiguration();
        if (_backimg == null) {
            createBackBuffer(gc);
        }

        // render into our back buffer
        do {
            // make sure our back buffer hasn't disappeared
            int valres = _backimg.validate(gc);

            // if we've changed resolutions, recreate the buffer
            if (valres == VolatileImage.IMAGE_INCOMPATIBLE) {
                Log.info("Back buffer incompatible, recreating.");
                createBackBuffer(gc);
            }

            Rectangle bounds = new Rectangle();
            Graphics g = null, fg = null;
            Insets fi = _frame.getInsets();
            try {
                g = _backimg.getGraphics();
                fg = _frame.getGraphics();

                // if the image wasn't A-OK, we need to rerender the
                // whole business rather than just the dirty parts
                if (valres != VolatileImage.IMAGE_OK) {
                    Log.info("Lost back buffer, redrawing.");
                }

                // repaint any widgets that have declared there need to be
                // repainted since the last tick
                _remgr.paintComponents(g);

                // paint our frame participants (which want to be handled
                // specially)
                int pcount = _participants.size();
                for (int ii = 0; ii < pcount; ii++) {
                    FrameParticipant part = (FrameParticipant)
                        _participants.get(ii);
                    Component pcomp = part.getComponent();
                    if (pcomp == null) {
                        continue;
                    }

                    // get the bounds of this component
                    pcomp.getBounds(bounds);

                    // the bounds adjustment we're about to call will add
                    // in the components initial bounds offsets, so we
                    // remove them here
                    bounds.setLocation(0, 0);

                    // convert them into top-level coordinates; also note
                    // that if this component does not have a valid or
                    // visible root, we don't want to paint it either
                    if (getRoot(pcomp, bounds) == null) {
                        continue;
                    }

                    try {
                        // render this participant
//                         Log.info("Rendering [comp=" + pcomp.getClass().getName() +
//                                  ", bounds=" + StringUtil.toString(bounds) + "].");

                        g.setClip(bounds);
                        g.translate(bounds.x, bounds.y);
                        pcomp.paint(g);
                        g.translate(-bounds.x, -bounds.y);

//                         // copy the off-screen buffer on-screen
//                         fg.setClip(bounds);
//                         fg.drawImage(_backimg, 0, 0, null);

                    } catch (Throwable t) {
                        String ptos = StringUtil.safeToString(part);
                        Log.warning("Frame participant choked during paint " +
                                    "[part=" + ptos + "].");
                        Log.logStackTrace(t);
                    }
                }

                // Log.info("insets: " + fi + ", fb: " + _frame.getBounds());
                // _frame.paint(g);
                fg.drawImage(_backimg, 0, 0, null);

            } finally {
                if (g != null) {
                    g.dispose();
                }
                if (fg != null) {
                    fg.dispose();
                }
            }

        } while (_backimg.contentsLost());

//         Graphics g = null;
//         try {
//             g = _bufstrat.getDrawGraphics();
//             _frame.paint(g);
//             _bufstrat.show();

//         } catch (Throwable t) {
//             Log.warning("Frame rendering choked.");
//             Log.logStackTrace(t);

//         } finally {
//             if (g != null) {
//                 g.dispose();
//             }
//         }
    }

    // documentation inherited
    public void checkpoint (String name, int ticks)
    {
//         Log.info("Frames in last second: " + ticks);
    }

    /**
     * Used to queue up frame ticks on the AWT thread at some point in the
     * future.
     */
    protected class Ticker extends Thread
    {
        /**
         * Tells the ticker to queue up a frame in the requisite number of
         * milliseconds.
         */
        public synchronized void tickIn (long millis, long now)
        {
            _sleepfor = millis;
            _now = now;
            this.notify();
        }

        public void run ()
        {
            synchronized (this) {
                while (_sleepfor != -1) {
                    try {
                        if (_sleepfor == 0) {
                            this.wait();
                        }
                        if (_sleepfor > 0) {
                            Thread.sleep(_sleepfor);

                            // make a note of our frame start time
                            _frameStart = System.currentTimeMillis();
//                             long error =_frameStart - (_sleepfor + _now);
//                             if (Math.abs(error) > 3) {
//                                 Log.warning("Funny business: " + error);
//                             }

                            // queue up our ticker on the AWT thread
                            EventQueue.invokeLater(_callTick);
                            _sleepfor = 0;
                        }

                    } catch (InterruptedException ie) {
                        Log.warning("Girl interrupted!");
                    }
                }
            }
        }

        protected long _sleepfor = 0l;
        protected long _now = 0l;
    }

    /**
     * Creates the off-screen buffer used to perform double buffered
     * rendering of the animated panel.
     */
    protected void createBackBuffer (GraphicsConfiguration gc)
    {
        _backimg = gc.createCompatibleVolatileImage(
            _frame.getWidth(), _frame.getHeight());
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
	    if (!c.isVisible() || c.getPeer() == null) {
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

    /** The frame into which we do our rendering. */
    protected Frame _frame;

    /** Our custom repaint manager. */
    protected FrameRepaintManager _remgr;

    /** The buffer strategy used to do our rendering. */
    protected BufferStrategy _bufstrat;

    /** The image used to render off-screen. */
    protected VolatileImage _backimg;

    /** The number of milliseconds per frame (33 by default, which gives
     * an fps of 30). */
    protected long _millisPerFrame = 33;

    /** The time at which we started the most recent "frame". */
    protected long _frameStart;

    /** Used to queue up a tick. */
    protected Ticker _ticker;

    /** Used to queue up a call to {@link #tick} on the AWT thread. */
    protected Runnable _callTick = new Runnable () {
        public void run () {
            tick();
        }
    };

    /** The entites that are ticked each frame. */
    protected ArrayList _participants = new ArrayList();
}
