//
// $Id: MisoSceneModel.java,v 1.8 2002/05/17 19:06:23 ray Exp $

package com.threerings.miso.scene;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.samskivert.util.StringUtil;

import com.threerings.miso.Log;

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

    /** The viewport width in tiles. */
    public int vwidth;

    /** The viewport height in tiles. */
    public int vheight;

    /** The combined tile ids (tile set id and tile id)
     * in compressed format.
     * Don't go poking around in here, use the accessor methods. */
    public int[] baseTileIds;

    /** The combined tile ids (tile set id and tile id) of the files in
     * the object layer in (x, y, tile id) format. */
    public int[] objectTileIds;

    /** The action strings associated with the object tiles in the order
     * that the tiles are specified in the {@link #objectTileIds} array.
     * Elements of this array may be null but will be converted to the
     * empty string during serialization. */
    public String[] objectActions;

    /**
     * Get the fully-qualified tile id of the base tile at the specified
     * row and column.
     */
    public int getBaseTile (int col, int row)
    {
        int index = getIndex(col, row);
        return (index == -1) ? 0 : baseTileIds[index];
    }

    /**
     * Set the fully-qualified tile id of a base tile.
     *
     * @return false if the specified tile coordinates are outside
     * of the viewport and the tile was not saved.
     */
    public boolean setBaseTile (int col, int row, int fqBaseTileId)
    {
        int index = getIndex(col, row);
        if (index == -1) {
            return false;
        }
        baseTileIds[index] = fqBaseTileId;
        return true;
    }

    /**
     * Get the index into the baseTileIds[] for the specified
     * x and y coordinates, or return -1 if the specified coordinates
     * are outside of the viewable area.
     *
     * Assumption: The viewable area is centered and aligned as far
     *   to the top of the isometric scene as possible, such that
     *   the upper-left corner is at the point where the tiles
     *   (0, vwid) and (0, vwid-1) touch. The upper-right corner
     *   is at the point where the tiles (vwid-1, 0) and (vwid, 0)
     *   touch.
     *
     * The viewable area is made up of "fat" rows and "thin" rows. The
     * fat rows display one more tile than the thin rows because their
     * first and last tiles are halfway off the viewable area. The thin
     * rows are fully contained within the viewable area except for the
     * first and last thin rows, which display only their bottom and top
     * halves, respectively. Note that #fatrows == #thinrows - 1;
     */
    protected int getIndex (int x, int y)
    {
        // check to see if the index lies in one of the "fat" rows
        if (((x + y) & 1) == (vwidth & 1)) {

            int col = (vwidth + x - y) >> 1;
            int row = x - col;
            if ((col < 0) || (col > vwidth) ||
                (row < 0) || (row >= vheight)) {
                return -1; // out of view
            }

            return (vwidth + 1) * row + col;

        } else {
            // the index must be in a "thin" row
            int col = (vwidth + x - y - 1) >> 1;
            int row = x - col;
            if ((col < 0) || (col >= vwidth) ||
                (row < 0) || (row > vheight)) {
                return -1; // out of view
            }

            // we store the all the fat rows first, then all the thin
            // rows, the '(vwidth + 1) * vheight' is the size of all
            // the fat rows.
            return row * vwidth + col + (vwidth + 1) * vheight;
        }
    }

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        int otc = objectTileIds.length;
        int btc = baseTileIds.length;

        // write everything into a ByteBuffer, viewed as an IntBuffer and
        // then write the bytes from those operations out to the output
        // stream
        ByteBuffer bbuf = ByteBuffer.allocate(4*(btc + otc + 5));
        IntBuffer ibuf = bbuf.asIntBuffer();

        // insert the dimensions
        ibuf.put(width);
        ibuf.put(height);
        ibuf.put(vwidth);
        ibuf.put(vheight);
        ibuf.put(otc);

        // insert the layer data
        ibuf.put(baseTileIds);
        ibuf.put(objectTileIds);

        // now write the binary data out to the output stream
        out.write(bbuf.array());

        // next write out the object action strings
        int acount = otc/3;
        for (int i = 0; i < acount; i++) {
            out.writeUTF((objectActions[i] == null) ? "" : objectActions[i]);
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
        vwidth = in.readInt();
        vheight = in.readInt();
        int otc = in.readInt();

        // read in the base layer
        allocateBaseTileArray();
        for (int i = 0; i < baseTileIds.length; i++) {
            baseTileIds[i] = in.readInt();
        }

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
     * Convert an old school model to the new-style, baby.
     * TODO: Remove this method someday after we've converted all
     * our old scenes.
     */
    public void convertOldSchool (int vwidth, int vheight)
    {
        // sanity check
        if (baseTileIds.length != (width * height)) {
            Log.warning("This isn't an old-school scene model!");
            return;
        }

        this.vwidth = vwidth;
        this.vheight = vheight;

        // make a copy of the oldschool base tile ids
        int[] oldschool = baseTileIds;
        allocateBaseTileArray();

        int idx = 0;
        for (int yy=0; yy < height; yy++) {
            for (int xx=0; xx < width; xx++) {
                setBaseTile(xx, yy, oldschool[idx++]);
            }
        }
    }

    /**
     * Allocate the base tile array.
     */
    protected void allocateBaseTileArray ()
    {
        baseTileIds = new int[vwidth + vheight + ((vwidth * vheight) << 1)];
    }

    /**
     * Generates a string representation of this scene model.
     */
    public String toString ()
    {
        return "[width=" + width + ", height=" + height +
            ", vwidth=" + vwidth + ", vheight=" + vheight +
            ", baseTileIds=" + StringUtil.toString(baseTileIds) +
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
        populateBlankMisoSceneModel(model, 0, 0, 0, 0);
        return model;
    }

    /**
     * Creates and returns a blank scene model with the specified width
     * and height.
     */
    public static MisoSceneModel blankMisoSceneModel (
        int width, int height, int vwidth, int vheight)
    {
        MisoSceneModel model = new MisoSceneModel();
        populateBlankMisoSceneModel(model, width, height, vwidth, vheight);
        return model;
    }

    /**
     * Populates a blank scene model with blank values.
     */
    protected static void populateBlankMisoSceneModel (
        MisoSceneModel model, int width, int height, int vwidth, int vheight)
    {
        model.width = width;
        model.height = height;
        model.vwidth = vwidth;
        model.vheight = vheight;
        model.allocateBaseTileArray();
        model.objectTileIds = new int[0];
        model.objectActions = new String[0];
    }
}
