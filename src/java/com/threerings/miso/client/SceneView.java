//
// $Id: SceneView.java,v 1.14 2001/08/21 20:02:39 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Graphics;
import java.util.List;

import com.threerings.media.sprite.Path;
import com.threerings.media.tile.Tile;

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
    public void invalidateRects (List rects);

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
    public void setScene (MisoScene scene);

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
    public Path getPath (AmbulatorySprite sprite, int x, int y);
}
