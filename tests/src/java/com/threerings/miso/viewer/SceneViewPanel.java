//
// $Id: SceneViewPanel.java,v 1.5 2001/08/02 00:42:02 shaper Exp $

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
 * The SceneViewPanel class is responsible for managing a SceneView,
 * rendering it to the screen, and handling view-related UI events.
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
        _view = new IsoSceneView(_ctx.getTileManager(), spritemgr,
                                 new IsoSceneModel());

        // create an animation manager for this panel
        AnimationManager animmgr =
            new AnimationManager(spritemgr, this, _view);

        // listen to the desired events
	addMouseListener(this);
	addMouseMotionListener(this);

        // load up the initial scene
        prepareStartingScene();

        setDoubleBuffered(false);

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
        String fname = config.getValue(CONF_SCENE, (String)DEF_SCENE);

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

        g.setColor(Color.yellow);
        g.drawRect(0, 0, 600, 600);

        PerformanceMonitor.tick(this, "paint");
    }

    public void checkpoint (String name, int ticks)
    {
        Log.info(name + "[ticks=" + ticks + "].");
    }

    /** MouseListener interface methods */

    public void mouseClicked (MouseEvent e)
    {
        Log.info("mouseClicked [x=" + e.getX() + ", y=" + e.getY() + "].");
    }

    public void mouseEntered (MouseEvent e) { }

    public void mouseExited (MouseEvent e) { }

    public void mousePressed (MouseEvent e)
    {
        int x = e.getX(), y = e.getY();
        Log.info("mousePressed [x=" + x + ", y=" + y + "].");
        _sprite.setDestination(x, y);
        ((EditableSceneView)_view).setHighlightedTile(x, y);
    }

    public void mouseReleased (MouseEvent e) { }

    /** MouseMotionListener interface methods */

    public void mouseMoved (MouseEvent e)
    {
    }

    public void mouseDragged (MouseEvent e)
    {
    }

    /** The config key to obtain the default scene filename. */
    protected static final String CONF_SCENE = "miso-viewer.default_scene";

    /** The default scene to load and display. */
    protected static final String DEF_SCENE = "rsrc/scenes/default.xml";

    protected Sprite _sprite;

    /** The context object. */
    protected ViewerContext _ctx;

    /** The scene view we're managing. */
    protected SceneView _view;
}
