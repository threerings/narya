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
