//
// $Id: PlaceRegistry.java,v 1.9 2001/10/02 02:07:50 mdb Exp $

package com.threerings.cocktail.party.server;

import java.util.Iterator;

import com.samskivert.util.Config;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Queue;

import com.threerings.cocktail.cher.dobj.*;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.data.PlaceObject;

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
     * @param pmgrClass the {@link PlaceManager} derived class that should
     * be instantiated to manage the place.
     *
     * @return a reference to the place manager that will manage the new
     * place object.
     *
     * @exception InstantiationException thrown if an error occurs trying
     * to instantiate and initialize the place manager.
     */
    public PlaceManager createPlace (Class pmgrClass)
        throws InstantiationException
    {
        try {
            // create a place manager for this place
            PlaceManager pmgr = (PlaceManager)pmgrClass.newInstance();
            // let the pmgr know about us
            pmgr.setPlaceRegistry(this);

            // stick the manager on the creation queue because we know
            // we'll get our calls to objectAvailable()/requestFailed() in
            // the order that we call createObject()
            _createq.append(pmgr);

            // and request to create the place object
            PartyServer.omgr.createObject(
                pmgr.getPlaceObjectClass(), this, false);

            return pmgr;

        } catch (IllegalAccessException iae) {
            throw new InstantiationException(
                "Error instantiating place manager: " + iae);
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

    public boolean handleEvent (DEvent event, DObject target)
    {
        // this shouldn't be called because we don't subscribe to
        // anything, we just want to hear about object creation
        return false;
    }

    protected Queue _createq = new Queue();
    protected HashIntMap _pmgrs = new HashIntMap();
}
