//
// $Id: SceneView.java,v 1.33 2003/01/31 23:10:45 mdb Exp $

package com.threerings.miso.client;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.Path;

/**
 * The scene view interface provides an interface to be implemented by
 * classes that provide a view of a given scene by drawing the scene
 * contents onto a particular GUI component.
 */
public interface SceneView
{
    /**
     * Renders an invalid porition of the scene to the given graphics
     * context.
     *
     * @param gfx the graphics context.
     * @param invalidRect the invalid region to be repainted.
     */
    public void paint (Graphics2D gfx, Rectangle invalidRect);

    /**
     * Sets the scene that we're rendering.
     *
     * @param scene the scene to render in the view.
     */
    public void setScene (DisplayMisoScene scene);

    /**
     * Returns the scene being rendered.
     */
    public DisplayMisoScene getScene ();

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
    public Path getPath (Sprite sprite, int x, int y);

    /**
     * Returns screen coordinates given the specified full coordinates.
     */
    public Point getScreenCoords (int x, int y);

    /**
     * Returns full coordinates given the specified screen coordinates.
     */
    public Point getFullCoords (int x, int y);

    /**
     * Must be called by the containing panel when the mouse moves over
     * the view.
     *
     * @return true if a repaint is required, false if not.
     */
    public boolean mouseMoved (MouseEvent e);

    /**
     * Must be called by the containing panel when the mouse exits the
     * view.
     */
    public void mouseExited (MouseEvent e);

    /**
     * Returns information about the object over which the mouse is
     * currently hovering (either a {@link DisplayObjectInfo} or a {@link
     * Sprite}), or null if the mouse is not hovering over anything of
     * interest.
     */
    public Object getHoverObject ();
}
