//
// $Id: FringeConfiguration.java,v 1.4 2002/04/05 01:35:11 ray Exp $

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
     * During parsing we get an int[] representing the fringes we have
     * defined.
     */
    public void setTiles (int[] tiles)
    {
        // create an array of all possible tiles and set them all to invalid
        _bits = new int[256];
        for (int ii=0; ii < 256; ii++) {
            _bits[ii] = -1;
        }

        // fill in the fringebit configurations we have tiles for with the
        // index of the tiles
        for (int ii=0; ii < tiles.length; ii++) {
            _bits[tiles[ii]] = ii;
        }
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

    /**
     * Return the tile index of any fringing tileset that should be used
     * if the following fringebits are turned on.
     * @return -1 if not defined
     */
    public int getTileIndexFromFringeBits (int bits)
    {
        return _bits[bits];
    }

    /**
     * Get the tileset id of the fringe for the specified basesetid.
     * We don't error check anything because you should be doing the right
     * thing here.
     */
    public int getFringeTileSetID (int basesetid)
    {
        FringeRecord f = (FringeRecord) _frecs.get(basesetid);

        // for now we just return the 1st tilesetid... no randomization
        return ((FringeTileSetRecord) f.tilesets.get(0)).fringe_tsid;
    }

    /** The mapping from base tileset id to fringerecord. */
    protected HashIntMap _frecs = new HashIntMap();

    /** An array that matches fringebits to fringe tile indexes. */
    protected int[] _bits;
}
