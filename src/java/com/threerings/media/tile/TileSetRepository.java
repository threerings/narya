//
// $Id: TileSetRepository.java,v 1.3 2001/11/29 00:12:11 mdb Exp $

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
     * Returns an iterator over the identifiers of all {@link TileSet}
     * objects available.
     */
    public Iterator enumerateTileSetIds ()
        throws PersistenceException;

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
