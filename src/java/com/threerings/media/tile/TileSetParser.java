//
// $Id: TileSetParser.java,v 1.8 2001/11/01 01:40:42 shaper Exp $

package com.threerings.media.tile;

import java.io.IOException;

import com.samskivert.util.HashIntMap;

/**
 * The tile set parser interface is intended to be implemented by
 * classes that load tileset descriptions from a file.
 */
public interface TileSetParser
{
    /**
     * Reads tileset description data from the specified file and
     * populates the given hashtable with {@link TileSet} objects
     * keyed on their tile set id.
     */
    public void loadTileSets (String fname, HashIntMap tilesets)
        throws IOException;
}
