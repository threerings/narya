//
// $Id: ViewerApp.java,v 1.13 2001/11/02 03:09:10 shaper Exp $

package com.threerings.miso.viewer;

import java.awt.Frame;
import java.io.IOException;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.Config;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;
import com.threerings.miso.scene.xml.XMLSceneRepository;
import com.threerings.miso.util.MisoUtil;
import com.threerings.miso.viewer.util.ViewerContext;

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
    {
        // create and size the main application frame
	_frame = new ViewerFrame();
	_frame.setSize(WIDTH, HEIGHT);
  	SwingUtil.centerWindow(_frame);

	// create the handles on our various services
	_config = MisoUtil.createConfig(
            ViewerModel.CONFIG_KEY, "rsrc/config/miso/viewer");
	_model = createModel(_config, args);
        _imgmgr = MisoUtil.createImageManager(_frame);
	_tilemgr = MisoUtil.createTileManager(_config, _imgmgr);
	_screpo = MisoUtil.createSceneRepository(_config, _tilemgr);

	// create the context object
	_ctx = new ViewerContextImpl();

    	// initialize the frame with the now-prepared context
	((ViewerFrame)_frame).init(_ctx);
    }

    protected ViewerModel createModel (Config config, String args[])
    {
	ViewerModel model = new ViewerModel(config);
	if (args.length >= 1) {
	    model.scenefile = args[0];
	}
	return model;
    }

    /**
     * The implementation of the ViewerContext interface that provides
     * handles to the config and manager objects that offer commonly used
     * services.
     */
    protected class ViewerContextImpl implements ViewerContext
    {
	public Config getConfig ()
	{
	    return _config;
	}

        public ImageManager getImageManager ()
        {
            return _imgmgr;
        }

	public TileManager getTileManager ()
	{
	    return _tilemgr;
	}

        public XMLSceneRepository getSceneRepository ()
        {
            return _screpo;
        }

	public ViewerModel getViewerModel ()
	{
	    return _model;
	}
    }

    /**
     * Run the application.
     */
    public void run ()
    {
        _frame.pack();
        _frame.show();
    }

    /**
     * Instantiate the application object and start it running.
     */
    public static void main (String[] args)
    {
	ViewerApp app = new ViewerApp(args);
        app.run();
    }

    /** The desired width and height for the main application window. */
    protected static final int WIDTH = 800;
    protected static final int HEIGHT = 622;

    /** The config object. */
    protected Config _config;

    /** The scene repository. */
    protected XMLSceneRepository _screpo;

    /** The tile manager object. */
    protected TileManager _tilemgr;

    /** The image manager object. */
    protected ImageManager _imgmgr;

    /** The viewer data model. */
    protected ViewerModel _model;

    /** The context object. */
    protected ViewerContext _ctx;

    /** The main application window. */
    protected Frame _frame;
}
