//
// $Id: SceneView.java,v 1.21 2002/01/08 22:16:59 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import com.threerings.media.sprite.DirtyRectList;
import com.threerings.media.sprite.Path;

/**
 * The scene view interface provides an interface to be implemented by
 * classes that provide a view of a given scene by drawing the scene
 * contents onto a particular GUI component.
 */
public interface SceneView
{
    /**
     * Invalidate a list of rectangles in screen pixel coordinates in the
     * scene view for later repainting.
     *
     * @param rects the list of {@link java.awt.Rectangle} objects.
     */
    public void invalidateRects (DirtyRectList rects);

    /**
     * Invalidate a rectangle in screen pixel coordinates in the scene
     * view for later repainting.
     *
     * @param rect the {@link java.awt.Rectangle} object.
     */
    public void invalidateRect (Rectangle rect);

    /**
     * Renders the scene to the given graphics context.
     *
     * @param g the graphics context.
     */
    public void paint (Graphics g);

    /**
     * Sets the scene that we're rendering.
     *
     * @param scene the scene to render in the view.
     */
    public void setScene (DisplayMisoScene scene);

    /**
     * Returns a {@link Path} object detailing a valid path for the
     * given sprite to take in the scene to get from its current
     * position to the destination position.
     *
     * @param sprite the sprite to move.
     * @param x the destination x-position in pixel coordinates.
     * @param y the destination y-position in pixel coordinates.
     *
     * @return the sprite's path, or null if no valid path exists.
     */
    public Path getPath (MisoCharacterSprite sprite, int x, int y);

    /**
     * Returns screen coordinates given the specified full coordinates.
     */
    public Point getScreenCoords (int x, int y);
}
