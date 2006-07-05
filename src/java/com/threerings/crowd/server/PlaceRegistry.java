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
import com.samskivert.util.Tuple;
import com.samskivert.util.Queue;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

/**
 * The place registry keeps track of all of the active places in the
 * server. It should be used to create new places and it will take care of
 * instantiating and initializing a place manager to manage newly created
 * places.
 */
public class PlaceRegistry
    implements Subscriber<PlaceObject>
{
    /**
     * Used to receive a callback when the place object associated with a
     * place manager (created via {@link PlaceRegistry#createPlace}) is
     * created.
     */
    public static interface CreationObserver
    {
        /**
         * Called when the place object is created and after it is
         * provided to the place manager that will be handling management
         * of the place.
         */
        public void placeCreated (PlaceObject place, PlaceManager pmgr);
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
        invmgr.registerDispatcher(new LocationDispatcher(locprov), true);

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
     * Creates and registers a new place manager along with the place
     * object to be managed. The registry takes care of tracking the
     * creation of the object and informing the manager when it is
     * created.
     *
     * @param config the configuration object for the place to be
     * created. The {@link PlaceManager} derived class that should be
     * instantiated to manage the place will be determined from the config
     * object.
     * @param observer an observer that will be notified when the place
     * object creation has completed (called after the place object is
     * provided to the place manager).
     *
     * @return a reference to the place manager that will manage the new
     * place object.
     *
     * @exception InstantiationException thrown if an error occurs trying
     * to instantiate and initialize the place manager.
     * @exception InvocationException thrown if the place manager returns
     * failure from the call to {@link PlaceManager#checkPermissions}. The
     * error string returned by that call will be provided as in the
     * exception.
     */
    public PlaceManager createPlace (
        PlaceConfig config, CreationObserver observer)
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

        // stick the manager on the creation queue because we know
        // we'll get our calls to objectAvailable()/requestFailed() in
        // the order that we call createObject()
        _createq.append(
            new Tuple<PlaceManager,CreationObserver>(pmgr, observer));

        // and request to create the place object
        @SuppressWarnings("unchecked") Class<PlaceObject> pclass =
            (Class<PlaceObject>)pmgr.getPlaceObjectClass();
        _omgr.createObject(pclass, this);

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

    // documentation inherited
    public void objectAvailable (PlaceObject plobj)
    {
        // pop the next place manager off of the queue and let it know
        // that everything went swimmingly
        Tuple<PlaceManager,CreationObserver> tuple = _createq.getNonBlocking();
        if (tuple == null) {
            Log.warning("Place created but no manager queued up to hear " +
                        "about it!? [pobj=" + plobj + "].");
            return;
        }

        PlaceManager pmgr = tuple.left;
        CreationObserver observer = tuple.right;

        // stick the manager into our table
        _pmgrs.put(plobj.getOid(), pmgr);

        // start the place manager up with the newly created place object
        try {
            pmgr.startup(plobj);
        } catch (Exception e) {
            Log.warning("Error starting place manager [obj=" + plobj +
                ", pmgr=" + pmgr + "].");
            Log.logStackTrace(e);
        }

        // inform the creation observer that the place object was created
        // and provided to the manager
        if (observer != null) {
            try {
                observer.placeCreated(plobj, pmgr);
            } catch (Exception e) {
                Log.warning("Error informing CreationObserver of place " +
                            "[obj=" + plobj + ", pmgr=" + pmgr +
                            ", obs=" + observer + "].");
                Log.logStackTrace(e);
            }
        }
    }

    // documentation inherited
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        // pop a place manager off the queue since it is queued up to
        // manage the failed place object
        Tuple<PlaceManager,CreationObserver> tuple = _createq.getNonBlocking();
        if (tuple == null) {
            Log.warning("Place creation failed but no manager queued " +
                        "up to hear about it!? [cause=" + cause + "].");
            return;
        }

        Log.warning("Failed to create place object [mgr=" + tuple.left +
                    ", cause=" + cause + "].");
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

    /** A queue of place managers waiting for their place objects. */
    protected Queue<Tuple<PlaceManager,CreationObserver>> _createq =
        new Queue<Tuple<PlaceManager,CreationObserver>>();

    /** A mapping from place object id to place manager. */
    protected HashIntMap<PlaceManager> _pmgrs = new HashIntMap<PlaceManager>();
}
