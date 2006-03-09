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

import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;

import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import com.jme.scene.state.ZBufferState;

import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.system.PropertiesIO;
import com.jme.system.lwjgl.LWJGLPropertiesDialog;

import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.Mouse;
import com.jme.input.MouseInput;
import com.jmex.bui.BRootNode;
import com.jmex.bui.PolledRootNode;

import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.util.Timer;

import com.threerings.jme.camera.CameraHandler;

/**
 * Defines a basic application framework providing integration with the
 * <a href="../presents/package.html">Presents</a> networking system and
 * targeting the framerate of the display.
 */
public class JmeApp
    implements RunQueue
{
    /**
     * Returns a context implementation that provides access to all the
     * necessary bits.
     */
    public JmeContext getContext ()
    {
        return _ctx;
    }

    /**
     * Does the main initialization of the application. This method should
     * be called first, and then the {@link #run} method should be called
     * to begin the rendering/event loop. Derived classes can override
     * this, being sure to call super before doing their own
     * initalization.
     *
     * @return true if the application initialized successfully, false if
     * initialization failed. (See {@link #reportInitFailure}.)
     */
    public boolean init ()
    {
        try {
            // initialize the rendering system
            initDisplay();
            if (!_display.isCreated()) {
                throw new IllegalStateException(
                    "Failed to initialize display?");
            }

            // create an appropriate timer
            _timer = Timer.getTimer(_api);

            // initialize our main camera controls and user input handling
            initInput();

            // initialize the root node
            initRoot();

            // initialize the lighting
            initLighting();

            // initialize the UI support stuff
            initInterface();

            // update everything for the zeroth tick
            _iface.updateRenderState();
            _geom.updateRenderState();
            _root.updateGeometricState(0f, true);
            _root.updateRenderState();

            return true;

        } catch (Throwable t) {
            reportInitFailure(t);
            return false;
        }
    }

    /**
     * Configures whether or not we display FPS and other statistics atop the
     * display.
     */
    public void displayStatistics (boolean display)
    {
        if (display && (_stats == null)) {
            _stats = new StatsDisplay(_display.getRenderer());
            _stats.updateGeometricState(0f, true);
            _stats.updateRenderState();
        } else if (!display && (_stats != null)) {
            _stats = null;
        }
    }

    /**
     * Returns true if we are displaying statistics, false if not.
     */
    public boolean showingStatistics ()
    {
        return (_stats != null);
    }

    /**
     * Starts up the main rendering and event processing loop. This method
     * will not return until the application is terminated with a call to
     * {@link #stop}.
     */
    public void run ()
    {
        synchronized (this) {
            _dispatchThread = Thread.currentThread();
        }

        // enter the main rendering and event processing loop
        while (!_finished && !_display.isClosing()) {
            try {
                long frameStart = processFrame();
                processEvents(frameStart);
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
     * Returns the duration (in seconds) between the previous frame and the
     * current frame.
     */
    public float getFrameTime ()
    {
        return _frameTime;
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
     * Determines the display configuration and creates the display. This must
     * fill in {@link #_api} as a side-effect.
     */
    protected DisplaySystem createDisplay ()
    {
        PropertiesIO props = new PropertiesIO(getConfigPath("jme.cfg"));
        if (!props.load()) {
            LWJGLPropertiesDialog dialog =
                new LWJGLPropertiesDialog(props, (String)null);
            while (dialog.isVisible()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Log.warning("Error waiting for dialog system, " +
                                "using defaults.");
                }
            }
        }
        _api = props.getRenderer();
        DisplaySystem display = DisplaySystem.getDisplaySystem(_api);
        display.createWindow(props.getWidth(), props.getHeight(),
                             props.getDepth(), props.getFreq(),
                             props.getFullscreen());
        return display;
    }

    /**
     * Initializes the underlying rendering system, creating a display of
     * the proper resolution and depth.
     */
    protected void initDisplay ()
        throws JmeException
    {
        // create the main display system
        _display = createDisplay();

        // create a camera
        int width = _display.getWidth(), height = _display.getHeight();
        _camera = _display.getRenderer().createCamera(width, height);

        // start with a black background
        _display.getRenderer().setBackgroundColor(ColorRGBA.black);

        // set up the camera
        _camera.setFrustumPerspective(45.0f, width/(float)height, 1, 10000);
        Vector3f loc = new Vector3f(0.0f, 0.0f, 25.0f);
        Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f dir = new Vector3f(0.0f, 0f, -1.0f);
        _camera.setFrame(loc, left, up, dir);
        _camera.update();
        _display.getRenderer().setCamera(_camera);

        // tell the renderer to keep track of rendering information (total
        // triangles drawn, etc.)
        _display.getRenderer().enableStatistics(true);
    }

    /**
     * Sets up a main input controller to handle the camera and deal with
     * global user input.
     */
    protected void initInput ()
    {
        _camhand = createCameraHandler(_camera);
        _input = createInputHandler(_camhand);
    }

    /**
     * Creates the camera handler which provides various camera manipulation
     * functionality.
     */
    protected CameraHandler createCameraHandler (Camera camera)
    {
        return new CameraHandler(camera);
    }

    /**
     * Creates the input handler used to control our camera and manage non-UI
     * keyboard input.
     */
    protected InputHandler createInputHandler (CameraHandler hand)
    {
        return new InputHandler();
    }

    /**
     * Creates our root node and sets up the basic rendering system.
     */
    protected void initRoot ()
    {
        _root = new Node("Root");

        // set up a node for our geometry
        _geom = new Node("Geometry");

        // set up a zbuffer
        ZBufferState zbuf = _display.getRenderer().createZBufferState();
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.CF_LEQUAL);
        _geom.setRenderState(zbuf);
        _root.attachChild(_geom);
    }

    /**
     * Sets up some default lighting.
     */
    protected void initLighting ()
    {
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(100, 100, 100));
        light.setEnabled(true);

        _lights = _display.getRenderer().createLightState();
        _lights.setEnabled(true);
        _lights.attach(light);
        _geom.setRenderState(_lights);
    }

    /**
     * Initializes our user interface bits.
     */
    protected void initInterface ()
    {
        // set up a node for our interface
        _iface = new Node("Interface");
        _root.attachChild(_iface);

        _rnode = new PolledRootNode(_timer, _input);
        _iface.attachChild(_rnode);
        // we don't hide the cursor
        MouseInput.get().setCursorVisible(true);
    }

    /**
     * Called when initialization fails to give the application a chance
     * to report the failure to the user.
     */
    protected void reportInitFailure (Throwable t)
    {
        Log.logStackTrace(t);
    }

    /**
     * Processes a single frame.
     */
    protected final long processFrame ()
    {
        // update our simulation and render a frame
        long frameStart = _timer.getTime();
        update(frameStart);
        render(frameStart);

        _display.getRenderer().displayBackBuffer();
        return frameStart;
    }

    /**
     * Processes as many events as possible until it is time to display
     * the next frame.
     */
    protected void processEvents (long frameStart)
    {
        Runnable r;
        int events = 0;
        while ((r = (Runnable)_evqueue.getNonBlocking()) != null) {
            r.run();
            // only process at most two events per frame
            if (++events > 1) {
                break;
            }
        }
    }

    /**
     * Called every frame to update whatever sort of real time business we
     * have that needs updating.
     */
    protected void update (long frameTick)
    {
        // recalculate the frame rate
        _timer.update();

        // run all of the controllers attached to nodes
        _frameTime = (_lastTick == 0L) ? 0f : (float)(frameTick - _lastTick) /
            _timer.getResolution();
        _lastTick = frameTick;
        _root.updateGeometricState(_frameTime, true);

        // update the camera handler
        _camhand.update(_frameTime);

        // update our stats display if we have one
        if (_stats != null) {
            _stats.update(_timer, _display.getRenderer());
        }
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

        // draw our stats atop everything
        if (_stats != null) {
            _display.getRenderer().draw(_stats);
        }
    }

    /**
     * Called when the application is terminating cleanly after having
     * successfully completed initialization and begun the main loop.
     */
    protected void cleanup ()
    {
        _display.reset();
        KeyInput.destroyIfInitalized();
        MouseInput.destroyIfInitalized();
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
        String home = System.getProperty("user.home");
        if (!StringUtil.isBlank(home)) {
            cfgdir = home + File.separator + cfgdir;
        }
        // create the configuration directory if it does not already exist
        File dir = new File(cfgdir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return cfgdir + File.separator + file;
    }

    /** Provides access to various needed bits. */
    protected JmeContext _ctx = new JmeContext() {
        public DisplaySystem getDisplay () {
            return _display;
        }

        public Renderer getRenderer () {
            return _display.getRenderer();
        }

        public CameraHandler getCameraHandler () {
            return _camhand;
        }

        public Node getGeometry () {
            return _geom;
        }

        public Node getInterface () {
            return _iface;
        }

        public InputHandler getInputHandler () {
            return _input;
        }

        public BRootNode getRootNode () {
            return _rnode;
        }
    };

    protected Timer _timer;
    protected Thread _dispatchThread;
    protected Queue _evqueue = new Queue();
    protected long _lastTick;
    protected float _frameTime;

    protected String _api;
    protected DisplaySystem _display;
    protected Camera _camera;
    protected CameraHandler _camhand;

    protected InputHandler _input;
    protected BRootNode _rnode;

    protected boolean _finished;
    protected int _failures;

    protected Node _root, _geom, _iface;
    protected LightState _lights;
    protected StatsDisplay _stats;

    /** If we fail 100 frames in a row, stick a fork in ourselves. */
    protected static final int MAX_SUCCESSIVE_FAILURES = 100;
}
