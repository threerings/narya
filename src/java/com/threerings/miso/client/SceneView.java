//
// $Id: SceneView.java,v 1.9 2001/08/02 20:43:03 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;

import com.threerings.miso.sprite.Path;
import com.threerings.miso.sprite.Sprite;
import com.threerings.miso.tile.Tile;

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

    /**
     * Return a Path object detailing a valid path for the given
     * sprite to take in the scene to get from its current position to
     * the destination position.
     *
     * @param sprite the sprite to move.
     * @param x the destination x-position in pixel coordinates.
     * @param y the destination y-position in pixel coordinates.
     *
     * @return the sprite's path or null if no valid path exists.
     */
    public Path getPath (Sprite sprite, int x, int y);
}
