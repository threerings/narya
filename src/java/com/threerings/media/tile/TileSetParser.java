//
// $Id: TileSetParser.java,v 1.7 2001/10/15 23:53:43 shaper Exp $

package com.threerings.media.tile;

import java.io.IOException;
import java.util.List;

/**
 * The tile set parser interface is intended to be implemented by
 * classes that load tileset descriptions from a file.
 */
public interface TileSetParser
{
    /**
     * Read tileset description data from the specified file and
     * append {@link TileSet} objects constructed from the data to the
     * given list.
     */
    public void loadTileSets (String fname, List tilesets) throws IOException;
}
