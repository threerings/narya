//
// $Id: SparseMisoSceneModel.java,v 1.3 2003/04/20 00:05:14 mdb Exp $

package com.threerings.miso.data;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.IntListUtil;
import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.media.util.MathUtil;
import com.threerings.util.StreamableHashIntMap;

import com.threerings.miso.Log;
import com.threerings.miso.util.ObjectSet;

/**
 * Contains miso scene data that is broken up into NxN tile sections.
 */
public class SparseMisoSceneModel extends MisoSceneModel
{
    /** An interface that allows external entities to "visit" and inspect
     * every object in this scene. */
    public static interface ObjectVisitor
    {
        /** Called for each object in the scene, interesting and not. */
        public void visit (int tileId, int x, int y);
    }

    /** Contains information on a section of this scene. This is only
     * public so that the scene model parser can do its job, so don't go
     * poking around in here. */
    public static class Section extends SimpleStreamableObject
        implements Cloneable
    {
        /** The tile coordinate of our upper leftmost tile. */
        public short x, y;

        /** The width of this section in tiles. */
        public int width;

        /** The combined tile ids (tile set id and tile id) for our
         * section (in row major order). */
        public int[] baseTileIds;

        /** The combined tile ids (tile set id and tile id) of the
         * "uninteresting" tiles in the object layer. */
        public int[] objectTileIds = new int[0];

        /** The x coordinate of the "uninteresting" tiles in the object
         * layer. */
        public short[] objectXs = new short[0];

        /** The y coordinate of the "uninteresting" tiles in the object
         * layer. */
        public short[] objectYs = new short[0];

        /** Information records for the "interesting" objects in the
         * object layer. */
        public ObjectInfo[] objectInfo = new ObjectInfo[0];

        /**
         * Creates a blank section instance, suitable for unserialization
         * or configuration by the XML scene parser.
         */
        public Section ()
        {
        }

        /**
         * Creates a new scene section with the specified dimensions.
         */
        public Section (short x, short y, short width, short height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            baseTileIds = new int[width*height];
        }

        public int getBaseTileId (int col, int row) {
//             if (col < x || col >= (x+width) || row < y || row >= (y+width)) {
//                 Log.warning("Requested bogus tile +" + col + "+" + row +
//                             " from " + this + ".");
//                 return -1;
//             } else {
                return baseTileIds[(row-y)*width+(col-x)];
//             }
        }

        public void setBaseTile (int col, int row, int fqBaseTileId) {
            baseTileIds[(row-y)*width+(col-x)] = fqBaseTileId;
        }

        public void addObject (ObjectInfo info) {
            if (info.isInteresting()) {
                objectInfo = (ObjectInfo[])ArrayUtil.append(objectInfo, info);
            } else {
                objectTileIds = ArrayUtil.append(objectTileIds, info.tileId);
                objectXs = ArrayUtil.append(objectXs, (short)info.x);
                objectYs = ArrayUtil.append(objectYs, (short)info.y);
            }
        }

        public boolean removeObject (ObjectInfo info) {
            // look for it in the interesting info array
            int oidx = ListUtil.indexOfEqual(objectInfo, info);
            if (oidx != -1) {
                objectInfo = (ObjectInfo[])
                    ArrayUtil.splice(objectInfo, oidx, 1);
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

        public void getObjects (Rectangle region, ObjectSet set) {
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

        public Object clone () {
            try {
                Section section = (Section)super.clone();
                section.baseTileIds = (int[])baseTileIds.clone();
                section.objectTileIds = (int[])objectTileIds.clone();
                section.objectXs = (short[])objectXs.clone();
                section.objectYs = (short[])objectYs.clone();
                section.objectInfo = (ObjectInfo[])objectInfo.clone();
                return section;
            } catch (CloneNotSupportedException cnse) {
                throw new RuntimeException(
                    "SparseMisoSceneModel.Section.clone: " + cnse);
            }
        }

        public String toString () {
            return ((width == 0) ? "<no bounds>" :
                    (width + "x" + (baseTileIds.length/width))) +
                "+" + x + "+" + y +
                ":" + objectInfo.length + ":" + objectTileIds.length;
        }
    }

    /** The dimensions of a section of our scene. */
    public short swidth, sheight;

    /**
     * Creates a scene model with the specified bounds.
     *
     * @param swidth the width of a single section (in tiles).
     * @param sheight the height of a single section (in tiles).
     */
    public SparseMisoSceneModel (int swidth, int sheight)
    {
        this.swidth = (short)swidth;
        this.sheight = (short)sheight;
    }

    /**
     * Creates a blank model suitable for unserialization.
     */
    public SparseMisoSceneModel ()
    {
    }

    /**
     * Adds all interesting {@link ObjectInfo} records in this scene to
     * the supplied list.
     */
    public void getInterestingObjects (ArrayList list)
    {
        for (Iterator iter = getSections(); iter.hasNext(); ) {
            Section sect = (Section)iter.next();
            for (int oo = 0; oo < sect.objectInfo.length; oo++) {
                list.add(sect.objectInfo[oo]);
            }
        }
    }

    /**
     * Informs the supplied visitor of each object in this scene.
     */
    public void visitObjects (ObjectVisitor visitor)
    {
        for (Iterator iter = getSections(); iter.hasNext(); ) {
            Section sect = (Section)iter.next();
            for (int oo = 0; oo < sect.objectInfo.length; oo++) {
                ObjectInfo oinfo = sect.objectInfo[oo];
                visitor.visit(oinfo.tileId, oinfo.x, oinfo.y);
            }
            for (int oo = 0; oo < sect.objectTileIds.length; oo++) {
                visitor.visit(sect.objectTileIds[oo],
                              sect.objectXs[oo], sect.objectYs[oo]);
            }
        }
    }

    // documentation inherited
    public int getBaseTileId (int col, int row)
    {
        Section sec = getSection(col, row, false);
        return (sec == null) ? -1 : sec.getBaseTileId(col, row);
    }

    // documentation inherited
    public boolean setBaseTile (int fqBaseTileId, int col, int row)
    {
        getSection(col, row, true).setBaseTile(col, row, fqBaseTileId);
        return true;
    }

    // documentation inherited
    public void getObjects (Rectangle region, ObjectSet set)
    {
        int minx = MathUtil.floorDiv(region.x, swidth)*swidth;
        int maxx = MathUtil.floorDiv(region.x+region.width-1, swidth)*swidth;
        int miny = MathUtil.floorDiv(region.y, sheight)*sheight;
        int maxy = MathUtil.floorDiv(region.y+region.height-1, sheight)*sheight;
        for (int yy = miny; yy <= maxy; yy += sheight) {
            for (int xx = minx; xx <= maxx; xx += swidth) {
                Section sec = getSection(xx, yy, false);
                if (sec != null) {
                    sec.getObjects(region, set);
                }
            }
        }
    }

    // documentation inherited
    public void addObject (ObjectInfo info)
    {
        getSection(info.x, info.y, true).addObject(info);
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
        Section sec = getSection(info.x, info.y, false);
        if (sec != null) {
            return sec.removeObject(info);
        } else {
            return false;
        }
    }

    /**
     * Don't call this method! This is only public so that the scene
     * parser can construct a scene from raw data. If only Java supported
     * class friendship.
     */
    public void setSection (Section section)
    {
        _sections.put(key(section.x, section.y), section);
    }

    /**
     * Don't call this method! This is only public so that the scene
     * writer can generate XML from the raw scene data.
     */
    public Iterator getSections ()
    {
        return _sections.values().iterator();
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", sections=" +
                   StringUtil.toString(_sections.values().iterator()));
    }

    /**
     * Returns the key for the specified section.
     */
    protected final int key (int x, int y)
    {
        int sx = MathUtil.floorDiv(x, swidth);
        int sy = MathUtil.floorDiv(y, sheight);
        return (sx << 16) | (sy & 0xFFFF);
    }

    /** Returns the section for the specified tile coordinate. */
    protected final Section getSection (int x, int y, boolean create)
    {
        int key = key(x, y);
        Section sect = (Section)_sections.get(key);
        if (sect == null && create) {
            short sx = (short)(MathUtil.floorDiv(x, swidth)*swidth);
            short sy = (short)(MathUtil.floorDiv(y, sheight)*sheight);
            _sections.put(key, sect = new Section(sx, sy, swidth, sheight));
//             Log.info("Created new section " + sect + ".");
        }
        return sect;
    }

    // documentation inherited
    public Object clone ()
    {
        SparseMisoSceneModel model = (SparseMisoSceneModel)super.clone();
        model._sections = new StreamableHashIntMap();
        for (Iterator iter = getSections(); iter.hasNext(); ) {
            Section sect = (Section)iter.next();
            model.setSection((Section)sect.clone());
        }
        return model;
    }

    /** Contains our sections in row major order. */
    protected StreamableHashIntMap _sections = new StreamableHashIntMap();
}
