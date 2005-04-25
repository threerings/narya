//
// $Id: ViewerApp.java 19661 2005-03-09 02:40:29Z andrzej $

package com.threerings.stage.tools.viewer;

import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import java.io.File;
import java.io.IOException;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.RuntimeAdjust;

import com.threerings.resource.ResourceManager;
import com.threerings.util.KeyDispatcher;
import com.threerings.util.KeyboardManager;
import com.threerings.util.MessageManager;

import com.threerings.media.FrameManager;
import com.threerings.media.IconManager;
import com.threerings.media.image.ColorPository;
import com.threerings.media.image.ImageManager;
import com.threerings.media.tile.bundle.BundledTileSetRepository;

import com.threerings.cast.CharacterManager;
import com.threerings.cast.bundle.BundledComponentRepository;
import com.threerings.cast.ComponentRepository;

import com.threerings.miso.tile.MisoTileManager;

import com.threerings.stage.Log;
import com.threerings.stage.util.StageContext;

/**
 * The ViewerApp is a scene viewing application that allows for trying out
 * Stage scenes in a pseudo-runtime environment.
 */
public class ViewerApp
{
    /**
     * Construct and initialize the ViewerApp object.
     */
    public ViewerApp (String[] args)
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

        _rmgr = new ResourceManager("rsrc");
        _rmgr.initBundles(null, "config/resource/manager.properties", null);
        _imgr = new ImageManager(_rmgr, _frame);
	_tilemgr = new MisoTileManager(_rmgr, _imgr);
        _tilemgr.setTileSetRepository(
            new BundledTileSetRepository(_rmgr, _imgr, "tilesets"));
        _colpos = ColorPository.loadColorPository(_rmgr);
        _crepo = new BundledComponentRepository(_rmgr, _imgr, "components");
        _mesgmgr = new MessageManager("rsrc.i18n");

	_frame = new ViewerFrame(gc);
        _framemgr = FrameManager.newInstance(_frame);

	StageContext ctx = new ContextImpl();
        _frame.init(ctx, new CharacterManager(_imgr, _crepo));

        // grab our argument
        _target = (args.length > 0) ? args[0] : null;

        // size and position the window, entering full-screen exclusive
        // mode if available and desired
        if (gd.isFullScreenSupported() /* && _viewFullScreen.getValue() */) {
            Log.info("Entering full-screen exclusive mode.");
            gd.setFullScreenWindow(_frame);
         } else {
             _frame.setSize(640, 575);
             SwingUtil.centerWindow(_frame);
         }
    }

    /**
     * The implementation of the {@link StageContext} interface that
     * provides handles to the config and manager objects that offer
     * commonly used services.
     */
    protected class ContextImpl implements StageContext
    {
        public FrameManager getFrameManager () {
            return _framemgr;
        }

	public MisoTileManager getTileManager () {
	    return _tilemgr;
	}

        // documentation inherited from interface
        public ResourceManager getResourceManager () {
            return _rmgr;
        }

        // documentation inherited from interface
        public ImageManager getImageManager () {
            return _imgr;
        }

        // documentation inherited from interface
        public MessageManager getMessageManager () {
            return _mesgmgr;
        }

        // documentation inherited from interface
        public IconManager getIconManager () {
            return null;
        }

        // documentation inherited from interface
        public KeyboardManager getKeyboardManager() {
            return null;
        }

        // documentation inherited from interface
        public ComponentRepository getComponentRepository () {
            return _crepo;
        }

        // documentation inherited from interface
        public ColorPository getColorPository () {
            return _colpos;
        }

        // documentation inherited from interface
        public KeyDispatcher getKeyDispatcher () {
            return null;
        }
        
        // documentation inherited from interface
        public String xlate (String message) {
            return message;
        }

        // documentation inherited from interface
        public String xlate (String bundle, String message) {
            return message;
        }
    }

    /**
     * Run the application.
     */
    public void run ()
    {
        // show the window
        _frame.setVisible(true);
        _framemgr.start();

        // load up anything specified on the command line
        EventQueue.invokeLater(new Runnable() {
            public void run () {
                if (_target != null) {
                    _frame.loadScene(_target);
                } else {
                    _frame.openScene(null);
                }
            }
        });
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

    protected ResourceManager _rmgr;
    protected MisoTileManager _tilemgr;
    protected ImageManager _imgr;
    protected BundledComponentRepository _crepo;
    protected ColorPository _colpos;
    protected MessageManager _mesgmgr;

    protected FrameManager _framemgr;
    protected ViewerFrame _frame;
    protected String _target;

//     /** A debug hook that toggles debug rendering of traversable tiles. */
//     protected static RuntimeAdjust.BooleanAdjust _viewFullScreen =
//         new RuntimeAdjust.BooleanAdjust(
//             "Toggles whether or not the scene viewer uses full screen mode.",
//             "stage.viewer.full_screen", ToolPrefs.config, false);
}
