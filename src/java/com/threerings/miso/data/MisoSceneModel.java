//
// $Id: MisoSceneModel.java,v 1.6 2002/05/16 02:25:19 ray Exp $

package com.threerings.miso.scene;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.samskivert.util.StringUtil;

/**
 * The scene model is the bare bones representation of the data for a
 * scene in the Miso system. From the scene model, one would create an
 * instance of {@link DisplayMisoScene}.
 */
public class MisoSceneModel
    implements Cloneable
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

    /** The action strings associated with the object tiles in the order
     * that the tiles are specified in the {@link #objectTileIds} array.
     * Elements of this array may be null but will be converted to the
     * empty string during serialization. */
    public String[] objectActions;

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        int tcount = width*height;
        int otc = objectTileIds.length;

        // write everything into a ByteBuffer, viewed as an IntBuffer and
        // then write the bytes from those operations out to the output
        // stream
        ByteBuffer bbuf = ByteBuffer.allocate(8*tcount + 4*otc + 4*3);
        IntBuffer ibuf = bbuf.asIntBuffer();

        // insert the dimensions
        ibuf.put(width);
        ibuf.put(height);
        ibuf.put(otc);

        // insert the layer data (except fringe)
        ibuf.put(baseTileIds);
        ibuf.put(objectTileIds);

        // now write the binary data out to the output stream
        out.write(bbuf.array());

        // next write out the object action strings
        int acount = otc/3;
        for (int i = 0; i < acount; i++) {
            String action = objectActions[i];
            out.writeUTF((action == null) ? "" : action);
        }
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        // we can read these directly because the byte order of the byte
        // buffer we created to write out the data is big endian which is
        // what the data input stream expects
        width = in.readInt();
        height = in.readInt();
        int otc = in.readInt();

        // read in the base layer
        int tcount = width*height;
        baseTileIds = new int[tcount];
        for (int i = 0; i < tcount; i++) {
            baseTileIds[i] = in.readInt();
        }

        // allocate, but don't read, the fringe layer
        fringeTileIds = new int[tcount];

        // read in the object layer
        objectTileIds = new int[otc];
        for (int i = 0; i < otc; i++) {
            objectTileIds[i] = in.readInt();
        }

        // read in the object action strings
        objectActions = new String[otc/3];
        for (int i = 0; i < otc/3; i++) {
            objectActions[i] = in.readUTF();
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
            ", objectTileIds=" + StringUtil.toString(objectTileIds) +
            ", objectActions=" + StringUtil.toString(objectActions) + "]";
    }

    /**
     * Creates a copy of this Miso scene model.
     */
    public Object clone ()
        throws CloneNotSupportedException
    {
        MisoSceneModel model = (MisoSceneModel)super.clone();
        model.baseTileIds = (int[])baseTileIds.clone();
        model.fringeTileIds = (int[])fringeTileIds.clone();
        model.objectTileIds = (int[])objectTileIds.clone();
        model.objectActions = (String[])objectActions.clone();
        return model;
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
        model.objectActions = new String[0];
    }
}
