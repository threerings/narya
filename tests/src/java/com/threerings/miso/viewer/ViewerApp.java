//
// $Id: ViewerApp.java,v 1.23 2002/01/08 22:16:59 shaper Exp $

package com.threerings.miso.viewer;

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

import com.threerings.cast.CharacterManager;
import com.threerings.cast.bundle.BundledComponentRepository;

import com.threerings.miso.Log;
import com.threerings.miso.scene.DisplayMisoSceneImpl;
import com.threerings.miso.scene.MisoCharacterSprite;
import com.threerings.miso.scene.MisoSceneModel;
import com.threerings.miso.tools.scene.xml.MisoSceneParser;
import com.threerings.miso.util.MisoContext;
import com.threerings.miso.util.MisoUtil;

/**
 * The ViewerApp is a scene viewing application that allows for trying
 * out game scenes in a pseudo-runtime environment.
 */
public class ViewerApp
{
    /**
     * Construct and initialize the ViewerApp object.
     */
    public ViewerApp (String[] args)
        throws IOException
    {
        if (args.length < 1) {
            System.err.println("Usage: ViewerApp scene_file.xml");
            System.exit(-1);
        }

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
	_frame = new ViewerFrame(gc);

        // we don't need to configure anything
        _config = new Config();
        ResourceManager rmgr = new ResourceManager(null, "rsrc");
        ImageManager imgr = new ImageManager(rmgr, _frame);
	_tilemgr = new TileManager(imgr);
        _tilemgr.setTileSetRepository(
            new BundledTileSetRepository(rmgr, imgr, "tilesets"));

        // bind our miso properties
        MisoUtil.bindProperties(_config);

	// create the context object
	MisoContext ctx = new ContextImpl();

        // create the various managers
        SpriteManager spritemgr = new SpriteManager();
        BundledComponentRepository crepo =
            new BundledComponentRepository(rmgr, imgr, "components");
        CharacterManager charmgr = new CharacterManager(crepo);
        charmgr.setCharacterClass(MisoCharacterSprite.class);

        // create our scene view panel
        _panel = new ViewerSceneViewPanel(ctx, spritemgr, charmgr, crepo);
        _frame.setPanel(_panel);

        // load up the scene specified by the user
        try {
            MisoSceneParser parser = new MisoSceneParser("miso");
            MisoSceneModel model = parser.parseScene(args[0]);
            if (model != null) {
                _panel.setScene(new DisplayMisoSceneImpl(model, _tilemgr));
            } else {
                Log.warning("No miso scene found in scene file " +
                            "[path=" + args[0] + "].");
            }

        } catch (Exception e) {
            Log.warning("Unable to parse scene [path=" + args[0] + "].");
            Log.logStackTrace(e);
            System.exit(-1);
        }

        // size and position the window, entering full-screen exclusive
        // mode if available
        if (gd.isFullScreenSupported()) {
            Log.info("Entering full-screen exclusive mode.");
            gd.setFullScreenWindow(_frame);

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
            ViewerApp app = new ViewerApp(args);
            app.run();
        } catch (IOException ioe) {
            System.err.println("Error initializing viewer app.");
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
    protected ViewerFrame _frame;

    /** The main panel. */
    protected ViewerSceneViewPanel _panel;

    /** The default scene to load and display. */
    protected static final String DEF_SCENE = "rsrc/scenes/default.xml";
}
