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
import com.jme.input.InputHandler;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.system.PropertiesIO;
import com.jme.system.lwjgl.LWJGLPropertiesDialog;
import com.jme.util.Timer;

import com.threerings.presents.client.Client;

import com.threerings.jme.input.GodViewHandler;

/**
 * Defines a basic application framework providing integration with the
 * <a href="../presents/package.html">Presents</a> networking system and
 * targeting a fixed framerate.
 */
public class JmeApp
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
        _targetFrameTime = (float)_timer.getResolution() / framesPerSecond;
    }

    /**
     * Initializes this application and starts up the main rendering and
     * event processing loop. This method will not return until the
     * application is terminated with a call to {@link #stop}.
     */
    public void start ()
    {
        synchronized (this) {
            _dispatchThread = Thread.currentThread();
        }

        try {
            // load up our renderer configuration
            _properties = new PropertiesIO(getConfigPath("jme.cfg"));
            if (!_properties.load()) {
                LWJGLPropertiesDialog dialog =
                    new LWJGLPropertiesDialog(_properties, (String)null);
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
            _timer = Timer.getTimer(_properties.getRenderer());

            // default to 60 fps
            setTargetFrameRate(60);

            // initialize the rendering system
            initDisplay();
            if (!_display.isCreated()) {
                throw new IllegalStateException("Failed to initialize display?");
            }

            // initialize our main camera controls and user input handling
            initInput();

        } catch (Throwable t) {
            Log.logStackTrace(t);
            exit();
            return;
        }

        // enter the main rendering and event processing loop
        while (!_finished && !_display.isClosing()) {
            try {
                processFrame();
                _failures = 0;

            } catch (Throwable t) {
                Log.logStackTrace(t);
                // stick a fork in things if we fail too many times in a row
                if (++_failures > MAX_SUCCESSIVE_FAILURES) {
                    stop();
                }
            }
        }

        try {
            cleanup();
        } catch (Throwable t) {
            Log.logStackTrace(t);
        } finally {
            exit();
        }
    }

    /**
     * Instructs the application to stop the main loop, cleanup and exit.
     */
    public void stop ()
    {
        _finished = true;
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
     * Initializes the underlying rendering system, creating a display of
     * the proper resolution and depth.
     */
    protected void initDisplay ()
        throws JmeException
    {
        // create the main display system
        _display = DisplaySystem.getDisplaySystem(_properties.getRenderer());
        _display.createWindow(
            _properties.getWidth(), _properties.getHeight(),
            _properties.getDepth(), _properties.getFreq(),
            _properties.getFullscreen());

        // create a camera
        float width = _display.getWidth(), height = _display.getHeight();
        _camera = _display.getRenderer().createCamera((int)width, (int)height);

        // start with a black background
        _display.getRenderer().setBackgroundColor(ColorRGBA.black);

        // set up the camera
        _camera.setFrustumPerspective(45.0f, width / height, 1, 1000);
        Vector3f loc = new Vector3f(0.0f, 0.0f, 25.0f);
        Vector3f left = new Vector3f( -1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f dir = new Vector3f(0.0f, 0f, -1.0f);
        _camera.setFrame(loc, left, up, dir);
        _camera.update();
        _display.getRenderer().setCamera(_camera);

        // tell the renderer to keep track of rendering information (total
        // triangles drawn, etc.)
        _display.getRenderer().enableStatistics(true);

//         // set our display title
//         _display.setTitle("TBD");
    }

    /**
     * Sets up a main input controller to handle the camera and deal with
     * global user input.
     */
    protected void initInput ()
    {
        _input = new GodViewHandler(this, _camera, _properties.getRenderer());

//         /** Signal to all key inputs they should work Nx faster. */
//         input.setKeySpeed(150f);
//         input.setMouseSpeed(1f);
    }

    /**
     * Processes a single frame.
     */
    protected final void processFrame ()
    {
        float frameStart = _timer.getTime();

        // update our simulation and render a frame
        update(frameStart);
        render(frameStart);

        _display.getRenderer().displayBackBuffer();
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
        } while (_timer.getTime() - frameStart < _targetFrameTime);
    }

    /**
     * Called every frame to update whatever sort of real time business we
     * have that needs updating.
     */
    protected void update (float frameTime)
    {
        // recalculate the frame rate
        _timer.update();

        // update the input system
        float timePerFrame = _timer.getTimePerFrame();
        _input.update(timePerFrame);

        // run all of the controllers attached to nodes
        _root.updateGeometricState(timePerFrame, true);
    }

    /**
     * Called every frame to issue the rendering instructions for this frame.
     */
    protected void render (float frameTime)
    {
        // clear out our previous information
        _display.getRenderer().clearStatistics();
        _display.getRenderer().clearBuffers();

        // draw the root node and all of its children
        _display.getRenderer().draw(_root);

        // this would render bounding boxes
        // _display.getRenderer().drawBounds(_root);
    }

    /**
     * Called when the application is terminating cleanly after having
     * successfully completed initialization and begun the main loop.
     */
    protected void cleanup ()
    {
        _display.reset();
    }

    /**
     * Closes the display and exits the JVM process.
     */
    protected void exit ()
    {
        if (_display != null) {
            _display.close();
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
    protected boolean _finished;

    protected PropertiesIO _properties;
    protected DisplaySystem _display;
    protected Camera _camera;
    protected InputHandler _input;
    protected Node _root;

    protected int _failures;

    protected int _frames;
    protected float _lastFPSStamp;
    protected float _targetFrameTime;

    /** If we fail 100 frames in a row, stick a fork in ourselves. */
    protected static final int MAX_SUCCESSIVE_FAILURES = 100;
}
