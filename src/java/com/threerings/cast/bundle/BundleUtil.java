//
// $Id: BundleUtil.java,v 1.7 2004/02/25 14:39:34 mdb Exp $

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
     * @return the unserialized object in question.
     *
     * @exception IOException thrown if an I/O error occurs while reading
     * the object from the bundle.
     */     
    public static Object loadObject (ResourceBundle bundle, String path)
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

        } finally {
            StreamUtil.close(bin);
        }
    }
}
