//
// $Id: TileSetParser.java,v 1.3 2001/07/23 18:52:51 shaper Exp $

package com.threerings.miso.tile;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * An interface to be implemented by classes that wish to provide a
 * general interface to load tileset description information from an
 * input stream.
 */
public interface TileSetParser
{
    /**
     * Read tileset description data from the given input stream and
     * construct TileSet objects to suit.  Return an ArrayList of all
     * TileSet objects constructed, or a zero-length array if no
     * tileset descriptions were fully parsed.
     */
    public ArrayList loadTileSets (InputStream tis) throws IOException;
}
