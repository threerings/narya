//
// $Id: TileSetManager.java,v 1.2 2001/07/16 18:59:31 shaper Exp $

package com.threerings.cocktail.miso.tile;

public interface TileSetManager
{
    /**
     * Return the tileset object corresponding to the specified tileset id.
     */
    public TileSet getTileSet (int tsid);

    /**
     * Return a String array of all tileset names.
     */
    public String[] getTileSetNames ();

    /**
     * Return the tileset id associated with the named tileset.
     */
    public int getTileSetId (String name);

    /**
     * Return the total number of tilesets.
     */
    public int getNumTileSets ();
}
