//
// $Id: ViewerSceneViewPanel.java,v 1.23 2001/10/26 01:17:22 shaper Exp $

package com.threerings.miso.viewer;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.JPanel;

import com.samskivert.util.Config;

import com.threerings.cast.*;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.TileManager;
import com.threerings.media.util.RandomUtil;
import com.threerings.media.util.PerformanceMonitor;
import com.threerings.media.util.PerformanceObserver;

import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.scene.xml.XMLFileSceneRepository;
import com.threerings.miso.scene.util.IsoUtil;
import com.threerings.miso.util.*;
import com.threerings.miso.viewer.util.ViewerContext;

public class ViewerSceneViewPanel extends SceneViewPanel
    implements PerformanceObserver, SpriteObserver
{
    /**
     * Construct the panel and initialize it with a context.
     */
    public ViewerSceneViewPanel (ViewerContext ctx, SpriteManager spritemgr)
    {
	super(ctx.getConfig(), spritemgr);

	_ctx = ctx;

        // create an animation manager for this panel
  	_animmgr = new AnimationManager(spritemgr, this);

        // load up the initial scene
        prepareStartingScene();

        // construct the character manager from which we obtain sprites
        CharacterManager charmgr = MisoUtil.createCharacterManager(
            ctx.getConfig(), ctx.getTileManager());

        // create the manipulable sprite
        _sprite = createSprite(spritemgr, charmgr, TSID_CHAR_USER);

        // create the decoy sprites
        createDecoys(spritemgr, charmgr);

        // listen to the desired events
	addMouseListener(new MouseAdapter() {
            public void mousePressed (MouseEvent e) {
                ViewerSceneViewPanel.this.mousePressed(e);
            }
        });

	PerformanceMonitor.register(this, "paint", 1000);
    }

    /**
     * Creates a new sprite.
     */
    protected MisoCharacterSprite createSprite (
        SpriteManager spritemgr, CharacterManager charmgr, int tsid)
    {
        int dummy[] = { tsid };
        CharacterDescriptor desc = new CharacterDescriptor(dummy);
        MisoCharacterSprite s =
            (MisoCharacterSprite)charmgr.getCharacter(desc);
        if (s != null) {
            s.setLocation(300, 300);
            IsoUtil.setSpriteSceneLocation(_scenemodel, s);
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
        _decoys = new MisoCharacterSprite[NUM_DECOYS];
        for (int ii = 0; ii < NUM_DECOYS; ii++) {
            _decoys[ii] = createSprite(spritemgr, charmgr, TSID_CHAR);
            if (_decoys[ii] != null) {
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
    protected boolean createPath (MisoCharacterSprite s, int x, int y)
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
    protected void createRandomPath (MisoCharacterSprite s)
    {
        Dimension d = _scenemodel.bounds.getSize();

        int x, y;
        do {
            x = RandomUtil.getInt(d.width);
            y = RandomUtil.getInt(d.height);
        } while (!createPath(s, x, y));
    }

    // documentation inherited
    public void handleEvent (SpriteEvent event)
    {
        if (event instanceof PathCompletedEvent) {
            MisoCharacterSprite s = (MisoCharacterSprite)event.getSprite();

            if (s != _sprite) {
                // move the sprite to a new random location
                createRandomPath(s);
            }
        }
    }

    /** The number of decoy characters milling about. */
    protected static final int NUM_DECOYS = 2;

    /** The tileset id for the decoy character tiles. */
    protected static final int TSID_CHAR = 1011;

    /** The tileset id for the user character tiles. */
    protected static final int TSID_CHAR_USER = 1012;

    /** The animation manager. */
    protected AnimationManager _animmgr;

    /** The sprite we're manipulating within the view. */
    protected MisoCharacterSprite _sprite;

    /** The test sprites that meander about aimlessly. */
    protected MisoCharacterSprite _decoys[];

    /** The context object. */
    protected ViewerContext _ctx;
}
