//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.jme;

import java.io.File;

import com.samskivert.util.Queue;
import com.samskivert.util.RunQueue;
import com.samskivert.util.StringUtil;

import com.jme.app.AbstractGame;
import com.jme.system.PropertiesIO;
import com.jme.system.lwjgl.LWJGLPropertiesDialog;
import com.jme.util.Timer;

import com.threerings.presents.client.Client;

/**
 * Extends the {@link AbstractGame} providing integration with the
 * <a href="../presents/package.html">Presents</a> networking system and
 * targeting a fixed framerate.
 */
public abstract class JmeApp extends AbstractGame
    implements RunQueue
{
    /**
     * Returns our average frames per second since the last call to this
     * method.
     */
    public float getFramesPerSecond ()
    {
        float now = _timer.getTime(), delta = now - _lastFPSStamp;
        float fps = _frames / delta;
        _lastFPSStamp = now;
        _frames = 0;
        return fps;
    }

    /**
     * Configures the target frame rate in frames per second.
     */
    public void setTargetFrameRate (long framesPerSecond)
    {
        if (framesPerSecond <= 0) {
            throw new IllegalArgumentException("FPS must be > 0.");
        }
        _targetTicksPerFrame = _timer.getResolution() / framesPerSecond;
    }

    // documentation inherited
    public void start ()
    {
        synchronized (this) {
            _dispatchThread = Thread.currentThread();
        }

        try {
            // load up our renderer configuration
            properties = new PropertiesIO(getConfigPath("jme.cfg"));
            if (!properties.load()) {
                LWJGLPropertiesDialog dialog =
                    new LWJGLPropertiesDialog(properties, (String)null);
                while (dialog.isVisible()) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Log.warning("Error waiting for dialog system, " +
                                    "using defaults.");
                    }
                }
            }

            // create an appropriate timer
            _timer = Timer.getTimer(properties.getRenderer());

            // default to 60 fps
            setTargetFrameRate(60);

            // initialize the AbstractGame
            initSystem();
            assertDisplayCreated();

            // initialize our derived class
            initGame();

        } catch (Throwable t) {
            Log.logStackTrace(t);
            quit();
            return;
        }

        // enter the main rendering and event processing loop
        while (!finished && !display.isClosing()) {
            try {
                processFrame();
                _failures = 0;

            } catch (Throwable t) {
                Log.logStackTrace(t);
                // stick a fork in things if we fail too many times in a row
                if (++_failures > MAX_SUCCESSIVE_FAILURES) {
                    finish();
                }
            }
        }

        try {
            cleanup();
            display.reset();
        } catch (Throwable t) {
            Log.logStackTrace(t);
        } finally {
            quit();
        }
    }

    // documentation inherited from interface RunQueue
    public void postRunnable (Runnable r)
    {
        _evqueue.append(r);
    }

    // documentation inherited from interface RunQueue
    public boolean isDispatchThread ()
    {
        return Thread.currentThread() == _dispatchThread;
    }

    /**
     * Processes a single frame.
     */
    protected final void processFrame ()
    {
        _frameStart = _timer.getTime();

        // update our simulation and render a frame
        update(-1f);
        render(-1f);

        display.getRenderer().displayBackBuffer();
        _frames++;

        // now process events or sleep until the next frame
        do {
            Runnable r = (Runnable)_evqueue.getNonBlocking();
            if (r == null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ie) {
                    Log.warning("Ticker interrupted: " + ie);
                }
            } else {
                r.run();
            }
        } while (_timer.getTime() - _frameStart < _targetTicksPerFrame);
    }

    // documentation inherited
    protected void quit ()
    {
        if (display != null) {
            display.close();
        }
        System.exit(0);
    }

    /**
     * Prepends the necessary bits onto the supplied path to properly
     * locate it in our configuration directory.
     */
    protected String getConfigPath (String file)
    {
        String cfgdir = ".narya";
        String home = System.getProperty("user.dir");
        if (!StringUtil.blank(home)) {
            cfgdir = home + File.separator + cfgdir;
        }
        // create the configuration directory if it does not already exist
        File dir = new File(cfgdir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return cfgdir + File.separator + file;
    }

    protected Timer _timer;
    protected Thread _dispatchThread;
    protected Queue _evqueue = new Queue();

    protected int _failures;

    protected int _frames;
    protected float _lastFPSStamp;

    protected long _targetTicksPerFrame;
    protected long _frameStart;

    /** If we fail 100 frames in a row, stick a fork in ourselves. */
    protected static final int MAX_SUCCESSIVE_FAILURES = 100;
}
