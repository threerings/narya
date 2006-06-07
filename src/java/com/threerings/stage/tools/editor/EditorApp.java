//
// $Id: EditorApp.java 19661 2005-03-09 02:40:29Z andrzej $

package com.threerings.stage.tools.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import com.samskivert.swing.RuntimeAdjust;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.DebugChords;
import com.samskivert.util.StringUtil;

import com.threerings.util.KeyDispatcher;
import com.threerings.util.KeyboardManager;
import com.threerings.util.MessageBundle;
import com.threerings.util.MessageManager;

import com.threerings.resource.ResourceManager;

import com.threerings.media.FrameManager;
import com.threerings.media.image.ColorPository;
import com.threerings.media.image.ImageManager;
import com.threerings.media.util.ModeUtil;

import com.threerings.media.tile.TileSetRepository;
import com.threerings.media.tile.bundle.BundledTileSetRepository;

import com.threerings.cast.ComponentRepository;
import com.threerings.cast.bundle.BundledComponentRepository;
import com.threerings.miso.tile.MisoTileManager;

import com.threerings.stage.data.StageSceneModel;
import com.threerings.stage.tools.editor.util.EditorContext;

/**
 * A scene editor application that provides facilities for viewing,
 * editing, and saving the scene templates that comprise a game.
 */
public class EditorApp implements Runnable
{
    /**
     * Construct and initialize the EditorApp object.
     */
    public EditorApp (String[] args)
        throws IOException
    {
        final String target = (args.length > 0) ? args[0] : null;

        if (System.getProperty("no_log_redir") != null) {
            Log.info("Logging to console only.");

        } else {
            String dlog = localDataDir("editor.log");
            try {
                PrintStream logOut = new PrintStream(
                    new BufferedOutputStream(new FileOutputStream(dlog)), true);
                System.setOut(logOut);
                System.setErr(logOut);
                Log.info("Opened debug log '" + dlog + "'.");

            } catch (IOException ioe) {
                Log.warning("Failed to open debug log [path=" + dlog +
                            ", error=" + ioe + "].");
            }
        }

        // we need to use heavy-weight popup menus so that they can overly
        // our non-lightweight Miso view
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        // create and size the main application frame
	_frame = createEditorFrame();

        // create our frame manager
        _framemgr = FrameManager.newInstance(_frame, _frame);

	// create our myriad managers, repositories, etc.
        _rmgr = new ResourceManager("rsrc");

        // build up a simple ui for displaying progress
        JPanel progressPanel = new JPanel(new BorderLayout());
        final JLabel progressLabel = new JLabel();
        final JProgressBar progress = new JProgressBar(0,100);
        final JPanel spacer = new JPanel();

        progressPanel.add(progressLabel, BorderLayout.CENTER);
        progressPanel.add(progress, BorderLayout.SOUTH);
        progressPanel.setPreferredSize(new Dimension(300,80));
        spacer.add(progressPanel);

        _frame.getContentPane().add(spacer);

        final EditorFrame frameRef = _frame;

        ResourceManager.InitObserver obs = new ResourceManager.InitObserver() {
            public void progress (int percent, long remaining) {
                String msg = "Unpacking...";
                if (remaining >= 0) {
                    msg += " " + remaining + " seconds remaining.";
                }
                progressLabel.setText(msg);
                progress.setValue(percent);

                if (percent >= 100) {
                    frameRef.getContentPane().remove(spacer);
                    EditorApp.this.finishInit(target);
                }
            }

            public void initializationFailed (Exception e) {
                Log.warning("Failed unpacking bundles [e=" + e + "].");
                Log.logStackTrace(e);
            }
        };
        // we want our methods called on the AWT thread
        obs = new ResourceManager.AWTInitObserver(obs);
        _rmgr.initBundles(null, "config/resource/editor.properties", obs);
    }

    public void finishInit (String target)
    {
        _msgmgr = new MessageManager("rsrc.i18n");
        _imgr = new ImageManager(_rmgr, _frame);
	_tilemgr = new MisoTileManager(_rmgr, _imgr);

        try {
            _tsrepo = new BundledTileSetRepository(_rmgr, _imgr, "tilesets");
            _tilemgr.setTileSetRepository(_tsrepo);
            _crepo = new BundledComponentRepository(
                _rmgr, _imgr, "components");
        } catch (IOException e) {
            Log.warning("Exception loading tilesets and and icon manager " +
                        "[Exception=" + e + "].");
            return;
        }

        _colpos = ColorPository.loadColorPository(_rmgr);
        _kbdmgr = new KeyboardManager();
        _keydisp = new KeyDispatcher(_frame);
        
        _ctx = new EditorContextImpl();

    	// initialize the frame with the now-prepared context
        _frame.init(_ctx, target);

        // wire up our runtime adjustment editor
        DebugChords.activate();

        // if we have a target file, load it up
        if (target != null) {
            _frame.openScene(target);
        }
    }

    /**
     * Given a subdirectory name (that should correspond to the calling
     * service), returns a file path that can be used to store local data.
     */
    public static String localDataDir (String subdir)
    {
        String appdir = System.getProperty("appdir");
        if (StringUtil.isBlank(appdir)) {
            appdir = ".narya-editor";
            String home = System.getProperty("user.home");
            if (!StringUtil.isBlank(home)) {
                appdir = home + File.separator + appdir;
            }
        }
        return appdir + File.separator + subdir;
    }

    /**
     * Run the application.
     */
    public void run ()
    {
        // enter full-screen exclusive mode if available and if we have
        // the right display mode
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = env.getDefaultScreenDevice();
        DisplayMode pmode = null;
        try {
            DisplayMode cmode = gd.getDisplayMode();
            pmode = ModeUtil.getDisplayMode(
                gd, cmode.getWidth(), cmode.getHeight(), 16, 15);
        } catch (Throwable t) {
            // Win98 seems to choke on it's own vomit when we attempt to
            // enumerate the available display modes; yay!
            Log.warning("Failed to probe display mode.");
            Log.logStackTrace(t);
        }

        if (_viewFullScreen.getValue() && gd.isFullScreenSupported() &&
            pmode != null) {
            Log.info("Switching to screen mode " +
                     "[mode=" + ModeUtil.toString(pmode) + "].");
            // set the frame to undecorated, full-screen
            _frame.setUndecorated(true);
            gd.setFullScreenWindow(_frame);
            // switch to our happy custom display mode
            gd.setDisplayMode(pmode);
            // lay the frame out properly (we can't do this before making
            // it full screen because packing causes the window to become
            // displayable which apparently prevents the window from
            // subsequently being made a full-screen window)
            _frame.pack();

        } else {
            _frame.setSize(1024, 768);
            SwingUtil.centerWindow(_frame);
            _frame.setVisible(true);
        }
        _framemgr.start();
    }

    protected EditorFrame createEditorFrame ()
    {
        return new EditorFrame();
    }

    /**
     * Derived classes can override this method and add additional scene
     * types.
     */
    protected void enumerateSceneTypes (List types)
    {
        types.add(StageSceneModel.WORLD);
    }

    /**
     * Instantiate the application object and start it running.
     */
    public static void main (String[] args)
    {
        try {
            EditorApp app = new EditorApp(args);
            // start up the UI on the AWT thread to avoid deadlocks
            EventQueue.invokeLater(app);

        } catch (IOException ioe) {
            Log.warning("Unable to initialize editor.");
            Log.logStackTrace(ioe);
        }
    }

    /**
     * The implementation of the EditorContext interface that provides
     * handles to the config and manager objects that offer commonly
     * used services.
     */
    protected class EditorContextImpl implements EditorContext
    {
	public MisoTileManager getTileManager () {
	    return _tilemgr;
	}

        public FrameManager getFrameManager () {
            return _framemgr;
        }

        public ResourceManager getResourceManager () {
            return _rmgr;
        }

        public ImageManager getImageManager () {
            return _imgr;
        }

        public MessageManager getMessageManager () {
            return _msgmgr;
        }

        public KeyboardManager getKeyboardManager() {
            return _kbdmgr;
        }

        public ComponentRepository getComponentRepository () {
            return _crepo;
        }

        public KeyDispatcher getKeyDispatcher () {
            return _keydisp;
        }
        
        public String xlate (String message) {
            return xlate("stage.editor", message);
        }

        public String xlate (String bundle, String message) {
            MessageBundle mbundle = _msgmgr.getBundle(bundle);
            if (mbundle == null) {
                Log.warning("Requested to translate message with " +
                            "non-existent bundle [bundle=" + bundle +
                            ", message=" + message + "].");
                return message;
            } else {
                return mbundle.xlate(message);
            }
        }

	public TileSetRepository getTileSetRepository () {
	    return _tsrepo;
	}

	public ColorPository getColorPository () {
	    return _colpos;
	}

        public void enumerateSceneTypes (List types) {
            EditorApp.this.enumerateSceneTypes(types);
        }
    }

    protected EditorContext _ctx;
    protected EditorFrame _frame;
    protected FrameManager _framemgr;

    protected ResourceManager _rmgr;
    protected ImageManager _imgr;
    protected MisoTileManager _tilemgr;
    protected TileSetRepository _tsrepo;
    protected ColorPository _colpos;
    protected MessageManager _msgmgr;
    protected KeyboardManager _kbdmgr;
    protected BundledComponentRepository _crepo;
    protected KeyDispatcher _keydisp;

    /** A debug hook that toggles debug rendering of traversable tiles. */
    protected static RuntimeAdjust.BooleanAdjust _viewFullScreen =
        new RuntimeAdjust.BooleanAdjust(
            "Toggles whether or not the scene editor uses full screen mode.",
            "stage.editor.full_screen", EditorConfig.config, false);
}
