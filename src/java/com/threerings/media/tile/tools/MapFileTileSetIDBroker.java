//
// $Id: MapFileTileSetIDBroker.java,v 1.2 2002/02/05 20:29:09 mdb Exp $

package com.threerings.media.tile.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashMap;

import com.samskivert.io.PersistenceException;

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
            FileInputStream fin = new FileInputStream(mapfile);
            ObjectInputStream oin = new ObjectInputStream(fin);
            _nextTileSetID = oin.readInt();
            _map = (HashMap)oin.readObject();
            oin.close();

        } catch (FileNotFoundException fnfe) {
            // create a blank map if our map file doesn't exist
            _map = new HashMap();

        } catch (Exception e) {
            // other errors are more fatal
            String errmsg = "Failure reading map file.";
            throw new PersistenceException(errmsg, e);
        }
    }

    // documentation inherited
    public int getTileSetID (String tileSetName)
        throws PersistenceException
    {
        Integer tsid = (Integer)_map.get(tileSetName);
        if (tsid == null) {
            tsid = new Integer(++_nextTileSetID);
            _map.put(tileSetName, tsid);
        }
        return tsid.intValue();
    }

    // documentation inherited
    public void commit ()
        throws PersistenceException
    {
        try {
            FileOutputStream fout = new FileOutputStream(_mapfile);
            ObjectOutputStream oout = new ObjectOutputStream(fout);
            oout.writeInt(_nextTileSetID);
            oout.writeObject(_map);
            oout.close();

        } catch (IOException ioe) {
            String errmsg = "Failure writing map file.";
            throw new PersistenceException(errmsg, ioe);
        }
    }

    /** Our persistent map file. */
    protected File _mapfile;

    /** The next tileset id that we'll assign. */
    protected int _nextTileSetID;

    /** Our mapping from tileset names to ids. */
    protected HashMap _map;
}
