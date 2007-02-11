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

package com.threerings.crowd.server;

import java.util.Iterator;

import com.samskivert.util.HashIntMap;

import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.CrowdCodes;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

/**
 * The place registry keeps track of all of the active places in the
 * server. It should be used to create new places and it will take care of
 * instantiating and initializing a place manager to manage newly created
 * places.
 */
public class PlaceRegistry
{
    /** Used in conjunction with {@link #createPlace}. */
    public static interface PreStartupHook
    {
        public void invoke (PlaceManager plmgr);
    }

    /** The location provider used by the place registry to provide
     * location-related invocation services. */
    public LocationProvider locprov;

    /**
     * Creates and initializes the place registry; called by the server
     * during its initialization phase.
     */
    public PlaceRegistry (InvocationManager invmgr, RootDObjectManager omgr)
    {
        // create and register our location provider
        locprov = new LocationProvider(invmgr, omgr, this);
        invmgr.registerDispatcher(new LocationDispatcher(locprov), CrowdCodes.CROWD_GROUP);

        // we'll need these later
        _omgr = omgr;
        _invmgr = invmgr;
    }

    /**
     * By overriding this method, it is possible to customize the place
     * registry to cause it to load the classes associated with a
     * particular place via a custom class loader. That loader may enforce
     * restricted privileges or obtain the classes from some special
     * source.
     *
     * @return the class loader to use when instantiating the {@link
     * PlaceManager} associated with the supplied {@link
     * PlaceConfig}. This method <em>must not</em> return null.
     */
    public ClassLoader getClassLoader (PlaceConfig config)
    {
        return getClass().getClassLoader();
    }

    /**
     * Creates and registers a new place manager along with the place object to
     * be managed. The registry takes care of tracking the creation of the
     * object and informing the manager when it is created.
     *
     * @param config the configuration object for the place to be created. The
     * {@link PlaceManager} derived class that should be instantiated to manage
     * the place will be determined from the config object.
     *
     * @return a reference to the place manager, which will have been
     * configured with its place object and started up (via a call to {@link
     * PlaceManager#startup}.
     *
     * @exception InstantiationException thrown if an error occurs trying to
     * instantiate and initialize the place manager.
     * @exception InvocationException thrown if the place manager returns
     * failure from the call to {@link PlaceManager#checkPermissions}. The
     * error string returned by that call will be provided as in the exception.
     */
    public PlaceManager createPlace (PlaceConfig config)
        throws InstantiationException, InvocationException
    {
        return createPlace(config, null);
    }

    /**
     * Don't use this method, see {@link #createPlace(PlaceConfig)}..
     *
     * @param hook an optional pre-startup hook that allows a place manager to
     * be configured prior to having {@link PlaceManager#startup} called. This
     * mainly exists because it used to be possible to do such things. Try not
     * to use this in new code.
     */
    public PlaceManager createPlace (PlaceConfig config, PreStartupHook hook)
        throws InstantiationException, InvocationException
    {
        PlaceManager pmgr = null;
        ClassLoader loader = getClassLoader(config);

        try {
            // load up the manager class
            Class pmgrClass = Class.forName(
                config.getManagerClassName(), true, loader);
            // create a place manager for this place
            pmgr = (PlaceManager)pmgrClass.newInstance();
            // let the pmgr know about us and its configuration
            pmgr.init(this, _invmgr, _omgr, config);

        } catch (Exception e) {
            Log.logStackTrace(e);
            throw new InstantiationException(
                "Error creating place manager [config=" + config + "].");
        }

        // give the manager an opportunity to abort the whole process
        // if it fails any permissions checks
        String errmsg = pmgr.checkPermissions();
        if (errmsg != null) {
            // give the place manager a chance to clean up after its early
            // initialization process
            pmgr.permissionsFailed();
            throw new InvocationException(errmsg);
        }

        // and create and register the place object
        PlaceObject plobj = pmgr.createPlaceObject();
        _omgr.registerObject(plobj);

        // stick the manager into our table
        _pmgrs.put(plobj.getOid(), pmgr);

        // start the place manager up with the newly created place object
        try {
            if (hook != null) {
                hook.invoke(pmgr);
            }

            pmgr.startup(plobj);
        } catch (Exception e) {
            Log.warning("Error starting place manager [obj=" + plobj +
                ", pmgr=" + pmgr + "].");
            Log.logStackTrace(e);
        }

        return pmgr;
    }

    /**
     * Returns the place manager associated with the specified place
     * object id or null if no such place exists.
     */
    public PlaceManager getPlaceManager (int placeOid)
    {
        return _pmgrs.get(placeOid);
    }

    /**
     * Returns an enumeration of all of the registered place objects. This
     * should only be accessed on the dobjmgr thread and shouldn't be kept
     * around across event dispatches.
     */
    public Iterator<PlaceObject> enumeratePlaces ()
    {
        final Iterator<PlaceManager> itr = _pmgrs.values().iterator();
        return new Iterator<PlaceObject>() {
            public boolean hasNext ()
            {
                return itr.hasNext();
            }

            public PlaceObject next ()
            {
                PlaceManager plmgr = itr.next();
                return (plmgr == null) ? null : plmgr.getPlaceObject();
            }

            public void remove ()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns an enumeration of all of the registered place managers.
     * This should only be accessed on the dobjmgr thread and shouldn't be
     * kept around across event dispatches.
     */
    public Iterator<PlaceManager> enumeratePlaceManagers ()
    {
        return _pmgrs.values().iterator();
    }

    /**
     * Called by the place manager when it has been shut down.
     */
    protected void unmapPlaceManager (PlaceManager pmgr)
    {
        int ploid = pmgr.getPlaceObject().getOid();
        // remove it from the table
        if (_pmgrs.remove(ploid) == null) {
            Log.warning("Requested to unmap unmapped place manager " +
                        "[pmgr=" + pmgr + "].");

//         } else {
//             Log.info("Unmapped place manager " +
//                      "[class=" + pmgr.getClass().getName() +
//                      ", ploid=" + ploid + "].");
        }
    }

    /** The invocation manager with which we operate. */
    protected InvocationManager _invmgr;

    /** The distributed object manager with which we operate. */
    protected RootDObjectManager _omgr;

    /** A mapping from place object id to place manager. */
    protected HashIntMap<PlaceManager> _pmgrs = new HashIntMap<PlaceManager>();
}
