//
// $Id: PlaceRegistry.java,v 1.12 2001/10/12 00:03:02 mdb Exp $

package com.threerings.crowd.server;

import java.util.Iterator;

import com.samskivert.util.Config;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Queue;

import com.threerings.presents.dobj.*;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

/**
 * The place registry keeps track of all of the active places in the
 * server. It should be used to create new places and it will take care of
 * instantiating and initializing a place manager to manage newly created
 * places.
 */
public class PlaceRegistry implements Subscriber
{
    /**
     * Creates and initializes the place registry; called by the server
     * during its initialization phase.
     */
    public PlaceRegistry (Config config)
    {
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
     *
     * @return a reference to the place manager that will manage the new
     * place object.
     *
     * @exception InstantiationException thrown if an error occurs trying
     * to instantiate and initialize the place manager.
     */
    public PlaceManager createPlace (PlaceConfig config)
        throws InstantiationException
    {
        try {
            // load up the manager class
            Class pmgrClass = Class.forName(config.getManagerClassName());
            // create a place manager for this place
            PlaceManager pmgr = (PlaceManager)pmgrClass.newInstance();
            // let the pmgr know about us and its configuration
            pmgr.init(this, config);

            // stick the manager on the creation queue because we know
            // we'll get our calls to objectAvailable()/requestFailed() in
            // the order that we call createObject()
            _createq.append(pmgr);

            // and request to create the place object
            CrowdServer.omgr.createObject(
                pmgr.getPlaceObjectClass(), this, false);

            return pmgr;

        } catch (Exception e) {
            Log.logStackTrace(e);
            throw new InstantiationException(
                "Error creating place manager [config=" + config + "].");
        }
    }

    /**
     * Returns the place manager associated with the specified place
     * object id or null if no such place exists.
     */
    public PlaceManager getPlaceManager (int placeOid)
    {
        return (PlaceManager)_pmgrs.get(placeOid);
    }

    /**
     * Returns an enumeration of all of the registered place objects. This
     * should only be accessed on the dobjmgr thread and shouldn't be kept
     * around across event dispatches.
     */
    public Iterator getPlaces ()
    {
        final Iterator enum = _pmgrs.elements();
        return new Iterator() {
            public boolean hasNext ()
            {
                return enum.hasNext();
            }

            public Object next ()
            {
                PlaceManager plmgr = (PlaceManager)enum.next();
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
    public Iterator getPlaceManagers ()
    {
        return _pmgrs.elements();
    }

    /**
     * Unregisters the place from the registry. Called by the place
     * manager when a place object that it was managing is destroyed.
     */
    public void placeWasDestroyed (int oid)
    {
        // remove the place manager from the table
        _pmgrs.remove(oid);
    }

    public void objectAvailable (DObject object)
    {
        // pop the next place manager off of the queue and let it know
        // that everything went swimmingly
        PlaceManager pmgr = (PlaceManager)_createq.getNonBlocking();
        if (pmgr == null) {
            Log.warning("Place created but no manager queued up to hear " +
                        "about it!? [pobj=" + object + "].");
            return;
        }

        // make sure it's the right kind of object
        if (!(object instanceof PlaceObject)) {
            Log.warning("Place registry notified of the creation of " +
                        "non-place object!? [obj=" + object + "].");
            return;
        }

        // stick the manager into our table
        _pmgrs.put(object.getOid(), pmgr);

        // start the place manager up with the newly created place object
        pmgr.startup((PlaceObject)object);
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        // pop a place manager off the queue since it is queued up to
        // manage the failed place object
        PlaceManager pmgr = (PlaceManager)_createq.getNonBlocking();
        if (pmgr == null) {
            Log.warning("Place creation failed but no manager queued " +
                        "up to hear about it!? [cause=" + cause + "].");
            return;
        }

        Log.warning("Failed to create place object [mgr=" + pmgr +
                    ", cause=" + cause + "].");
    }

    protected Queue _createq = new Queue();
    protected HashIntMap _pmgrs = new HashIntMap();
}
