//
// $Id: TileSetParser.java,v 1.4 2001/07/23 22:31:48 shaper Exp $

package com.threerings.miso.tile;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The TileSetParser is a general interface to be implemented by
 * classes that load tileset descriptions in a particular format from
 * a file.
 */
public interface TileSetParser
{
    /**
     * Read tileset description data from the specified file and
     * construct TileSet objects to suit.  Return an ArrayList of all
     * TileSet objects constructed, or a zero-length list if no
     * tileset descriptions were fully parsed.
     */
    public ArrayList loadTileSets (String fname) throws IOException;
}
