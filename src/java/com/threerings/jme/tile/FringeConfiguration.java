//
// $Id: FringeConfiguration.java 3403 2005-03-14 23:58:02Z mdb $
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

package com.threerings.jme.tile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.samskivert.util.StringUtil;

import com.threerings.jme.Log;

/**
 * Used to manage data about which tiles fringe on which others and how
 * they fringe.
 */
public class FringeConfiguration implements Serializable
{
    /** Contains information on a type of tile and all of the fringe
     * records associated with it. */
    public static class TileRecord implements Serializable
    {
        /** The type of tile to which this applies. */
        public String type;

        /** The fringe priority of this type. */
        public int priority;

        /** A list of the fringe records that can be used for fringing. */
        public ArrayList fringes = new ArrayList();

        /** Used when parsing from an XML definition. */
        public void addFringe (FringeRecord record)
        {
            if (record.isValid()) {
                fringes.add(record);
            } else {
                Log.warning("Not adding invalid fringe record [tile=" + this +
                            ", fringe=" + record + "].");
            }
        }

        /** Did everything parse well? */
        public boolean isValid ()
        {
            return ((type != null) && (priority > 0));
        }

        /** Generates a string representation of this instance. */
        public String toString ()
        {
            return "[type=" + type + ", priority=" + priority +
                ", fringes=" + StringUtil.toString(fringes) + "]";
        }

        /** Increase this value when object's serialized state is impacted
         * by a class change (modification of fields, inheritance). */
        private static final long serialVersionUID = 1;
    }

    /** Used to parse the type fringe definitions. */
    public static class FringeRecord implements Serializable
    {
        /** The name of the fringe tileset image. */
        public String name;

        /** Is this a mask? */
        public boolean mask;

        /** Did everything parse well? */
        public boolean isValid ()
        {
            return (name != null);
        }

        /** Generates a string representation of this instance. */
        public String toString ()
        {
            return "[name=" + name + ", mask=" + mask + "]";
        }

        /** Increase this value when object's serialized state is impacted
         * by a class change (modification of fields, inheritance). */
        private static final long serialVersionUID = 1;
    }

    /**
     * Adds a parsed fringe record to this instance. This is used when
     * parsing the fringe records from xml.
     */
    public void addTileRecord (TileRecord record)
    {
        if (record.isValid()) {
            _trecs.put(record.type, record);
        } else {
            Log.warning("Refusing to add invalid tile record " +
                        "[tile=" + record + "].");
        }
    }

    /**
     * If the first type fringes upon the second, return the fringe
     * priority of the first type, otherwise return -1.
     */
    public int fringesOn (String fringer, String fringed)
    {
        TileRecord f1 = (TileRecord)_trecs.get(fringer);

        // we better have a fringe record for the fringer
        if (null != f1) {
            // it had better have some types defined
            if (f1.fringes.size() > 0) {
                TileRecord f2 = (TileRecord)_trecs.get(fringed);
                // and we only fringe if fringed doesn't have a fringe
                // record or has a lower priority
                if ((null == f2) || (f1.priority > f2.priority)) {
                    return f1.priority;
                }
            }
        }

        return -1;
    }

    /**
     * Get a random FringeRecord from amongst the ones listed for the
     * specified type.
     */
    public FringeRecord getFringe (String type, int hashValue)
    {
        TileRecord t = (TileRecord)_trecs.get(type);
        return (FringeRecord)t.fringes.get(hashValue % t.fringes.size());
    }

    /** The mapping from tile type to tile record. */
    protected HashMap _trecs = new HashMap();

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;
}
