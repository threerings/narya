//
// $Id: SceneViewPanel.java,v 1.21 2001/11/29 20:33:16 mdb Exp $

package com.threerings.miso.scene;

import java.awt.*;
import java.util.List;
import javax.swing.*;

import com.samskivert.util.Config;

import com.threerings.media.sprite.*;
import com.threerings.miso.util.MisoUtil;

/**
 * The scene view panel is responsible for managing a {@link
 * SceneView}, rendering it to the screen, and handling view-related
 * UI events.
 */
public class SceneViewPanel extends AnimatedPanel
    implements IsoSceneViewModelListener
{
    /**
     * Constructs the scene view panel.
     */
    public SceneViewPanel (Config config, SpriteManager spritemgr)
    {
        // create the data model for the scene view
        _viewmodel = new IsoSceneViewModel(config);

        // listen to the iso scene view model to receive notice when
        // the scene display has changed and needs must be repainted
        _viewmodel.addListener(this);

	// create the scene view
        _view = newSceneView(spritemgr, _viewmodel);
    }

    /**
     * Gets the iso scene view model associated with this panel.
     */
    public IsoSceneViewModel getModel ()
    {
        return _viewmodel;
    }

    /**
     * Constructs the underlying scene view implementation.
     */
    protected IsoSceneView newSceneView (
	SpriteManager smgr, IsoSceneViewModel model)
    {
        return new IsoSceneView(smgr, model);
    }

    /**
     * Sets the scene managed by the panel.
     */
    public void setScene (DisplayMisoScene scene)
    {
	_view.setScene(scene);
    }

    /**
     * Gets the scene managed by the panel.
     */
    public SceneView getSceneView ()
    {
	return _view;
    }

    // documentation inherited
    protected void render (Graphics g)
    {
        _view.paint(g);
    }

    // documentation inherited
    public void invalidateRects (DirtyRectList rects)
    {
        // pass the invalid rects on to our scene view
        _view.invalidateRects(rects);
    }

    /**
     * Returns the desired size for the panel based on the requested
     * and calculated bounds of the scene view.
     */
    public Dimension getPreferredSize ()
    {
        Dimension psize = (_viewmodel == null) ?
            super.getPreferredSize() : _viewmodel.bounds.getSize();
	return psize;
    }

    // documentation inherited
    public void viewChanged (int event)
    {
        // update the scene view display
        repaint();
    }

    /** The scene view data model. */
    protected IsoSceneViewModel _viewmodel;

    /** The scene view we're managing. */
    protected SceneView _view;
}
