//
// $Id: MisoSceneModel.java,v 1.3 2001/11/30 21:54:34 mdb Exp $

package com.threerings.miso.scene;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeInt(width);
        out.writeInt(height);

        // write out the base and fringe layers
        int tcount = width*height;
        for (int i = 0; i < tcount; i++) {
            out.writeInt(baseTileIds[i]);
            out.writeInt(fringeTileIds[i]);
        }

        // write out the object layer
        int otc = objectTileIds.length;
        out.writeInt(otc);
        for (int i = 0; i < otc; i++) {
            out.writeInt(objectTileIds[i]);
        }
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        width = in.readInt();
        height = in.readInt();

        // read in the base and fringe layers
        int tcount = width*height;
        baseTileIds = new int[tcount];
        fringeTileIds = new int[tcount];
        for (int i = 0; i < tcount; i++) {
            baseTileIds[i] = in.readInt();
            fringeTileIds[i] = in.readInt();
        }

        // read in the object layer
        int otc = in.readInt();
        objectTileIds = new int[otc];
        for (int i = 0; i < otc; i++) {
            objectTileIds[i] = in.readInt();
        }
    }

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
