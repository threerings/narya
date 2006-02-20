//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.admin.server;

import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DObject;

/**
 * Provides a registry of configuration distributed objects. Using distributed
 * object to store runtime configuration data can be exceptionally useful in
 * that clients (with admin privileges) can view and update the running
 * server's configuration parameters on the fly.
 *
 * <p> Users of the service are responsible for creating their own
 * configuration objects which are then registered via this class. The config
 * object registry then performs a few functions:
 *
 * <ul>

 * <li> It populates the config object with values from the persistent
 * configuration information.
 * <li> It mirrors object updates out to the persistent configuration
 * repository.
 * <li> It makes the set of registered objects available for inspection and
 * modification via the admin client interface.
 * </ul>
 *
 * <p> Users of this service will want to use {@link AccessController}s on
 * their configuration distributed objects to prevent non-administrators from
 * subscribing to or modifying the objects.
*/
public abstract class ConfigRegistry
{
    /**
     * Registers the supplied configuration object with the system.
     *
     * @param key a string that identifies this object. These are generally
     * hierarchical in nature (of the form <code>system.subsystem</code>), for
     * example: <code>yohoho.crew</code>.
     * @param path The the path in the persistent configuration repository.
     * @param object the object to be registered.
     */
    public abstract void registerObject (
        String key, String path, DObject object);

    /**
     * Returns the config object mapped to the specified key, or null if none
     * exists for that key.
     */
    public abstract DObject getObject (String key);

    /**
     * Returns an array containing the keys of all registered configuration
     * objects.
     */
    public abstract String[] getKeys ();
}
