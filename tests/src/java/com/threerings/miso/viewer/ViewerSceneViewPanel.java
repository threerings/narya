//
// $Id: ViewerSceneViewPanel.java,v 1.19 2001/10/22 18:15:57 shaper Exp $

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
    implements PerformanceObserver
{
    /**
     * Construct the panel and initialize it with a context.
     */
    public ViewerSceneViewPanel (
	ViewerContext ctx, SpriteManager spritemgr, AmbulatorySprite sprite)
    {
	super(ctx.getConfig(), spritemgr);

	_ctx = ctx;
        _sprite = sprite;

        // create an animation manager for this panel
  	_animmgr = new AnimationManager(spritemgr, this);

        // listen to the desired events
	addMouseListener(new MouseAdapter() {
            public void mousePressed (MouseEvent e) {
                ViewerSceneViewPanel.this.mousePressed(e);
            }
        });

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
	if (path == null) {
	    _sprite.cancelMove();
	    return;
	}

	((LineSegmentPath)path).setVelocity(100f/1000f);
	_sprite.move(path);
    }

    /** The animation manager. */
    AnimationManager _animmgr;

    /** The sprite we're manipulating within the view. */
    protected AmbulatorySprite _sprite;

    /** The context object. */
    protected ViewerContext _ctx;
}
