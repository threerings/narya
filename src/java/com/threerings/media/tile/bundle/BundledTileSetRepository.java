//
// $Id: BundledTileSetRepository.java,v 1.9 2003/01/13 22:49:47 mdb Exp $

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
    public BundledTileSetRepository (ResourceManager rmgr, ImageManager imgr,
                                     String name)
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
        _bundles = new TileSetBundle[tbundles.size()];
        tbundles.toArray(_bundles);

        // create image providers for our bundles
        _improvs = new IMImageProvider[_bundles.length];
        for (int ii = 0; ii < _bundles.length; ii++) {
            _improvs[ii] = new IMImageProvider(imgr, _bundles[ii]);
        }
    }

    // documentation inherited from interface
    public Iterator enumerateTileSetIds ()
        throws PersistenceException
    {
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

    /** An array of tileset bundles from which we obtain tilesets. */
    protected TileSetBundle[] _bundles;

    /** Image providers for each of our tile set bundles. */
    protected IMImageProvider[] _improvs;
}
