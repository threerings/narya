//
// $Id: BundleUtil.java,v 1.4 2003/07/28 04:07:30 mdb Exp $

package com.threerings.media.tile.bundle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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

    /**
     * Extracts, but does not initialize, a serialized tileset bundle
     * instance from the supplied file.
     */
    public static TileSetBundle extractBundle (File file)
        throws IOException, ClassNotFoundException
    {
        // unserialize the tileset bundles array
        FileInputStream fin = new FileInputStream(file);
        try {
            ObjectInputStream oin = new ObjectInputStream(
                new BufferedInputStream(fin));
            TileSetBundle tsb = (TileSetBundle)oin.readObject();
            return tsb;
        } finally {
            StreamUtil.close(fin);
        }
    }
}
