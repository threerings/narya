//
// $Id: SceneViewPanel.java,v 1.16 2001/10/17 22:21:22 shaper Exp $

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
{
    /**
     * Constructs the scene view panel.
     */
    public SceneViewPanel (Config config, SpriteManager spritemgr)
    {
        // create the data model for the scene view
        _smodel = new IsoSceneViewModel(config);

	// create the scene view
        _view = newSceneView(spritemgr, _smodel);
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
    public void setScene (MisoScene scene)
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
        Dimension psize = (_smodel == null) ?
            super.getPreferredSize() : _smodel.bounds;
	return psize;
    }

    /** The scene view data model. */
    protected IsoSceneViewModel _smodel;

    /** The scene view we're managing. */
    protected SceneView _view;
}
