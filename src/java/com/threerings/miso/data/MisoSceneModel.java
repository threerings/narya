//
// $Id: MisoSceneModel.java,v 1.2 2001/11/29 00:17:14 mdb Exp $

package com.threerings.miso.scene;

import com.samskivert.util.StringUtil;

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

    /**
     * Generates a string representation of this scene model.
     */
    public String toString ()
    {
        return "[width=" + width + ", height=" + height +
            ", baseTileIds=" + StringUtil.toString(baseTileIds) +
            ", fringeTileIds=" + StringUtil.toString(fringeTileIds) +
            ", objectTileIds=" + StringUtil.toString(objectTileIds) + "]";
    }

    /**
     * Creates and returns a blank scene model (with zero width and
     * height).
     */
    public static MisoSceneModel blankMisoSceneModel ()
    {
        MisoSceneModel model = new MisoSceneModel();
        populateBlankMisoSceneModel(model, 0, 0);
        return model;
    }

    /**
     * Creates and returns a blank scene model with the specified width
     * and height.
     */
    public static MisoSceneModel blankMisoSceneModel (int width, int height)
    {
        MisoSceneModel model = new MisoSceneModel();
        populateBlankMisoSceneModel(model, width, height);
        return model;
    }

    /**
     * Populates a blank scene model with blank values.
     */
    protected static void populateBlankMisoSceneModel (
        MisoSceneModel model, int width, int height)
    {
        model.width = width;
        model.height = height;
        model.baseTileIds = new int[width*height];
        model.fringeTileIds = new int[width*height];
        model.objectTileIds = new int[0];
    }
}
