//
// $Id: SwingSceneViewPanel.java,v 1.3 2002/01/22 18:58:52 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

import java.util.List;

import com.samskivert.util.Config;

import com.threerings.media.animation.AnimatedView;
import com.threerings.media.animation.AnimationManager;
import com.threerings.media.sprite.SpriteManager;

import com.threerings.miso.util.MisoUtil;

/**
 * The swing scene view panel is responsible for managing a {@link
 * SceneView}, rendering it to the screen, and handling view-related UI
 * events.
 *
 * <p> This class is nearly identical to {@link SceneViewPanel} excepting
 * that it extends {@link JComponent} rather than {@link
 * com.threerings.media.animation.AnimatedPanel}, which allows it to be
 * used in applications like the scene editor that may need to have menus
 * and tooltips displayed properly when overlapping the scene view, and
 * that do not depend on use of the animation manager to refresh the
 * display.
 */
public class SwingSceneViewPanel extends JComponent
    implements AnimatedView, IsoSceneViewModelListener
{
    /**
     * Constructs the scene view panel.
     */
    public SwingSceneViewPanel (Config config, SpriteManager spritemgr)
    {
        // create the data model for the scene view
        _viewmodel = new IsoSceneViewModel(config);

        // listen to the iso scene view model to receive notice when
        // the scene display has changed and needs must be repainted
        _viewmodel.addListener(this);

        // create an animation manager for this panel
        AnimationManager animmgr = new AnimationManager(spritemgr, this);

	// create the scene view
        _view = newSceneView(animmgr, spritemgr, _viewmodel);
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
	AnimationManager amgr, SpriteManager smgr, IsoSceneViewModel model)
    {
        return new IsoSceneView(amgr, smgr, model);
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
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);
        _view.paint(g);
    }

    // documentation inherited
    public void invalidateRects (List rects)
    {
        // pass the invalid rects on to our scene view
        _view.invalidateRects(rects);
    }

    // documentation inherited
    public void invalidateRect (Rectangle rect)
    {
        _view.invalidateRect(rect);
    }

    // documentation inherited
    public void paintImmediately ()
    {
        repaint();
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
