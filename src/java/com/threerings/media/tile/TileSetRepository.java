//
// $Id: TileSetRepository.java,v 1.4 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

import java.util.Iterator;
import com.samskivert.io.PersistenceException;

import com.threerings.media.image.ImageDataProvider;

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
