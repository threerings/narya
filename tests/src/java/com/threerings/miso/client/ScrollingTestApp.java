//
// $Id: ScrollingTestApp.java,v 1.4 2002/02/19 07:21:15 mdb Exp $

package com.threerings.miso.scene;

import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.Config;

import com.threerings.resource.ResourceManager;
import com.threerings.media.ImageManager;

import com.threerings.media.sprite.SpriteManager;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.bundle.BundledTileSetRepository;

import com.threerings.miso.Log;
import com.threerings.miso.util.MisoContext;
import com.threerings.miso.util.MisoUtil;

/**
 * Tests the scrolling capabilities of the IsoSceneView.
 */
public class ScrollingTestApp
{
    /**
     * Construct and initialize the scrolling test app.
     */
    public ScrollingTestApp (String[] args)
        throws IOException
    {
        // get the graphics environment
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();

        // get the target graphics device
        GraphicsDevice gd = env.getDefaultScreenDevice();
        Log.info("Graphics device [dev=" + gd +
                 ", mem=" + gd.getAvailableAcceleratedMemory() +
                 ", displayChange=" + gd.isDisplayChangeSupported() +
                 ", fullScreen=" + gd.isFullScreenSupported() + "].");

        // get the graphics configuration and display mode information
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        DisplayMode dm = gd.getDisplayMode();
        Log.info("Display mode [bits=" + dm.getBitDepth() +
                 ", wid=" + dm.getWidth() + ", hei=" + dm.getHeight() +
                 ", refresh=" + dm.getRefreshRate() + "].");

        // create the window
	_frame = new ScrollingFrame(gc);

        // we don't need to configure anything
        _config = new Config();
        ResourceManager rmgr = new ResourceManager(
            "rsrc", null, "config/resource/manager.properties");
        ImageManager imgr = new ImageManager(rmgr, _frame);
	_tilemgr = new TileManager(imgr);
        _tilemgr.setTileSetRepository(
            new BundledTileSetRepository(rmgr, imgr, "tilesets"));

        // bind our miso properties
	_config.bindProperties("miso", "rsrc/config/miso/scrolling");

	// create the context object
	MisoContext ctx = new ContextImpl();

        // create the various managers
        SpriteManager spritemgr = new SpriteManager();

        // create our scene view panel
        _panel = new SceneViewPanel(new IsoSceneViewModel(_config));
        _frame.setPanel(_panel);

        // set the scene to our scrolling scene
        try {
            _panel.setScene(new ScrollingScene(ctx));
            _panel.setScrolling(30, -30);

        } catch (Exception e) {
            Log.warning("Error creating scene: " + e);
        }

        // size and position the window, entering full-screen exclusive
        // mode if available
        if (gd.isFullScreenSupported()) {
            Log.info("Entering full-screen exclusive mode.");
            gd.setFullScreenWindow(_frame);
            _frame.setUndecorated(true);

        } else {
            Log.warning("Full-screen exclusive mode not available.");
            _frame.pack();
            SwingUtil.centerWindow(_frame);
        }
    }

    /**
     * The implementation of the MisoContext interface that provides
     * handles to the config and manager objects that offer commonly used
     * services.
     */
    protected class ContextImpl implements MisoContext
    {
	public Config getConfig ()
	{
	    return _config;
	}

	public TileManager getTileManager ()
	{
	    return _tilemgr;
	}
    }

    /**
     * Run the application.
     */
    public void run ()
    {
        // show the window
        _frame.show();
    }

    /**
     * Instantiate the application object and start it running.
     */
    public static void main (String[] args)
    {
        try {
            ScrollingTestApp app = new ScrollingTestApp(args);
            app.run();
        } catch (IOException ioe) {
            System.err.println("Error initializing scrolling app.");
            ioe.printStackTrace();
        }
    }

    /** The desired width and height for the main application window. */
    protected static final int WIDTH = 800;
    protected static final int HEIGHT = 622;

    /** The config object. */
    protected Config _config;

    /** The tile manager object. */
    protected TileManager _tilemgr;

    /** The main application window. */
    protected ScrollingFrame _frame;

    /** The main panel. */
    protected SceneViewPanel _panel;
}
