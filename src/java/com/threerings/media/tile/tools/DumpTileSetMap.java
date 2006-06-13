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
import java.util.Iterator;

import com.samskivert.io.PersistenceException;

/**
 * Prints out the tileset mappings in a {@link MapFileTileSetIDBroker}.
 */
public class DumpTileSetMap
{
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: DumpTileSetMap tileset.map");
            System.exit(-1);
        }

        try {
            MapFileTileSetIDBroker broker =
                new MapFileTileSetIDBroker(new File(args[0]));
            Iterator iter = broker.enumerateMappings();
            while (iter.hasNext()) {
                String tsname = iter.next().toString();
                System.out.println(tsname + " => " +
                                   broker.getTileSetID(tsname));
            }

        } catch (PersistenceException pe) {
            System.err.println("Unable to dump mapping: " + pe);
            System.exit(-1);
        }
    }
}
