//
// $Id: ScrollingTestApp.java,v 1.7 2002/03/08 22:37:50 mdb Exp $

package com.threerings.miso.scene;

import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;

import java.io.IOException;
import java.util.Iterator;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.Config;

import com.threerings.resource.ResourceManager;
import com.threerings.media.ImageManager;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.sprite.MultiFrameImageImpl;

import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.bundle.BundledTileSetRepository;

import com.threerings.cast.CharacterComponent;
import com.threerings.cast.CharacterDescriptor;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.NoSuchComponentException;
import com.threerings.cast.bundle.BundledComponentRepository;

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
        BundledComponentRepository crepo =
            new BundledComponentRepository(rmgr, imgr, "components");
        CharacterManager charmgr = new CharacterManager(crepo);
        charmgr.setCharacterClass(MisoCharacterSprite.class);

        // create our scene view panel
        _panel = new SceneViewPanel(new IsoSceneViewModel(_config));
        _frame.setPanel(_panel);

        // create our "ship" sprite
        String scclass = "navsail", scname = "smsloop";
        try {
            CharacterComponent ccomp = crepo.getComponent(scclass, scname);
            CharacterDescriptor desc = new CharacterDescriptor(
                new int[] { ccomp.componentId }, null);

            // now create the actual sprite and stick 'em in the scene
            MisoCharacterSprite s =
                (MisoCharacterSprite)charmgr.getCharacter(desc);
            if (s != null) {
                s.setRestingAction("sailing");
                s.setActionSequence("sailing");
                s.setLocation(160 - s.getWidth()/2, 144 + s.getHeight()/2);
                _panel.addSprite(s);
            }

        } catch (NoSuchComponentException nsce) {
            Log.warning("Can't locate ship component [class=" + scclass +
                        ", name=" + scname + "].");
        }

        // set the scene to our scrolling scene
        try {
            _panel.setScene(new ScrollingScene(ctx));
            _panel.setScrolling(90, 90);

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

    /** The config object. */
    protected Config _config;

    /** The tile manager object. */
    protected TileManager _tilemgr;

    /** The main application window. */
    protected ScrollingFrame _frame;

    /** The main panel. */
    protected SceneViewPanel _panel;
}
