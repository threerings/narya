//
// $Id: ViewerApp.java,v 1.17 2001/11/27 08:49:58 mdb Exp $

package com.threerings.miso.viewer;

import java.io.IOException;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.Config;

import com.threerings.resource.ResourceManager;
import com.threerings.media.sprite.SpriteManager;
import com.threerings.media.tile.TileManager;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.bundle.BundledComponentRepository;

import com.threerings.miso.Log;
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
        // we don't need to configure anything
        _config = new Config();
        _rsrcmgr = new ResourceManager(null, "rsrc");
	_tilemgr = new TileManager(_rsrcmgr);

        // bind our miso properties
        MisoUtil.bindProperties(_config);

	// create the context object
	MisoContext ctx = new ContextImpl();

        // create the various managers
        SpriteManager spritemgr = new SpriteManager();
        BundledComponentRepository crepo =
            new BundledComponentRepository(_rsrcmgr, "components");
        CharacterManager charmgr = new CharacterManager(crepo);

        // create and size the main application frame
	_frame = new ViewerFrame();
	_frame.setSize(WIDTH, HEIGHT);

        // create our scene view panel
        _panel = new ViewerSceneViewPanel(ctx, spritemgr, charmgr, crepo);
        _frame.setPanel(_panel);

        // determine whether or not the user specified a scene to be
        // displayed or if we'll be using the default
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
        _frame.pack();
  	SwingUtil.centerWindow(_frame);
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

    /** The resource manager. */
    protected ResourceManager _rsrcmgr;

    /** The tile manager object. */
    protected TileManager _tilemgr;

    /** The main application window. */
    protected ViewerFrame _frame;

    /** The main panel. */
    protected ViewerSceneViewPanel _panel;

    /** The default scene to load and display. */
    protected static final String DEF_SCENE = "rsrc/scenes/default.xml";
}
