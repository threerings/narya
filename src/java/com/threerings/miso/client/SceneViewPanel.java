//
// $Id: SceneViewPanel.java,v 1.7 2001/08/15 22:16:43 shaper Exp $

package com.threerings.miso.scene;

import java.awt.*;
import javax.swing.JPanel;

import com.threerings.media.sprite.SpriteManager;
import com.threerings.miso.tile.TileManager;

/**
 * The <code>SceneViewPanel</code> class is responsible for managing a
 * <code>SceneView</code>, rendering it to the screen, and handling
 * view-related UI events.
 */
public class SceneViewPanel extends JPanel
{
    /**
     * Construct the panel and initialize it with a context.
     */
    public SceneViewPanel (TileManager tilemgr, SpriteManager spritemgr)
    {
        // create the data model for the scene view
        _smodel = new IsoSceneViewModel();
        _smodel.setTileDimensions(TWIDTH, THEIGHT);
        _smodel.setBounds((HTILES * TWIDTH), (VTILES * THEIGHT));
        _smodel.setOrigin(_smodel.bounds.width / 2, VOFFSET);

	// create the scene view
        _view = new IsoSceneView(tilemgr, spritemgr, _smodel);

	// set our attributes for optimal display performance
        setDoubleBuffered(false);
        setOpaque(true);
    }

    /**
     * Set the scene managed by the panel.
     */
    public void setScene (MisoScene scene)
    {
	_view.setScene(scene);
    }

    /**
     * Get the scene managed by the panel.
     */
    public SceneView getSceneView ()
    {
	return _view;
    }

    /**
     * Render the panel and the scene view to the given graphics object.
     */
    public void render (Graphics g)
    {
	if (_offimg == null) {
	    createOffscreen();
	}

	_view.paint(_offg);
	g.drawImage(_offimg, 0, 0, null);
    }

    public void paintComponent (Graphics g)
    {
	render(g);
    }

    protected void createOffscreen ()
    {
	Rectangle bounds = getBounds();
	_offimg = createImage(bounds.width, bounds.height);
	_offg = _offimg.getGraphics();
    }

    /**
     * Return the desired size for the panel based on the requested
     * and calculated bounds of the scene view.
     */
    public Dimension getPreferredSize ()
    {
	return (_smodel == null) ? super.getPreferredSize() : _smodel.bounds;
    }

    /** Tile width in pixels. */
    protected static final int TWIDTH = 64;

    /** Tile height in pixels. */
    protected static final int THEIGHT = 48;

    /** Number of horizontal tiles in the scene. */
    protected static final int HTILES = 10;

    /** Number of vertical tiles in the scene. */
    protected static final int VTILES = 12;

    /** Origin vertical offset in pixels. */
    protected static final int VOFFSET = -(5 * THEIGHT);

    /** The offscreen image used for double-buffering. */
    protected Image _offimg;

    /** The graphics context for the offscreen image. */
    protected Graphics _offg;

    /** The scene view data model. */
    protected IsoSceneViewModel _smodel;

    /** The scene view we're managing. */
    protected SceneView _view;
}
