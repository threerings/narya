//
// $Id: TileSetRepository.java,v 1.6 2004/02/25 14:43:17 mdb Exp $

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
     * Returns the {@link TileSet} with the specified tile set
     * identifier. The repository is responsible for configuring the tile
     * set with an image provider.
     *
     * @exception NoSuchTileSetException thrown if no tileset exists with
     * the specified identifier.
     * @exception PersistenceException thrown if an error occurs
     * communicating with the underlying persistence mechanism.
     */
    public TileSet getTileSet (int tileSetId)
        throws NoSuchTileSetException, PersistenceException;

    /**
     * Returns the unique identifier of the {@link TileSet} with the
     * specified tile set name.
     *
     * @exception NoSuchTileSetException thrown if no tileset exists with
     * the specified name.
     * @exception PersistenceException thrown if an error occurs
     * communicating with the underlying persistence mechanism.
     */
    public int getTileSetId (String setName)
        throws NoSuchTileSetException, PersistenceException;

    /**
     * Returns the {@link TileSet} with the specified tile set name. The
     * repository is responsible for configuring the tile set with an
     * image provider.
     *
     * @exception NoSuchTileSetException thrown if no tileset exists with
     * the specified name.
     * @exception PersistenceException thrown if an error occurs
     * communicating with the underlying persistence mechanism.
     */
    public TileSet getTileSet (String setName)
        throws NoSuchTileSetException, PersistenceException;
}
