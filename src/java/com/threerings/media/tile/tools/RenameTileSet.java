//
// $Id: RenameTileSet.java,v 1.1 2002/06/04 02:50:02 mdb Exp $

package com.threerings.media.tile.tools;

import java.io.File;

import com.samskivert.io.PersistenceException;

/**
 * Used to map a tileset name to the same ID as a pre-existing tileset.
 * This only works for tileset mappings stored using a {@link
 * MapFileTileSetIDBroker}. If a tileset is renamed, this utility must be
 * used to map the new name to the old ID, otherwise scenes created with
 * the renamed tileset will cease to work as the tileset will live under a
 * new ID.
 */
public class RenameTileSet
{
    /**
     * Loads up the tileset map file with the specified path and copies
     * the tileset ID from the old tileset name to the new tileset name.
     * This is necessary when a tileset is renamed so that the new name
     * does not cause the tileset to be assigned a new tileset ID. Bear in
     * mind that the old name should never again be used as it will
     * conflict with a tileset provided under the new name.
     */
    public static void renameTileSet (
        String mapPath, String oldName, String newName)
        throws PersistenceException
    {
        MapFileTileSetIDBroker broker =
            new MapFileTileSetIDBroker(new File(mapPath));
        if (!broker.renameTileSet(oldName, newName)) {
            throw new PersistenceException(
                "No such old tileset '" + oldName + "'.");
        }
        broker.commit();
    }

    public static void main (String[] args)
    {
        if (args.length < 3) {
            System.err.println("Usage: RenameTileSet tileset.map " +
                               "old_name new_name");
            System.exit(-1);
        }

        try {
            renameTileSet(args[0], args[1], args[2]);
        } catch (PersistenceException pe) {
            System.err.println("Unable to rename tileset: " + pe);
            System.exit(-1);
        }
    }
}
