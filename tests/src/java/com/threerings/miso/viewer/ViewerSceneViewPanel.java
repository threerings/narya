//
// $Id: ViewerSceneViewPanel.java,v 1.20 2001/10/23 02:03:49 shaper Exp $

package com.threerings.miso.viewer;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.JPanel;

import com.samskivert.util.Config;

import com.threerings.media.sprite.*;
import com.threerings.media.util.RandomUtil;

import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.scene.xml.XMLFileSceneRepository;
import com.threerings.miso.util.*;
import com.threerings.miso.viewer.util.ViewerContext;

public class ViewerSceneViewPanel extends SceneViewPanel
    implements PerformanceObserver, SpriteObserver
{
    /**
     * Construct the panel and initialize it with a context.
     */
    public ViewerSceneViewPanel (
	ViewerContext ctx, SpriteManager spritemgr, CharacterManager charmgr)
    {
	super(ctx.getConfig(), spritemgr);

	_ctx = ctx;

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

        // create the manipulable sprite
        _sprite = createSprite(spritemgr, charmgr);

        // create the decoy sprites
        createDecoys(spritemgr, charmgr);

	PerformanceMonitor.register(this, "paint", 1000);
    }

    /**
     * Creates a new sprite.
     */
    protected AmbulatorySprite createSprite (
        SpriteManager spritemgr, CharacterManager charmgr)
    {
        AmbulatorySprite s = charmgr.getCharacter(TSID_CHAR);
        if (s != null) {
            s.setLocation(300, 300);
            s.addSpriteObserver(this);
            spritemgr.addSprite(s);
        }

        return s;
    }

    /**
     * Creates the decoy sprites.
     */
    protected void createDecoys (
        SpriteManager spritemgr, CharacterManager charmgr)
    {
        _decoys = new AmbulatorySprite[NUM_DECOYS];
        for (int ii = 0; ii < NUM_DECOYS; ii++) {
            if ((_decoys[ii] = createSprite(spritemgr, charmgr)) != null) {
                createRandomPath(_decoys[ii]);
            }
        }
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

    // documentation inherited
    public void paintComponent (Graphics g)
    {
	super.paintComponent(g);
	PerformanceMonitor.tick(this, "paint");
    }

    // documentation inherited
    public void checkpoint (String name, int ticks)
    {
        Log.info(name + " [ticks=" + ticks + "].");
    }

    /** MouseListener interface methods */

    public void mousePressed (MouseEvent e)
    {
        int x = e.getX(), y = e.getY();
        Log.info("mousePressed [x=" + x + ", y=" + y + "].");
        createPath(_sprite, x, y);
    }

    /**
     * Assigns the sprite a path leading to the given destination
     * screen coordinates.  Returns whether a path was successfully
     * assigned.
     */
    protected boolean createPath (AmbulatorySprite s, int x, int y)
    {
        // get the path from here to there
        Path path = _view.getPath(s, x, y);
	if (path == null) {
	    s.cancelMove();
	    return false;
	}

        // start the sprite moving along the path
	((LineSegmentPath)path).setVelocity(100f/1000f);
	s.move(path);
        return true;
    }

    /**
     * Assigns a new random path to the given sprite.
     */
    protected void createRandomPath (AmbulatorySprite s)
    {
        Dimension d = _scenemodel.bounds.getSize();

        int x, y;
        do {
            x = RandomUtil.getInt(d.width);
            y = RandomUtil.getInt(d.height);
            // Log.info("Moving sprite [s=" + s + ", x=" + x +
            // ", y=" + y + "].");
        } while (!createPath(s, x, y));
    }

    // documentation inherited
    public void handleEvent (SpriteEvent event)
    {
        if (event instanceof PathCompletedEvent) {
            AmbulatorySprite s = (AmbulatorySprite)event.getSprite();
            // Log.info("Path completed [sprite=" + s + "].");

            if (s != _sprite) {
                // move the sprite to a new random location
                createRandomPath(s);
            }
        }
    }

    /** The number of decoy characters milling about. */
    protected static final int NUM_DECOYS = 10;

    /** The tileset id for the character tiles. */
    protected static final int TSID_CHAR = 1011;

    /** The animation manager. */
    AnimationManager _animmgr;

    /** The sprite we're manipulating within the view. */
    protected AmbulatorySprite _sprite;

    /** The test sprites that meander about aimlessly. */
    protected AmbulatorySprite _decoys[];

    /** The context object. */
    protected ViewerContext _ctx;
}
