//
// $Id: FringeConfiguration.java,v 1.1 2002/04/03 22:52:44 ray Exp $

package com.threerings.miso.scene;

import java.io.Serializable;
import java.util.ArrayList;

import com.samskivert.util.HashIntMap;

/**
 * Used to manage data about which base tilesets fringe on which others
 * and how they fringe.
 */
public class FringeConfiguration implements Serializable
{
    /**
     * The path (relative to the resource directory) at which the fringe
     * configuration should be loaded and stored.
     */
    public static final String CONFIG_PATH = "config/miso/scene/fringeconf.dat";

    public static class FringeRecord implements Serializable
    {
        /** The tileset id of the base tileset to which this applies. */
        public int base_tsid;

        /** The fringe priority of this base tileset. */
        public int priority;

        /** A list of the possible tilesets that can be used for fringing. */
        public ArrayList tilesets = new ArrayList();

        /** Used when parsing the tilesets definitions. */
        public void addTileset (FringeTileSetRecord record)
        {
            tilesets.add(record);
        }
     }

    /**
     * Used to parse the tileset fringe definitions.
     */
    public static class FringeTileSetRecord implements Serializable
    {
        /** The tileset id of the fringe tileset. */
        public int fringe_tsid;

        /** Is this a mask? */
        public boolean mask;
    }

    /**
     * Adds a parsed FringeRecord to this instance. This is used when parsing
     * the fringerecords from xml.
     */
    public void addFringeRecord (FringeRecord frec)
    {
        _frecs.put(frec.base_tsid, frec);
    }

    /**
     * Does the first tileset fringe upon the second?
     */
    public boolean fringesOn (int first, int second)
    {
        FringeRecord f1 = (FringeRecord) _frecs.get(first);

        // we better have a fringe record for the first
        if (null != f1) {

            // it had better have some tilesets defined
            if (f1.tilesets.size() > 0) {

                FringeRecord f2 = (FringeRecord) _frecs.get(second);

                // and we only fringe if second doesn't exist or has a lower
                // priority
                if ((null == f2) || (f1.priority > f2.priority)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** The mapping from base tileset id to fringerecord. */
    protected HashIntMap _frecs = new HashIntMap();
}
