//
// $Id: BundleUtil.java,v 1.1 2001/11/29 21:57:31 mdb Exp $

package com.threerings.media.tile.bundle;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import com.threerings.resource.ResourceBundle;

/**
 * Bundle related utility functions.
 */
public class BundleUtil
{
    /** The path to the metadata resource that we will attempt to load
     * from our resource set. */
    public static final String METADATA_PATH = "tsbundles.dat";

    /**
     * Extracts and initializes a serialized tileset bundle instance from
     * the supplied resource bundle.
     */
    public static TileSetBundle extractBundle (ResourceBundle bundle)
        throws IOException, ClassNotFoundException
    {
        // unserialize the tileset bundles array
        InputStream tbin = bundle.getResource(METADATA_PATH);
        ObjectInputStream oin = new ObjectInputStream(
            new BufferedInputStream(tbin));
        TileSetBundle tsb = (TileSetBundle)oin.readObject();
        // initialize the bundle and add it to the list
        tsb.init(bundle);
        return tsb;
    }
}
