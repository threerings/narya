//
// $Id: TileSetParser.java,v 1.6 2001/10/12 16:36:58 shaper Exp $

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
     * construct {@link TileSet} objects to suit.  Return a list of
     * all tile set objects constructed, or a zero-length list if no
     * tileset descriptions were fully parsed.
     */
    public List loadTileSets (String fname) throws IOException;
}
