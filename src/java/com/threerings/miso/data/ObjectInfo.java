//
// $Id: ObjectInfo.java,v 1.5 2004/02/25 14:43:57 mdb Exp $

package com.threerings.miso.data;

import com.samskivert.util.StringUtil;
import com.threerings.io.SimpleStreamableObject;
import com.threerings.media.tile.TileUtil;

/**
 * Contains information about an object in a Miso scene.
 */
public class ObjectInfo extends SimpleStreamableObject
    implements Cloneable
{
    /** The fully qualified object tile id. */
    public int tileId;

    /** The x and y tile coordinates of the object. */
    public int x, y;

    /** Don't access this directly unless you are serializing this
     * instance. Use {@link #getPriority} instead. */
    public byte priority = 0;

    /** The action associated with this object or null if it has no
     * action. */
    public String action;

    /** A "spot" associated with this object (specified as an offset from
     * the fine coordinates of the object's origin tile). */
    public byte sx, sy;

    /** The orientation of the "spot" associated with this object. */
    public byte sorient;

    /** Up to two colorization assignments for this object. */
    public int zations;

    /**
     * Convenience constructor.
     */
    public ObjectInfo (int tileId, int x, int y)
    {
        this.tileId = tileId;
        this.x = x;
        this.y = y;
    }

    /**
     * Creates an object info that is a copy of the supplied info.
     */
    public ObjectInfo (ObjectInfo other)
    {
        this.tileId = other.tileId;
        this.x = other.x;
        this.y = other.y;
        this.priority = other.priority;
        this.action = other.action;
        this.sx = other.sx;
        this.sy = other.sy;
        this.sorient = other.sorient;
        this.zations = other.zations;
    }

    /**
     * Zero argument constructor needed for unserialization.
     */
    public ObjectInfo ()
    {
    }

    /**
     * Returns the render priority of this object tile.
     */
    public int getPriority ()
    {
        return priority;
    }

    /**
     * Returns the primary colorization assignment.
     */
    public int getPrimaryZation ()
    {
        return (zations & 0xFFFF);
    }

    /**
     * Returns the secondary colorization assignment.
     */
    public int getSecondaryZation ()
    {
        return ((zations >> 16) & 0xFFFF);
    }

    /**
     * Sets the primary and secondary colorization assignments.
     */
    public void setZations (short primary, short secondary)
    {
        zations = ((secondary << 16) | primary);
    }

    /**
     * Returns true if this object info contains non-default data for
     * anything other than the tile id and coordinates.
     */
    public boolean isInteresting ()
    {
        return (!StringUtil.blank(action) || priority != 0 ||
                sx != 0 || sy != 0 || zations != 0);
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        if (other instanceof ObjectInfo) {
            ObjectInfo ooi = (ObjectInfo)other;
            return (x == ooi.x && y == ooi.y && tileId == ooi.tileId);
        } else {
            return false;
        }
    }

    // documentation inherited
    public int hashCode ()
    {
        return x ^ y ^ tileId;
    }

    // documentation inherited
    public Object clone ()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            // notgunnahappen.
            return null;
        }
    }

    /** Enhances our {@link SimpleStreamableObject#toString} output. */
    public String tileIdToString ()
    {
        return (TileUtil.getTileSetId(tileId) + ":" +
                TileUtil.getTileIndex(tileId));
    }
}
