//
// $Id: TileSetRepository.java,v 1.1 2001/11/01 01:40:42 shaper Exp $

package com.threerings.media.tile;

import java.util.Iterator;

import com.samskivert.util.Config;
import com.threerings.media.ImageManager;

/**
 * The tile set repository interface should be implemented by classes
 * that provide access to tile sets keyed on their unique tile set
 * identifier.
 */
public interface TileSetRepository {

    /**
     * Initializes the tile set repository.
     */
    public void init (Config config, ImageManager imgmgr);

    /**
     * Returns an iterator over all {@link TileSet} objects available.
     */
    public Iterator enumerateTileSets ();

    /**
     * Returns the {@link TileSet} with the specified unique tile set
     * identifier.
     */
    public TileSet getTileSet (int tsid)
        throws NoSuchTileSetException;
}
