//
// $Id: TileSetParser.java,v 1.2 2001/07/18 21:45:42 shaper Exp $

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
     * Return all tileset objects constructed from any previous
     * parsing activity.
     */
    public ArrayList getTileSets ();

    /**
     * Construct tileset objects from the tileset description data on
     * the given input stream.  Classes that implement this method
     * should store the tileset objects for later retrieval via the
     * <code>getTileSets()</code> method.
     */
    public void loadTileSets (InputStream tis) throws IOException;
}
