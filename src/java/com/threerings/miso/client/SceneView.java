//
// $Id: SceneView.java,v 1.8 2001/08/02 00:42:02 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.miso.tile.Tile;

import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * The SceneView interface provides an interface to be implemented by
 * classes that provide a view of a given scene by drawing the scene
 * contents onto a particular GUI component.
 */
public interface SceneView
{
    /**
     * Render the scene to the given graphics context.
     *
     * @param g the graphics context.
     */
    public void paint (Graphics g);

    /**
     * Set the scene that we're rendering.
     *
     * @param scene the scene to render in the view.
     */
    public void setScene (Scene scene);

    /**
     * Invalidate a list of rectangles in screen pixel coordinates in
     * the scene view for later repainting.
     *
     * @param rects the list of <code>java.awt.Rectangle</code> objects.
     */
    public void invalidateRects (ArrayList rects);
}
