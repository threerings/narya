//
// $Id: SceneView.java,v 1.7 2001/07/23 18:52:51 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.miso.tile.Tile;

import java.awt.Component;
import java.awt.Graphics;

/**
 * The SceneView interface provides an interface to be implemented by
 * classes that provide a view of a given scene by drawing the scene
 * contents onto a particular GUI component.
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
}
