//
// $Id: PlaceManager.java,v 1.3 2001/08/01 03:22:54 mdb Exp $

package com.threerings.cocktail.party.server;

import com.threerings.cocktail.cher.dobj.*;

import com.threerings.cocktail.party.data.PlaceObject;

/**
 * The place manager is the server-side entity that handles all
 * place-related interaction. It subscribes to the place object and reacts
 * to message and other events. Behavior specific to a place (or class of
 * places) should live in the place manager. An intelligently constructed
 * hierarchy of place manager classes working in concert with invocation
 * services should provide the majority of the server-side functionality
 * of an application built on the Cocktail platform.
 *
 * <p> The base place manager class takes care of the necessary
 * interactions with the place registry to manage place registration. It
 * handles the place-related component of chatting. It also provides the
 * basis for place-based access control.
 */
public class PlaceManager implements Subscriber
{
    /**
     * Called by the place manager after the place object has been
     * successfully created.
     */
    public void init (PlaceObject plobj)
    {
        // keep track of this
        _plobj = plobj;

        // we'll want to be included among the place object's subscribers;
        // we know that we can call addSubscriber() directly because the
        // place manager is doing all of our initialization on the dobjmgr
        // thread
        plobj.addSubscriber(this);

        // let our derived classes do their thang
        didInit();
    }

    protected void didInit ()
    {
    }

    /**
     * Returns the place object managed by this place manager.
     */
    public PlaceObject getPlaceObject ()
    {
        return _plobj;
    }

    public void objectAvailable (DObject object)
    {
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
    }

    public boolean handleEvent (DEvent event, DObject target)
    {
        return true;
    }

    /**
     * Called by the place registry after creating this place manager.
     * This is necessary so that the manager can inform the place registry
     * when the place goes away.
     */
    public void setRegistry (PlaceRegistry registry)
    {
        _registry = registry;
    }

    protected PlaceObject _plobj;
    protected PlaceRegistry _registry;
}
