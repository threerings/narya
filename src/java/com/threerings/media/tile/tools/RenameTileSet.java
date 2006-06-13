//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
