//
// $Id: MisoSceneModel.java,v 1.1 2001/11/18 04:09:22 mdb Exp $

package com.threerings.miso.scene;

/**
 * The scene model is the bare bones representation of the data for a
 * scene in the Miso system. From the scene model, one would create an
 * instance of {@link DisplayMisoScene}.
 */
public class MisoSceneModel
{
    /** The width of the scene in tile units. */
    public int width;

    /** The height of the scene in tile units. */
    public int height;

    /** The combined tile ids (tile set id and tile id) of the tiles in
     * the base layer, in row-major order. */
    public int[] baseTileIds;

    /** The combined tile ids (tile set id and tile id) of the tiles in
     * the fringe layer, in row-major order. */
    public int[] fringeTileIds;

    /** The combined tile ids (tile set id and tile id) of the files in
     * the object layer in (x, y, tile id) format. */
    public int[] objectTileIds;
}
