//
// $Id: PlaceRegistry.java,v 1.1 2001/08/01 03:22:54 mdb Exp $

package com.threerings.cocktail.party.server;

import java.util.Enumeration;
import com.samskivert.util.Config;
import com.samskivert.util.Queue;

import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.cher.util.IntMap;

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
     * Creates and registers a new place along with a manager to manage
     * that place. The registry takes care of tracking the creation of the
     * object and informing the manager when it is created.
     *
     * @param pobjClass the <code>PlaceObject</code> derived class that
     * should be instantiated to create the place object.
     * @param pmgrClass the <code>PlaceManager</code> derived class that
     * should be instantiated to manage the place.
     */
    public void createPlace (Class pobjClass, Class pmgrClass)
    {
        // create a place manager for this place
        try {
            PlaceManager pmgr = (PlaceManager)pmgrClass.newInstance();
            // stick the manager on the creation queue because we know
            // we'll get our calls to objectAvailable()/requestFailed() in
            // the order that we call createObject()
            _createq.append(pmgr);
            // and request to create the place object
            PartyServer.omgr.createObject(pobjClass, this, false);

        } catch (Exception e) {
            Log.warning("Error creating place " +
                        "[pobjc=" + pobjClass.getName() +
                        ", pmgrc=" + pmgrClass.getName() +
                        ", error=" + e + "].");
        }
    }

    /**
     * Returns an enumeration of all of the registered place objects. This
     * should only be accessed on the dobjmgr thread and shouldn't be kept
     * around across event dispatches.
     */
    public Enumeration getPlaces ()
    {
        final Enumeration enum = _pmgrs.elements();
        return new Enumeration() {
            public boolean hasMoreElements ()
            {
                return enum.hasMoreElements();
            }

            public Object nextElement ()
            {
                PlaceManager plmgr = (PlaceManager)enum.nextElement();
                return (plmgr == null) ? null : plmgr.getPlaceObject();
            }
        };
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

        // initialize the place manager with the newly created place
        // object
        pmgr.init((PlaceObject)object);

        // stick the manager into our table
        _pmgrs.put(object.getOid(), pmgr);
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
    protected IntMap _pmgrs = new IntMap();
}
