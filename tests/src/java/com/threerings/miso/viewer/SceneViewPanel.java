//
// $Id: SceneViewPanel.java,v 1.7 2001/08/02 20:43:03 shaper Exp $

package com.threerings.miso.viewer;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.JPanel;

import com.samskivert.util.Config;
import com.threerings.miso.Log;
import com.threerings.miso.viewer.util.ViewerContext;
import com.threerings.miso.scene.*;
import com.threerings.miso.scene.xml.XMLFileSceneRepository;
import com.threerings.miso.sprite.*;
import com.threerings.miso.util.PerformanceMonitor;
import com.threerings.miso.util.PerformanceObserver;

/**
 * The <code>SceneViewPanel</code> class is responsible for managing a
 * <code>SceneView</code>, rendering it to the screen, and handling
 * view-related UI events.
 */
public class SceneViewPanel extends JPanel
    implements MouseListener, MouseMotionListener, PerformanceObserver
{
    /**
     * Construct the panel and initialize it with a context.
     */
    public SceneViewPanel (ViewerContext ctx, SpriteManager spritemgr,
                           Sprite sprite)
    {
	_ctx = ctx;

        _sprite = sprite;

        // construct the view object
        IsoSceneModel smodel = new IsoSceneModel();
        smodel.setTileDimensions(64, 48);
        smodel.setBounds((10 * 64), (12 * 48));
        smodel.setOrigin(320, -(5 * 48));

        _view = new IsoSceneView(_ctx.getTileManager(), spritemgr, smodel);

        // create an animation manager for this panel
        AnimationManager animmgr =
            new AnimationManager(spritemgr, this, _view);

        // listen to the desired events
	addMouseListener(this);
	addMouseMotionListener(this);

        // load up the initial scene
        prepareStartingScene();

        //setDoubleBuffered(false);

        PerformanceMonitor.register(this, "paint", 1000);
    }

    /**
     * Load and set up the starting scene for display.
     */
    protected void prepareStartingScene ()
    {
        // get the scene repository
        XMLFileSceneRepository repo = (XMLFileSceneRepository)
            _ctx.getSceneManager().getSceneRepository();

        // get the starting scene filename
        Config config = _ctx.getConfig();
        String fname = config.getValue(CFG_SCENE, DEF_SCENE);

        try {
            // load and set up the scene
            _view.setScene(repo.loadScene(fname));
        } catch (IOException ioe) {
            Log.warning("Exception loading scene [fname=" + fname +
                        ", ioe=" + ioe + "].");
        }
    }

    /**
     * Set the scene managed by the panel.
     */
    public void setScene (Scene scene)
    {
	_view.setScene(scene);
    }

    /**
     * Render the panel and the scene view to the given graphics object.
     */
    public void paintComponent (Graphics g)
    {
        Rectangle bounds = getBounds();

//          Log.info("SceneViewPanel: paint [width=" + bounds.width +
//                   ", height=" + bounds.height + "].");

	_view.paint(g);

        PerformanceMonitor.tick(this, "paint");
    }

    public void checkpoint (String name, int ticks)
    {
        Log.info(name + " [ticks=" + ticks + "].");
    }

    /** MouseListener interface methods */

    public void mousePressed (MouseEvent e)
    {
        int x = e.getX(), y = e.getY();
        Log.info("mousePressed [x=" + x + ", y=" + y + "].");

        // get the path from here to there
        Path path = _view.getPath(_sprite, x, y);
        if (path != null) {
            _sprite.move(path);
        }

        // hackily highlight the tile that was clicked on for happy testing
        ((EditableSceneView)_view).setHighlightedTile(x, y);
    }

    public void mouseClicked (MouseEvent e) { }
    public void mouseEntered (MouseEvent e) { }
    public void mouseExited (MouseEvent e) { }
    public void mouseReleased (MouseEvent e) { }

    /** MouseMotionListener interface methods */

    public void mouseMoved (MouseEvent e) { }
    public void mouseDragged (MouseEvent e) { }

    /** The config key to obtain the default scene filename. */
    protected static final String CFG_SCENE = "miso-viewer.default_scene";

    /** The default scene to load and display. */
    protected static final String DEF_SCENE = "rsrc/scenes/default.xml";

    /** The sprite we're manipulating within the view. */
    protected Sprite _sprite;

    /** The context object. */
    protected ViewerContext _ctx;

    /** The scene view we're managing. */
    protected SceneView _view;
}
