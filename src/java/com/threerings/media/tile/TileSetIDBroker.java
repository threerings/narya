//
// $Id: TileSetIDBroker.java,v 1.1 2001/11/18 04:09:21 mdb Exp $

package com.threerings.media.tile;

import com.samskivert.io.PersistenceException;

/**
 * Brokers tileset ids. The tileset repository interface makes available a
 * collection of tilesets based on a unique identifier. The expectation is
 * that a collection of tilesets will be used to populate a repository and
 * in that population process, tileset ids will be assigned to the
 * tilesets. The tileset id broker system provides a means by which named
 * tilesets can be mapped consistently to a set of tileset ids. Humans can
 * then be responsible for assigning unique names to the tilesets and the
 * broker will ensure that those names map to unique ids that won't change
 * if the repository is rebuilt from the source tilesets.
 */
public interface TileSetIDBroker
{
    /**
     * Returns the unique identifier for the named tileset. If no
     * identifier has yet been assigned to the specified named tileset,
     * one should be assigned and returned.
     *
     * @exception PersistenceException thrown if an error occurs
     * communicating with the underlying persistence mechanism used to
     * store the name to id mappings.
     */
    public int getTileSetID (String tileSetName)
        throws PersistenceException;
}
