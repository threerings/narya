//
// $Id: TileSetRepository.java,v 1.2 2001/11/18 04:09:21 mdb Exp $

package com.threerings.media.tile;

import java.util.Iterator;
import com.samskivert.io.PersistenceException;

/**
 * The tileset repository interface should be implemented by classes that
 * provide access to tilesets keyed on a unique tileset identifier. The
 * tileset id space is up to the repository implementation, which may or
 * may not desire to use a {@link TileSetIDBroker} to manage the space.
 */
public interface TileSetRepository
{
    /**
     * Returns an iterator over all {@link TileSet} objects available.
     */
    public Iterator enumerateTileSets ()
        throws PersistenceException;

    /**
     * Returns the {@link TileSet} with the specified tile set identifier.
     *
     * @exception NoSuchTileSetException thrown if no tileset exists with
     * the specified identifier.
     * @exception PersistenceException thrown if an error occurs
     * communicating with the underlying persistence mechanism.
     */
    public TileSet getTileSet (int tileSetId)
        throws NoSuchTileSetException, PersistenceException;
}
