//
// $Id: SceneViewPanel.java,v 1.33 2002/02/19 07:41:55 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.util.List;

import com.threerings.media.animation.AnimationManager;
import com.threerings.media.animation.AnimatedPanel;
import com.threerings.media.sprite.SpriteManager;

import com.threerings.miso.Log;
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
     * Constructs the scene view panel with the supplied view model.
     */
    public SceneViewPanel (IsoSceneViewModel model)
    {
        // create a sprite manager
        _spritemgr = new SpriteManager();

        // configure our animation manager with the sprite manager
        _animmgr.setSpriteManager(_spritemgr);

        // create the data model for the scene view
        _viewmodel = model;

        // listen to the iso scene view model to receive notice when
        // the scene display has changed and needs must be repainted
        _viewmodel.addListener(this);

	// create the scene view
        _view = newSceneView(_animmgr, _spritemgr, _viewmodel);

	// listen to mouse...
	addMouseListener(new MouseAdapter() {
            public void mouseExited (MouseEvent e) {
                _view.mouseExited(e);
                repaint();
            }
        });

        // ...and mouse motion events
	addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved (MouseEvent e) {
                if (_view.mouseMoved(e)) {
                    repaint();
                }
            }
            public void mouseDragged (MouseEvent e) {
                if (_view.mouseMoved(e)) {
                    repaint();
                }
            }
        });
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
    protected SceneView newSceneView (
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
    public void validate ()
    {
        super.validate();

        // figure out our translations
        Dimension size = getSize();
        _tx = (_viewmodel.bounds.width - size.width)/2;
        _ty = (_viewmodel.bounds.height - size.height)/2;

        // tell the sprite manager about the offsets so that it can
        // communicate valid dirty rectangles to the animation manager
        _spritemgr.setViewportOffset(_tx, _ty);
    }

    /**
     * If we're scrolling, we need to pass the word on to our scene view.
     */
    protected void viewWillScroll (int dx, int dy)
    {
        _view.viewWillScroll(dx, dy);
    }

    /**
     * We overload this to translate mouse events into the proper
     * coordinates before they are dispatched to any of the mouse
     * listeners.
     */
    protected void processMouseEvent (MouseEvent event)
    {
        event.translatePoint(_tx, _ty);
        super.processMouseEvent(event);
    }

    /**
     * We overload this to translate mouse events into the proper
     * coordinates before they are dispatched to any of the mouse
     * listeners.
     */
    protected void processMouseMotionEvent (MouseEvent event)
    {
        event.translatePoint(_tx, _ty);
        super.processMouseMotionEvent(event);
    }

    // documentation inherited
    protected void render (Graphics2D gfx, List invalidRects)
    {
        // we have to translate all of the invalid rects into our
        // translated coordinate space before passing them on to the
        // underlying view code
        int isize = invalidRects.size();
        for (int i = 0; i < isize; i++) {
            ((Rectangle)invalidRects.get(i)).translate(_tx, _ty);
        }

        // translate into happy space
        gfx.translate(-_tx, -_ty);

        // render the view
        _view.paint(gfx, invalidRects);

        // give derived classes a chance to render on top of the view
        renderOnView(gfx, invalidRects);

        // translate back out of happy space
        gfx.translate(_tx, _ty);

        // now we translate them back out (because the animated panel
        // needs to use them as they were); we translate in and out rather
        // than copy them to save on object creation overhead
        for (int i = 0; i < isize; i++) {
            ((Rectangle)invalidRects.get(i)).translate(-_tx, -_ty);
        }
    }

    /**
     * Provides an opportunity for derived classes to render things while
     * under the influence of the proper coordinate translations. This is
     * called after the view is rendered, so things drawn here appear on
     * top of the scene view.
     */
    protected void renderOnView (Graphics2D gfx, List invalidRects)
    {
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

    /** A reference to the active sprite manager. */
    protected SpriteManager _spritemgr;

    /** Our rendering translation. */
    protected int _tx, _ty;
}
