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

package com.threerings.cast.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;

import com.samskivert.io.StreamUtil;

import com.threerings.cast.Log;
import com.threerings.resource.ResourceBundle;

/**
 * Utility functions related to creating and manipulating component
 * bundles.
 */
public class BundleUtil
{
    /** The path in the metadata bundle to the serialized action table. */
    public static final String ACTIONS_PATH = "actions.dat";

    /** The path in the metadata bundle to the serialized action tile sets
     * table. */
    public static final String ACTION_SETS_PATH = "action_sets.dat";

    /** The path in the metadata bundle to the serialized component class
     * table. */
    public static final String CLASSES_PATH = "classes.dat";

    /** The path in the component bundle to the serialized component id to
     * class/type mapping. */
    public static final String COMPONENTS_PATH = "components.dat";

    /** The file extension of our action tile images. */
    public static final String IMAGE_EXTENSION = ".png";

    /** The serialized tileset extension for our action tilesets. */
    public static final String TILESET_EXTENSION = ".dat";

    /**
     * Attempts to load an object from the supplied resource bundle with
     * the specified path.
     *
     * @param wipeBundleOnFailure if there is an error reading the object
     * from the bundle and this parameter is true, we will instruct the
     * bundle to delete its underlying jar file before propagating the
     * exception with the expectation that it will be redownloaded and
     * repaired the next time the application is run.
     *
     * @return the unserialized object in question.
     *
     * @exception IOException thrown if an I/O error occurs while reading
     * the object from the bundle.
     */     
    public static Object loadObject (ResourceBundle bundle, String path,
                                     boolean wipeBundleOnFailure)
        throws IOException, ClassNotFoundException
    {
        InputStream bin = null;
        try {
            bin = bundle.getResource(path);
            if (bin == null) {
                return null;
            }
            return new ObjectInputStream(bin).readObject();

        } catch (InvalidClassException ice) {
            Log.warning("Aiya! Serialized object is hosed " +
                        "[bundle=" + bundle.getSource().getPath() +
                        ", element=" + path +
                        ", error=" + ice.getMessage() + "].");
            return null;

        } catch (IOException ioe) {
            Log.warning("Error reading resource from bundle " +
                        "[bundle=" + bundle + ", path=" + path +
                        ", wiping?=" + wipeBundleOnFailure + "].");
            if (wipeBundleOnFailure) {
                StreamUtil.close(bin);
                bin = null;
                bundle.wipeBundle(false);
            }
            throw ioe;

        } finally {
            StreamUtil.close(bin);
        }
    }
}
