//
// $Id: SceneView.java,v 1.1 2001/07/12 22:38:03 shaper Exp $

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
     * Set the scene that we're rendering.
     */
    public void setScene (Scene scene);
    
    /**
     * Set the target component to which we're rendering.
     */
    public void setTarget (Component target);
}
