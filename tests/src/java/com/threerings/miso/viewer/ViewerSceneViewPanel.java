//
// $Id: ViewerSceneViewPanel.java,v 1.16 2001/10/01 22:18:24 mdb Exp $

package com.threerings.miso.viewer;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.JPanel;

import com.samskivert.util.Config;
import com.threerings.media.sprite.*;

import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.scene.xml.XMLFileSceneRepository;
import com.threerings.miso.util.*;
import com.threerings.miso.viewer.util.ViewerContext;

public class ViewerSceneViewPanel extends SceneViewPanel
    implements MouseListener, MouseMotionListener, PerformanceObserver
{
    /**
     * Construct the panel and initialize it with a context.
     */
    public ViewerSceneViewPanel (
	ViewerContext ctx, SpriteManager spritemgr, AmbulatorySprite sprite)
    {
	super(ctx.getConfig(), ctx.getTileManager(), spritemgr);

	_ctx = ctx;
        _sprite = sprite;

        // create an animation manager for this panel
  	_animmgr = new AnimationManager(spritemgr, this);

        // listen to the desired events
	addMouseListener(this);
	addMouseMotionListener(this);

        // load up the initial scene
        prepareStartingScene();

	PerformanceMonitor.register(this, "paint", 1000);
    }

    /**
     * Load and set up the starting scene for display.
     */
    protected void prepareStartingScene ()
    {
	ViewerModel model = _ctx.getViewerModel();
        try {
	    XMLFileSceneRepository screpo = _ctx.getSceneRepository();
            _view.setScene(screpo.loadScene(model.scenefile));

        } catch (IOException ioe) {
            Log.warning("Exception loading scene [fname=" + model.scenefile +
                        ", ioe=" + ioe + "].");
        }
    }

    public void paintComponent (Graphics g)
    {
	super.paintComponent(g);
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
	_sprite.move(path);
    }

    public void mouseClicked (MouseEvent e) { }
    public void mouseEntered (MouseEvent e) { }
    public void mouseExited (MouseEvent e) { }
    public void mouseReleased (MouseEvent e) { }

    /** MouseMotionListener interface methods */

    public void mouseMoved (MouseEvent e) { }
    public void mouseDragged (MouseEvent e) { }

    /** The animation manager. */
    AnimationManager _animmgr;

    /** The sprite we're manipulating within the view. */
    protected AmbulatorySprite _sprite;

    /** The context object. */
    protected ViewerContext _ctx;
}
