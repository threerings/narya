//
// $Id: MisoSceneModel.java,v 1.13 2003/04/12 02:14:10 mdb Exp $

package com.threerings.miso.data;

import java.util.ArrayList;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.IntListUtil;
import com.samskivert.util.ListUtil;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.media.util.MathUtil;

/**
 * Contains basic information for a miso scene model that is shared among
 * every specialized model implementation.
 */
public class MisoSceneModel extends SimpleStreamableObject
    implements Cloneable
{
    /** The width of this scene or section of the scene (depending on the
     * model implementation), in tile units. */
    public short width;

    /** The height of this scene or section of the scene (depending on the
     * model implementation), in tile units. */
    public short height;

    /** The combined tile ids (tile set id and tile id) of the
     * "uninteresting" tiles in the object layer. */
    public int[] objectTileIds;

    /** The x coordinate of the "uninteresting" tiles in the object
     * layer. */
    public short[] objectXs;

    /** The y coordinate of the "uninteresting" tiles in the object
     * layer. */
    public short[] objectYs;

    /** Information records for the "interesting" objects in the object
     * layer. */
    public ObjectInfo[] objectInfo;

    /**
     * Creates a completely uninitialized model suitable for little more
     * than unserialization.
     */
    public MisoSceneModel ()
    {
    }

    /**
     * Creates a blank model with the specified dimensions.
     */
    public MisoSceneModel (int width, int height)
    {
        this.width = (short)MathUtil.bound(
            Short.MIN_VALUE, width, Short.MAX_VALUE);
        this.height = (short)MathUtil.bound(
            Short.MIN_VALUE, height, Short.MAX_VALUE);

        // start with zero-length object arrays
        objectTileIds = new int[0];
        objectXs = new short[0];
        objectYs = new short[0];
        objectInfo = new ObjectInfo[0];
    }

    /**
     * Adds an object to this model.
     */
    public void addObject (ObjectInfo info)
    {
        if (info.isInteresting()) {
            objectInfo = (ObjectInfo[])ArrayUtil.append(objectInfo, info);
        } else {
            objectTileIds = ArrayUtil.append(objectTileIds, info.tileId);
            objectXs = ArrayUtil.append(objectXs, (short)info.x);
            objectYs = ArrayUtil.append(objectYs, (short)info.y);
        }
    }

    /**
     * Removes an object from this model.
     */
    public void removeObject (ObjectInfo info)
    {
        // look for it in the interesting info array
        int oidx = ListUtil.indexOfEqual(objectInfo, info);
        if (oidx != -1) {
            objectInfo = (ObjectInfo[])ArrayUtil.splice(objectInfo, oidx, 1);
        }

        // look for it in the uninteresting arrays
        oidx = IntListUtil.indexOf(objectTileIds, info.tileId);
        if (oidx != -1) {
            objectTileIds = ArrayUtil.splice(objectTileIds, oidx, 1);
            objectXs = ArrayUtil.splice(objectXs, oidx, 1);
            objectYs = ArrayUtil.splice(objectYs, oidx, 1);
        }
    }

    /**
     * Creates a copy of this scene model.
     */
    public Object clone ()
    {
        try {
            MisoSceneModel model = (MisoSceneModel)super.clone();
            model.objectTileIds = (int[])objectTileIds.clone();
            model.objectXs = (short[])objectXs.clone();
            model.objectYs = (short[])objectYs.clone();
            model.objectInfo = (ObjectInfo[])objectInfo.clone();
            return model;
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("MisoSceneModel.clone: " + cnse);
        }
    }

    /**
     * Populates the interesting and uninteresting parts of a miso scene
     * model given lists of {@link ObjectInfo} records for each.
     */
    public static void populateObjects (MisoSceneModel model,
                                        ArrayList ilist, ArrayList ulist)
    {
        // set up the uninteresting arrays
        int ucount = ulist.size();
        model.objectTileIds = new int[ucount];
        model.objectXs = new short[ucount];
        model.objectYs = new short[ucount];
        for (int ii = 0; ii < ucount; ii++) {
            ObjectInfo info = (ObjectInfo)ulist.get(ii);
            model.objectTileIds[ii] = info.tileId;
            model.objectXs[ii] = (short)info.x;
            model.objectYs[ii] = (short)info.y;
        }

        // set up the interesting array
        int icount = ilist.size();
        model.objectInfo = new ObjectInfo[icount];
        ilist.toArray(model.objectInfo);
    }
}
