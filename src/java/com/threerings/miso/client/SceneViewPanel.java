//
// $Id: SceneViewPanel.java,v 1.2 2001/08/06 18:57:39 shaper Exp $

package com.threerings.miso.scene;

import java.awt.*;
import javax.swing.JPanel;

import com.threerings.miso.sprite.SpriteManager;
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
        _smodel = new IsoSceneModel();
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
    public void setScene (Scene scene)
    {
	_view.setScene(scene);
    }

    /**
     * Render the panel and the scene view to the given graphics object.
     */
    public void paintComponent (Graphics g)
    {
	super.paintComponent(g);
	_view.paint(g);
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

    /** The scene data model. */
    protected IsoSceneModel _smodel;

    /** The scene view we're managing. */
    protected SceneView _view;
}
