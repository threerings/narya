//
// $Id: ConfObjRegistry.java,v 1.1 2002/06/07 06:22:24 mdb Exp $

package com.threerings.admin.server;

import java.util.HashMap;
import java.util.Iterator;

import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DObject;

/**
 * Provides a registry of configuration distributed objects. Using
 * distributed object to store runtime configuration data can be
 * exceptionally useful in that clients (with admin privileges) can view
 * and update the running server's configuration parameters on the fly.
 *
 * <p> Users of the service are responsible for creating their own
 * configuration objects which are then registered with this class which
 * simply makes available a list of all registered configuration objects
 * to the client. The service users will want to use {@link
 * AccessController}s on their configuration distributed objects to
 * prevent non-administrators from subscribing to or modifying the
 * objects.
 */
public class ConfObjRegistry
{
    /**
     * Registers the supplied configuration object with the system.
     *
     * @param key a string that identifies this object. These are
     * generally hierarchical in nature (of the form
     * <code>system.subsystem</code>), for example:
     * <code>yohoho.crew</code>.
     * @param object the object to be registered.
     */
    public static void registerObject (String key, DObject object)
    {
        _confobjs.put(key, object);
    }

    /**
     * Returns the config object mapped to the specified key, or null if
     * none exists for that key.
     */
    public static DObject getObject (String key)
    {
        return (DObject)_confobjs.get(key);
    }

    /**
     * Returns an array containing the keys of all registered
     * configuration objects.
     */
    public static String[] getKeys ()
    {
        String[] keys = new String[_confobjs.size()];
        Iterator iter = _confobjs.keySet().iterator();
        for (int ii = 0; iter.hasNext(); ii++) {
            keys[ii] = (String)iter.next();
        }
        return keys;
    }

    /** A mapping from identifying key to config object. */
    protected static HashMap _confobjs = new HashMap();
}
