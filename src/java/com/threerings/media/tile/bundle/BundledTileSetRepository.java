//
// $Id: BundledTileSetRepository.java,v 1.12 2003/06/18 05:48:45 mdb Exp $

package com.threerings.media.tile.bundle;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.HashIntMap;

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
        _imgr = imgr;

        // unpack our bundles in the background
        new Thread(new Runnable() {
            public void run () {
                initBundles(rmgr, name);
            }
        }).start();
    }

    /**
     * Initializes our bundles, 
     */
    protected void initBundles (ResourceManager rmgr, String name)
    {
        // first we obtain the resource set from which we will load up our
        // tileset bundles
        ResourceBundle[] rbundles = rmgr.getResourceSet(name);

        // sanity check
        if (rbundles == null) {
            Log.warning("Unable to fetch tileset resource set " +
                        "[name=" + name + "]. Perhaps it's not defined " +
                        "in the resource manager config?");
            return;
        }

        HashIntMap idmap = new HashIntMap();
        HashMap namemap = new HashMap();

        // iterate over the resource bundles in the set, loading up the
        // tileset bundles in each resource bundle
        ArrayList tbundles = new ArrayList();
        for (int i = 0; i < rbundles.length; i++) {
            try {
                // unserialize our tileset bundle
                TileSetBundle tsb = BundleUtil.extractBundle(rbundles[i]);
                // initialize it and add it to the list
                tsb.init(rbundles[i]);
                addBundle(idmap, namemap, tsb);

            } catch (Exception e) {
                Log.warning("Unable to load tileset bundle '" +
                            BundleUtil.METADATA_PATH + "' from resource " +
                            "bundle [rbundle=" + rbundles[i] +
                            ", error=" + e + "].");
                Log.logStackTrace(e);
            }
        }

        // fill in our bundles array and wake up any waiters
        synchronized (this) {
            _idmap = idmap;
            _namemap = namemap;
            notifyAll();
        }
    }

    /**
     * Adds the tilesets in the supplied bundle to our tileset mapping
     * tables. Any tilesets with the same name or id will be overwritten.
     */
    protected void addBundle (HashIntMap idmap, HashMap namemap,
                              TileSetBundle bundle)
    {
        IMImageProvider improv = new IMImageProvider(_imgr, bundle);

        // map all of the tilesets in this bundle
        for (Iterator iter = bundle.entrySet().iterator(); iter.hasNext(); ) {
            HashIntMap.Entry entry = (HashIntMap.Entry)iter.next();
            Integer tsid = (Integer)entry.getKey();
            TileSet tset = (TileSet)entry.getValue();
            tset.setImageProvider(improv);
            idmap.put(tsid.intValue(), tset);
            namemap.put(tset.getName(), tsid);
        }
    }

    // documentation inherited from interface
    public Iterator enumerateTileSetIds ()
        throws PersistenceException
    {
        waitForBundles();
        return _idmap.keySet().iterator();
    }

    // documentation inherited from interface
    public Iterator enumerateTileSets ()
        throws PersistenceException
    {
        waitForBundles();
        return _idmap.values().iterator();
    }

    // documentation inherited from interface
    public TileSet getTileSet (int tileSetId)
        throws NoSuchTileSetException, PersistenceException
    {
        waitForBundles();
        TileSet tset = (TileSet)_idmap.get(tileSetId);
        if (tset == null) {
            throw new NoSuchTileSetException(tileSetId);
        }
        return tset;
    }

    // documentation inherited from interface
    public int getTileSetId (String setName)
        throws NoSuchTileSetException, PersistenceException
    {
        waitForBundles();
        Integer tsid = (Integer)_namemap.get(setName);
        if (tsid != null) {
            return tsid.intValue();
        }
        throw new NoSuchTileSetException(setName);
    }

    // documentation inherited from interface
    public TileSet getTileSet (String setName)
        throws NoSuchTileSetException, PersistenceException
    {
        waitForBundles();
        TileSet tset = null;
        Integer tsid = (Integer)_namemap.get(setName);
        if (tsid != null) {
            return getTileSet(tsid.intValue());
        }
        throw new NoSuchTileSetException(setName);
    }

    /** Used to allow bundle unpacking to proceed asynchronously. */
    protected synchronized void waitForBundles ()
    {
        while (_idmap == null) {
            try {
                wait();
            } catch (InterruptedException ie) {
                Log.warning("Interrupted waiting for bundles " + ie);
            }
        }
    }

    /** The image manager via which we load our images. */
    protected ImageManager _imgr;

    /** A mapping from tileset id to tileset. */
    protected HashIntMap _idmap;

    /** A mapping from tileset name to tileset id. */
    protected HashMap _namemap;
}
