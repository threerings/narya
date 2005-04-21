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

import com.jme.ui.UIFonts;
import com.jme.ui.UIColorScheme;

import com.jme.input.InputHandler;
import com.jme.input.InputSystem;

import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.util.Timer;

import com.threerings.presents.client.Client;

import com.threerings.jme.input.GodViewHandler;
import com.threerings.jme.input.HardwareMouse;

/**
 * Defines a basic application framework providing integration with the
 * <a href="../presents/package.html">Presents</a> networking system and
 * targeting a fixed framerate.
 */
public class JmeApp
    implements RunQueue
{
    /**
     * Configures the target frame rate in frames per second.
     */
    public void setTargetFrameRate (long framesPerSecond)
    {
        if (framesPerSecond <= 0) {
            throw new IllegalArgumentException("FPS must be > 0.");
        }
        _targetFrameTicks = _timer.getResolution() / framesPerSecond;
    }

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
     */
    public void init ()
    {
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

        // initialize the root node
        initRoot();

        // initialize the lighting
        initLighting();

        // initialize the UI support stuff
        initInterface();

        // update everything for the zeroth tick
        _root.updateGeometricState(0f, true);
        _root.updateRenderState();

        // create and add our statistics display
        if (displayStatistics()) {
            _stats = new StatsDisplay(_display.getRenderer());
            _stats.updateGeometricState(0f, true);
            _stats.updateRenderState();
        }
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
        _bufferedInput = new InputHandler();

        // we don't hide the cursor
        InputSystem.getMouseInput().setCursorVisible(true);
        HardwareMouse mouse = new HardwareMouse("Mouse");
        mouse.setMouseInput(InputSystem.getMouseInput());
        _input.setMouse(mouse);
        _bufferedInput.setMouse(mouse);
    }

    /**
     * Creates our root node and sets up the basic rendering system.
     */
    protected void initRoot ()
    {
        _root = new Node("Root");

        // set up a zbuffer
        ZBufferState zbuf = _display.getRenderer().createZBufferState();
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.CF_LEQUAL);
        _root.setRenderState(zbuf);
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
        _root.setRenderState(_lights);
    }

    /**
     * Initializes our user interface bits.
     */
    protected void initInterface ()
    {
        String[] names = { "main" };
        String[] locs = { StatsDisplay.DEFAULT_JME_FONT };
        _fonts = new UIFonts(names, locs);
        _colorScheme = new UIColorScheme();
    }

    /**
     * Processes a single frame.
     */
    protected final void processFrame ()
    {
        // update our simulation and render a frame
        long frameStart = _timer.getTime();
        update(frameStart);
        render(frameStart);

        _display.getRenderer().displayBackBuffer();

        // now process events or sleep until the next frame (assume zero
        // frame duration to start to ensure that we always process at
        // least one event per frame)
        long frameDuration = 0L;
        while (frameDuration < _targetFrameTicks) {
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
            frameDuration = _timer.getTime() - frameStart;
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

        // update the input system
        float timePerFrame = _timer.getTimePerFrame();
        _input.update(timePerFrame);
        _bufferedInput.update(timePerFrame);

        // run all of the controllers attached to nodes
        _root.updateGeometricState(timePerFrame, true);

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

        if (InputSystem.getKeyInput() != null) {
            InputSystem.getKeyInput().destroy();
        }
        if (InputSystem.getMouseInput() != null) {
            InputSystem.getMouseInput().destroy();
        }
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
     * If true we'll display some renderer statistics at the bottom of the
     * screen.
     */
    protected boolean displayStatistics ()
    {
        return true;
    }

    /**
     * Prepends the necessary bits onto the supplied path to properly
     * locate it in our configuration directory.
     */
    protected String getConfigPath (String file)
    {
        String cfgdir = ".narya";
        String home = System.getProperty("user.home");
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

    /** Provides access to various needed bits. */
    protected JmeContext _ctx = new JmeContext() {
        public Renderer getRenderer () {
            return _display.getRenderer();
        }

        public Node getRoot () {
            return _root;
        }

        public InputHandler getInputHandler () {
            return _input;
        }

        public InputHandler getBufferedInputHandler () {
            return _bufferedInput;
        }

        public UIColorScheme getColorScheme () {
            return _colorScheme;
        }

        public UIFonts getFonts () {
            return _fonts;
        }
    };

    protected Timer _timer;
    protected Thread _dispatchThread;
    protected Queue _evqueue = new Queue();

    protected PropertiesIO _properties;
    protected DisplaySystem _display;
    protected Camera _camera;
    protected InputHandler _input;
    protected InputHandler _bufferedInput;

    protected long _targetFrameTicks;
    protected boolean _finished;
    protected int _failures;

    protected Node _root;
    protected LightState _lights;
    protected StatsDisplay _stats;

    protected UIColorScheme _colorScheme;
    protected UIFonts _fonts;

    /** If we fail 100 frames in a row, stick a fork in ourselves. */
    protected static final int MAX_SUCCESSIVE_FAILURES = 100;
}
