//
// $Id: BundleUtil.java,v 1.3 2003/05/14 21:34:01 ray Exp $

package com.threerings.media.tile.bundle;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import com.samskivert.io.StreamUtil;

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
     * Extracts, but does not initialize, a serialized tileset bundle
     * instance from the supplied resource bundle.
     */
    public static TileSetBundle extractBundle (ResourceBundle bundle)
        throws IOException, ClassNotFoundException
    {
        // unserialize the tileset bundles array
        InputStream tbin = null;
        try {
            tbin = bundle.getResource(METADATA_PATH);
            ObjectInputStream oin = new ObjectInputStream(
                new BufferedInputStream(tbin));
            TileSetBundle tsb = (TileSetBundle)oin.readObject();
            return tsb;
        } finally {
            StreamUtil.close(tbin);
        }
    }
}
