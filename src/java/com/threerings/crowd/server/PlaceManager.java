//
// $Id: PlaceManager.java,v 1.5 2001/08/01 20:37:35 mdb Exp $

package com.threerings.cocktail.party.server;

import java.util.Properties;

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
 *
 * <p> A derived class is expected to achieve its functionality via the
 * callback functions:
 *
 * <pre>
 * protected void didStartup ()
 * protected void willShutdown ()
 * protected void didShutdown ()
 * </pre>
 *
 * as well as through additions to <code>handlEvent</code>.
 */
public class PlaceManager implements Subscriber
{
    /**
     * Called by the place registry after creating this place manager.
     * Initialization is followed by startup which will happen when the
     * place object to be managed is available.
     */
    public void init (PlaceRegistry registry, Properties config)
    {
        _registry = registry;
        _config = config;
    }

    /**
     * Called by the place manager after the place object has been
     * successfully created.
     */
    public void startup (PlaceObject plobj)
    {
        // keep track of this
        _plobj = plobj;

        // we'll want to be included among the place object's subscribers;
        // we know that we can call addSubscriber() directly because the
        // place manager is doing all of our initialization on the dobjmgr
        // thread
        plobj.addSubscriber(this);

        // let our derived classes do their thang
        didStartup();
    }

    /**
     * Derived classes should override this (and be sure to call
     * <code>super.didStartup()</code>) to perform any startup time
     * initialization. The place object will be available by the time this
     * method is executed.
     */
    protected void didStartup ()
    {
    }

    /**
     * Returns the place object managed by this place manager.
     */
    public PlaceObject getPlaceObject ()
    {
        return _plobj;
    }

    // nothing doing
    public void objectAvailable (DObject object)
    {
    }

    // nothing doing
    public void requestFailed (int oid, ObjectAccessException cause)
    {
    }

    /**
     * Derived classes can override this to handle events, but they must
     * be sure to pass unknown events up to their super class.
     */
    public boolean handleEvent (DEvent event, DObject target)
    {
        return true;
    }

    /** A reference to the place object that we manage. */
    protected PlaceObject _plobj;

    /** A reference to the place registry with which we're registered. */
    protected PlaceRegistry _registry;

    /** The configuration provided for this place manager. */
    protected Properties _config;
}
