//
// $Id: SceneView.java,v 1.2 2001/07/16 00:45:06 shaper Exp $

package com.threerings.cocktail.miso.scene;

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
}
