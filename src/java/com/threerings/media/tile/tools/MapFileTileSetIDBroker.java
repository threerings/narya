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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.QuickSort;

import com.threerings.media.tile.TileSetIDBroker;

/**
 * Stores a set of tileset name to id mappings in a map file.
 */
public class MapFileTileSetIDBroker implements TileSetIDBroker
{
    /**
     * Creates a broker that will use the specified file as its persistent
     * store. The persistent store will be created if it does not yet
     * exist.
     */
    public MapFileTileSetIDBroker (File mapfile)
        throws PersistenceException
    {
        // keep this for later
        _mapfile = mapfile;

        // load up our map data
        try {
            BufferedReader bin = new BufferedReader(new FileReader(mapfile));
            // read in our metadata
            _nextTileSetID = readInt(bin);
            _storedTileSetID = _nextTileSetID;
            // read in our mappings
            _map = new HashMap();
            readMapFile(bin, _map);

            bin.close();

        } catch (FileNotFoundException fnfe) {
            // create a blank map if our map file doesn't exist
            _map = new HashMap();

        } catch (Exception e) {
            // other errors are more fatal
            String errmsg = "Failure reading map file.";
            throw new PersistenceException(errmsg, e);
        }
    }

    protected int readInt (BufferedReader bin)
        throws IOException
    {
        String line = bin.readLine();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException nfe) {
            throw new IOException("Expected number, got '" + line + "'");
        }
    }

    // documentation inherited
    public int getTileSetID (String tileSetName)
        throws PersistenceException
    {
        Integer tsid = (Integer)_map.get(tileSetName);
        if (tsid == null) {
            tsid = Integer.valueOf(++_nextTileSetID);
            _map.put(tileSetName, tsid);
        }
        return tsid.intValue();
    }

    // documentation inherited from interface
    public boolean tileSetMapped (String tileSetName)
        throws PersistenceException
    {
        return _map.containsKey(tileSetName);
    }

    // documentation inherited
    public void commit ()
        throws PersistenceException
    {
        // only write ourselves out if we've changed
        if (_storedTileSetID == _nextTileSetID) {
            return;
        }

        try {
            BufferedWriter bout = new BufferedWriter(new FileWriter(_mapfile));
            // write out our metadata
            String tline = "" + _nextTileSetID;
            bout.write(tline, 0, tline.length());
            bout.newLine();
            // write out our mappings
            writeMapFile(bout, _map);
            bout.close();

        } catch (IOException ioe) {
            String errmsg = "Failure writing map file.";
            throw new PersistenceException(errmsg, ioe);
        }
    }

    /**
     * Reads in a mapping from strings to integers, which should have been
     * written via {@link #writeMapFile}.
     */
    public static void readMapFile (BufferedReader bin, HashMap map)
        throws IOException
    {
        String line;
        while ((line = bin.readLine()) != null) {
            int eidx = line.indexOf(SEP_STR);
            if (eidx == -1) {
                throw new IOException("Malformed line, no '" + SEP_STR +
                                      "': '" + line + "'");
            }
            try {
                String code = line.substring(eidx+SEP_STR.length());
                map.put(line.substring(0, eidx), Integer.valueOf(code));
            } catch (NumberFormatException nfe) {
                String errmsg = "Malformed line, invalid code: '" + line + "'";
                throw new IOException(errmsg);
            }
        }
    }

    /**
     * Writes out a mapping from strings to integers in a manner that can
     * be read back in via {@link #readMapFile}.
     */
    public static void writeMapFile (BufferedWriter bout, HashMap map)
        throws IOException
    {
        String[] lines = new String[map.size()];
        Iterator iter = map.keySet().iterator();
        for (int ii = 0; iter.hasNext(); ii++) {
            String key = (String)iter.next();
            Integer value = (Integer)map.get(key);
            lines[ii] = key + SEP_STR + value;
        }
        QuickSort.sort(lines);
        for (int ii = 0; ii < lines.length; ii++) {
            bout.write(lines[ii], 0, lines[ii].length());
            bout.newLine();
        }
        bout.flush();
    }

    /**
     * Copies the ID from the old tileset to the new tileset which is
     * useful when a tileset is renamed. This is called by the {@link
     * RenameTileSet} utility.
     */
    protected boolean renameTileSet (String oldName, String newName)
    {
        Integer tsid = (Integer)_map.get(oldName);
        if (tsid != null) {
            _map.put(newName, tsid);
            // fudge our stored tileset ID so that we flush ourselves when
            // the rename tool requests that we commit the changes
            _storedTileSetID--;
            return true;

        } else {
            return false;
        }
    }

    /**
     * Used by {@link DumpTileSetMap} to enumerate our tileset ID
     * mappings.
     */
    protected Iterator enumerateMappings ()
    {
        return _map.keySet().iterator();
    }

    /** Our persistent map file. */
    protected File _mapfile;

    /** The next tileset id that we'll assign. */
    protected int _nextTileSetID;

    /** The last tileset id assigned when we were unserialized. */
    protected int _storedTileSetID;

    /** Our mapping from tileset names to ids. */
    protected HashMap _map;

    /** The character we use to separate tileset name from code in the map
     * file. */
    protected static final String SEP_STR = " := ";
}
