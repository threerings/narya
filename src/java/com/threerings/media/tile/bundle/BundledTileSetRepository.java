//
// $Id: BundledTileSetRepository.java,v 1.10 2003/01/14 02:52:04 mdb Exp $

package com.threerings.media.tile.bundle;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.CompoundIterator;
import com.samskivert.util.CompoundIterator.IteratorProvider;

import com.threerings.resource.ResourceBundle;
import com.threerings.resource.ResourceManager;

import com.threerings.media.Log;
import com.threerings.media.image.ImageDataProvider;
import com.threerings.media.image.ImageManager;

import com.threerings.media.tile.IMImageProvider;
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
    /**
     * Constructs a repository which will obtain its resource set from the
     * supplied resource manager.
     *
     * @param rmgr the resource manager from which to obtain our resource
     * set.
     * @param imgr the image manager through which we will configure the
     * tile sets to load their images.
     * @param name the name of the resource set from which we will be
     * loading our tile data.
     */
    public BundledTileSetRepository (final ResourceManager rmgr,
                                     final ImageManager imgr,
                                     final String name)
    {
        // unpack our bundles in the background
        new Thread(new Runnable() {
            public void run () {
                initBundles(rmgr, imgr, name);
            }
        }).start();
    }

    /**
     * Initializes our bundles, 
     */
    protected void initBundles (
        ResourceManager rmgr, ImageManager imgr, String name)
    {
        // first we obtain the resource set from which we will load up our
        // tileset bundles
        ResourceBundle[] rbundles = rmgr.getResourceSet(name);

        // sanity check
        if (rbundles == null) {
            Log.warning("Unable to fetch tileset resource set " +
                        "[name=" + name + "]. Perhaps it's not defined " +
                        "in the resource manager config?");
            _bundles = new TileSetBundle[0];
            return;
        }

        // iterate over the resource bundles in the set, loading up the
        // tileset bundles in each resource bundle
        ArrayList tbundles = new ArrayList();
        for (int i = 0; i < rbundles.length; i++) {
            try {
                // unserialize our tileset bundle
                TileSetBundle tsb = BundleUtil.extractBundle(rbundles[i]);
                // initialize it and add it to the list
                tsb.init(rbundles[i]);
                tbundles.add(tsb);
                
            } catch (Exception e) {
                Log.warning("Unable to load tileset bundle from resource " +
                            "bundle [rbundle=" + rbundles[i] +
                            ", error=" + e + "].");
                Log.logStackTrace(e);
            }
        }

        // finally create one big fat array of all of the tileset bundles
        TileSetBundle[] bundles = new TileSetBundle[tbundles.size()];
        tbundles.toArray(bundles);

        // create image providers for our bundles
        _improvs = new IMImageProvider[bundles.length];
        for (int ii = 0; ii < bundles.length; ii++) {
            _improvs[ii] = new IMImageProvider(imgr, bundles[ii]);
        }

        // fill in our bundles array and wake up any waiters
        synchronized (this) {
            _bundles = bundles;
            notifyAll();
        }
    }

    // documentation inherited from interface
    public Iterator enumerateTileSetIds ()
        throws PersistenceException
    {
        waitForBundles();
        return new CompoundIterator(new IteratorProvider() {
            public Iterator nextIterator () {
                if (_bidx < _bundles.length) {
                    return _bundles[_bidx++].enumerateTileSetIds();
                } else {
                    return null;
                }
            }
            protected int _bidx = 0;
        });
    }

    // documentation inherited from interface
    public Iterator enumerateTileSets ()
        throws PersistenceException
    {
        waitForBundles();
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

    // documentation inherited from interface
    public TileSet getTileSet (int tileSetId)
        throws NoSuchTileSetException, PersistenceException
    {
        waitForBundles();
        TileSet tset = null;
        int blength = _bundles.length;
        for (int i = 0; i < blength; i++) {
            tset = _bundles[i].getTileSet(tileSetId);
            if (tset != null) {
                tset.setImageProvider(_improvs[i]);
                return tset;
            }
        }
        throw new NoSuchTileSetException(tileSetId);
    }

    // documentation inherited from interface
    public TileSet getTileSet (String setName)
        throws NoSuchTileSetException, PersistenceException
    {
        waitForBundles();
        int bcount = _bundles.length;
        for (int ii = 0; ii < bcount; ii++) {
            TileSetBundle tsb = _bundles[ii];
            // search for the tileset in this bundle
            Iterator tsiter = tsb.enumerateTileSets();
            while (tsiter.hasNext()) {
                TileSet set = (TileSet)tsiter.next();
                if (set.getName().equals(setName)) {
                    set.setImageProvider(_improvs[ii]);
                    return set;
                }
            }
        }
        return null;
    }

    /** Used to allow bundle unpacking to proceed asynchronously. */
    protected synchronized void waitForBundles ()
    {
        while (_bundles == null) {
            try {
                wait();
            } catch (InterruptedException ie) {
                Log.warning("Interrupted waiting for bundles " + ie);
            }
        }
    }

    /** An array of tileset bundles from which we obtain tilesets. */
    protected TileSetBundle[] _bundles;

    /** Image providers for each of our tile set bundles. */
    protected IMImageProvider[] _improvs;
}
