//
// $Id: SceneView.java,v 1.3 2001/07/16 22:12:01 shaper Exp $

package com.threerings.cocktail.miso.scene;

import com.threerings.cocktail.miso.tile.Tile;

import java.awt.Component;
import java.awt.Graphics;

/**
 * An interface to be implemented by classes that provide a view of a
 * given scene by drawing the scene contents onto a particular GUI
 * component.
 */
public interface SceneView
{
    /**
     * Render the scene to the given graphics context.
     */
    public void paint (Graphics g);

    /**
     * Set a tile to be highlighted when the scene is rendered.
     */
    public void setHighlightedTile (int x, int y);

    /**
     * Set the scene that we're rendering.
     */
    public void setScene (Scene scene);

    /**
     * Set the target component to which we're rendering.
     */
    public void setTarget (Component target);

    /**
     * Set the tile at the specified location and layer in the scene.
     */
    public void setTile (int x, int y, int lnum, Tile tile);
}
