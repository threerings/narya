//
// $Id: BundledTileSetRepository.java,v 1.2 2001/11/21 02:42:15 mdb Exp $

package com.threerings.media.tile.bundle;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.CompoundIterator;
import com.samskivert.util.CompoundIterator.IteratorProvider;

import com.threerings.resource.ResourceBundle;
import com.threerings.resource.ResourceManager;

import com.threerings.media.Log;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileSetRepository;

/**
 * Loads tileset data from a set of resource bundles.
 *
 * @see ResourceManager
 */
public class BundledTileSetRepository
    implements TileSetRepository
{
    /** The path to the metadata resource that we will attempt to load
     * from our resource set. */
    public static final String METADATA_PATH = "tsbundles.dat";

    /**
     * Constructs a repository which will obtain its resource set from the
     * supplied resource manager.
     *
     * @param rmgr the resource manager from which to obtain our resource
     * set.
     * @param name the name of the resource set from which we will be
     * loading our tile data.
     */
    public BundledTileSetRepository (ResourceManager rmgr, String name)
    {
        // first we obtain the resource set from which we will load up our
        // tileset bundles
        ResourceBundle[] rbundles = rmgr.getResourceSet(name);

        // iterate over the resource bundles in the set, loading up the
        // tileset bundles in each resource bundle
        ArrayList tbundles = new ArrayList();;
        for (int i = 0; i < rbundles.length; i++) {
            try {
                // unserialize the tileset bundles array
                InputStream tbin = rbundles[i].getResource(METADATA_PATH);
                ObjectInputStream oin = new ObjectInputStream(
                    new BufferedInputStream(tbin));
                TileSetBundle tsb = (TileSetBundle)oin.readObject();
                // initialize the bundle and add it to the list
                tsb.init(rbundles[i]);
                tbundles.add(tsb);

            } catch (Exception e) {
                Log.warning("Unable to load tileset bundles from resource " +
                            "bundle [rbundle=" + rbundles[i] +
                            ", error=" + e + "].");
            }
        }

        // finally create one big fat array of all of the tileset bundles
        _bundles = new TileSetBundle[tbundles.size()];
        tbundles.toArray(_bundles);
    }

    // documentation inherited
    public Iterator enumerateTileSets ()
        throws PersistenceException
    {
        return new CompoundIterator(new IteratorProvider() {
            public Iterator nextIterator () {
                if (_bidx < _bundles.length) {
                    return _bundles[_bidx++].enumerateTileSets();
                } else {
                    return null;
                }
            }
            protected int _bidx = 0;
        });
    }

    // documentation inherited
    public TileSet getTileSet (int tileSetId)
        throws NoSuchTileSetException, PersistenceException
    {
        TileSet tset = null;
        int blength = _bundles.length;
        for (int i = 0; i < blength; i++) {
            tset = _bundles[i].getTileSet(tileSetId);
            if (tset != null) {
                return tset;
            }
        }
        throw new NoSuchTileSetException(tileSetId);
    }

    /** An array of tileset bundles from which we obtain tilesets. */
    protected TileSetBundle[] _bundles;
}
