//
// $Id: SceneViewPanel.java,v 1.11 2001/08/22 02:14:57 mdb Exp $

package com.threerings.miso.scene;

import java.awt.*;
import java.util.List;
import javax.swing.JPanel;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.TileManager;

/**
 * The <code>SceneViewPanel</code> class is responsible for managing a
 * <code>SceneView</code>, rendering it to the screen, and handling
 * view-related UI events.
 */
public class SceneViewPanel
    extends JPanel implements AnimatedView
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

    // documentation inherited
    public void invalidateRects (DirtyRectList rects)
    {
        // pass the invalid rects on to our scene view
        _view.invalidateRects(rects);
    }

    /**
     * Paints this panel immediately. Since we know that we are always
     * opaque and not dependent on Swing's double-buffering, we bypass the
     * antics that <code>JComponent.paintImmediately()</code> performs in
     * the interest of better performance.
     */
    public void paintImmediately ()
    {
        Graphics g = null;

        try {
            Graphics pcg = getGraphics();
            g = pcg.create();
            pcg.dispose();
            paint(g);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void paintComponent (Graphics g)
    {
	render(g);

//          Rectangle bounds = getBounds();
//          g.drawRect(bounds.x, bounds.y, bounds.width-1, bounds.height-1);
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
        Dimension psize = (_smodel == null) ?
            super.getPreferredSize() : _smodel.bounds;
	return psize;
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
