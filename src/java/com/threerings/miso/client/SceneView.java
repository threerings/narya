//
// $Id: SceneView.java,v 1.25 2002/02/19 01:24:59 mdb Exp $

package com.threerings.miso.scene;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

import com.threerings.media.sprite.Path;

/**
 * The scene view interface provides an interface to be implemented by
 * classes that provide a view of a given scene by drawing the scene
 * contents onto a particular GUI component.
 */
public interface SceneView
{
    /**
     * Scrolls the view by the requested number of pixels. As the view is
     * not responsible for maintaining the back buffer, this will simply
     * dirty the regions exposed by scrolling and update the view's
     * internal offsets. It also instructs the sprite manager to dirty the
     * scrolled bounds of all sprites in the view.
     */
    public void scrollView (int dx, int dy);

    /**
     * Renders the scene to the given graphics context.
     *
     * @param gfx the graphics context.
     * @param invalidRects the list of invalid regions to be repainted.
     */
    public void paint (Graphics2D gfx, List invalidRects);

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
     * Returns the object (sprite or object tile) over which the mouse is
     * currently hovering.
     */
    public Object getHoverObject ();
}
