//
// $Id: SceneView.java,v 1.10 2001/08/14 23:35:22 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Component;
import java.awt.Graphics;

import com.threerings.media.sprite.*;
import com.threerings.miso.tile.Tile;

/**
 * The SceneView interface provides an interface to be implemented by
 * classes that provide a view of a given scene by drawing the scene
 * contents onto a particular GUI component.
 */
public interface SceneView extends AnimatedView
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
