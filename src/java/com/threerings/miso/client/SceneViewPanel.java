//
// $Id: SceneViewPanel.java,v 1.41 2002/06/18 22:38:12 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.util.List;
import com.samskivert.util.StringUtil;

import com.threerings.media.FrameManager;
import com.threerings.media.VirtualMediaPanel;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.SpriteManager;

import com.threerings.miso.Log;
import com.threerings.miso.scene.util.IsoUtil;

/**
 * The scene view panel is responsible for managing a {@link
 * SceneView}, rendering it to the screen, and handling view-related
 * UI events.
 */
public class SceneViewPanel extends VirtualMediaPanel
    implements IsoSceneViewModelListener
{
    /**
     * Constructs the scene view panel with the supplied view model.
     */
    public SceneViewPanel (FrameManager framemgr, IsoSceneViewModel model)
    {
        super(framemgr);

        // we're going to want to be opaque
        setOpaque(true);

        // create the data model for the scene view
        _viewmodel = model;

        // listen to the iso scene view model to receive notice when
        // the scene display has changed and needs must be repainted
        _viewmodel.addListener(this);

	// create the scene view
        _view = newSceneView(_spritemgr, _viewmodel);

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
        SpriteManager smgr, IsoSceneViewModel model)
    {
        return new IsoSceneView(smgr, model, _remgr);
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

    /**
     * Adds the supplied sprite to the scene, setting up its scene
     * coordinates if it is a {@link MisoCharacterSprite}.
     */
    public void addSprite (Sprite sprite)
    {
        if (sprite instanceof MisoCharacterSprite) {
            // set up the sprite's tile coordinates
            IsoUtil.setSpriteSceneLocation(
                _viewmodel, (MisoCharacterSprite)sprite);
        }

        super.addSprite(sprite);
    }

    // documentation inherited
    public void doLayout ()
    {
        super.doLayout();

        // figure out our viewport offsets
        Dimension size = getSize();

        // start out centered in the display
        setViewLocation((_viewmodel.bounds.width - size.width)/2,
                        (_viewmodel.bounds.height - size.height)/2);

//         Log.info("Size: " + StringUtil.toString(size) +
//                  ", vsize: " + StringUtil.toString(_viewmodel.bounds) +
//                  ", nx: " + _nx + ", ny: " + _ny + ".");
    }

    // documentation inherited
    protected void paintBetween (Graphics2D gfx, Rectangle dirty)
    {
        _view.paint(gfx, dirty);
    }

    /**
     * We don't want sprites rendered using the standard mechanism because
     * we intersperse them with objects in our scene and need to manage
     * their z-order.
     */
    protected void paintBits (Graphics2D gfx, int layer, Rectangle dirty)
    {
        _animmgr.renderAnimations(gfx, layer, dirty);
    }

    /**
     * Returns the desired size for the panel based on the requested
     * and calculated bounds of the scene view.
     */
    public Dimension getPreferredSize ()
    {
	return (_viewmodel == null) ? super.getPreferredSize() :
            _viewmodel.bounds.getSize();
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
