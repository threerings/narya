//
// $Id: SimpleMisoSceneModel.java,v 1.7 2004/08/27 02:20:06 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.miso.data;

import java.awt.Rectangle;
import java.util.ArrayList;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.IntListUtil;
import com.samskivert.util.ListUtil;

import com.threerings.media.util.MathUtil;
import com.threerings.miso.util.ObjectSet;

/**
 * Contains miso scene data for a scene that is assumed to be reasonably
 * simple and small, such that all base tile data for the entire scene can
 * be stored in a single contiguous array.
 *
 * <p> Additionally, it makes the assumption that the single model will be
 * used to display a scene on a rectangular screen (dimensions defined by
 * {@link #vwidth} and {@link #vheight}) and further optimizes the base
 * tile array to obviate the need to store tile data for things that fall
 * outside the bounds of the screen.
 */
public class SimpleMisoSceneModel extends MisoSceneModel
{
    /** The width of this scene in tiles. */
    public short width;

    /** The height of this scene in tiles. */
    public short height;

    /** The viewport width in tiles. */
    public int vwidth;

    /** The viewport height in tiles. */
    public int vheight;

    /** The combined tile ids (tile set id and tile id) in compressed
     * format.  Don't go poking around in here, use the accessor
     * methods. */
    public int[] baseTileIds;

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
    public SimpleMisoSceneModel ()
    {
    }

    /**
     * Creates a blank scene model with the specified dimensions.
     */
    public SimpleMisoSceneModel (int width, int height, int vwidth, int vheight)
    {
        this.width = (short)MathUtil.bound(
            Short.MIN_VALUE, width, Short.MAX_VALUE);
        this.height = (short)MathUtil.bound(
            Short.MIN_VALUE, height, Short.MAX_VALUE);
        this.vwidth = vwidth;
        this.vheight = vheight;
        allocateBaseTileArray();

        // start with zero-length object arrays
        objectTileIds = new int[0];
        objectXs = new short[0];
        objectYs = new short[0];
        objectInfo = new ObjectInfo[0];
    }

    // documentation inherited
    public int getBaseTileId (int col, int row)
    {
        int index = getIndex(col, row);
        return (index == -1) ? 0 : baseTileIds[index];
    }

    // documentation inherited
    public boolean setBaseTile (int fqBaseTileId, int col, int row)
    {
        int index = getIndex(col, row);
        if (index == -1) {
            return false;
        }
        baseTileIds[index] = fqBaseTileId;
        return true;
    }

    // documentation inherited
    public void getObjects (Rectangle region, ObjectSet set)
    {
        // first look for intersecting interesting objects
        for (int ii = 0; ii < objectInfo.length; ii++) {
            ObjectInfo info = objectInfo[ii];
            if (region.contains(info.x, info.y)) {
                set.insert(info);
            }
        }

        // now look for intersecting non-interesting objects
        for (int ii = 0; ii < objectTileIds.length; ii++) {
            int x = objectXs[ii], y = objectYs[ii];
            if (region.contains(x, y)) {
                set.insert(new ObjectInfo(objectTileIds[ii], x, y));
            }
        }
    }

    // documentation inherited
    public boolean addObject (ObjectInfo info)
    {
        if (info.isInteresting()) {
            objectInfo = (ObjectInfo[])ArrayUtil.append(objectInfo, info);
        } else {
            objectTileIds = ArrayUtil.append(objectTileIds, info.tileId);
            objectXs = ArrayUtil.append(objectXs, (short)info.x);
            objectYs = ArrayUtil.append(objectYs, (short)info.y);
        }
        return true;
    }

    // documentation inherited
    public void updateObject (ObjectInfo info)
    {
        // not efficient, but this is only done in editing situations
        removeObject(info);
        addObject(info);
    }

    // documentation inherited
    public boolean removeObject (ObjectInfo info)
    {
        // look for it in the interesting info array
        int oidx = ListUtil.indexOfEqual(objectInfo, info);
        if (oidx != -1) {
            objectInfo = (ObjectInfo[])ArrayUtil.splice(objectInfo, oidx, 1);
            return true;
        }

        // look for it in the uninteresting arrays
        oidx = IntListUtil.indexOf(objectTileIds, info.tileId);
        if (oidx != -1) {
            objectTileIds = ArrayUtil.splice(objectTileIds, oidx, 1);
            objectXs = ArrayUtil.splice(objectXs, oidx, 1);
            objectYs = ArrayUtil.splice(objectYs, oidx, 1);
            return true;
        }

        return false;
    }

    // documentation inherited
    public Object clone ()
    {
        SimpleMisoSceneModel model = (SimpleMisoSceneModel)super.clone();
        model.baseTileIds = (int[])baseTileIds.clone();
        model.objectTileIds = (int[])objectTileIds.clone();
        model.objectXs = (short[])objectXs.clone();
        model.objectYs = (short[])objectYs.clone();
        model.objectInfo = (ObjectInfo[])objectInfo.clone();
        return model;
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

    /**
     * Allocate the base tile array.
     */
    protected void allocateBaseTileArray ()
    {
        baseTileIds = new int[vwidth + vheight + ((vwidth * vheight) << 1)];
    }

    /**
     * Populates the interesting and uninteresting parts of a miso scene
     * model given lists of {@link ObjectInfo} records for each.
     */
    public static void populateObjects (SimpleMisoSceneModel model,
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
