//
// $Id: IsoSprite.java,v 1.1 2002/11/28 03:42:17 mdb Exp $

package com.threerings.miso.scene;

/**
 * An interface implementable by sprites that know their tile coordinates
 * in the isometric view.
 */
public interface IsoSprite
{
    /**
     * Returns the sprite's location on the x-axis in tile coordinates.
     */
    public int getTileX ();

    /**
     * Returns the sprite's location on the y-axis in tile coordinates.
     */
    public int getTileY ();
}
