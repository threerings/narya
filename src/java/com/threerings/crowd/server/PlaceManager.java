//
// $Id: PlaceManager.java,v 1.7 2001/08/04 01:13:36 mdb Exp $

package com.threerings.cocktail.party.server;

import java.util.HashMap;
import java.util.Properties;

import com.threerings.cocktail.cher.dobj.*;

import com.threerings.cocktail.party.Log;
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
 * as well as through additions to <code>handleEvent</code>.
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
     * Called when a body object enters this place.
     */
    protected void bodyEntered (int bodyOid)
    {
        Log.info("Body entered [ploid=" + _plobj.getOid() +
                 ", oid=" + bodyOid + "].");
    }

    /**
     * Called when a body object leaves this place.
     */
    protected void bodyLeft (int bodyOid)
    {
        Log.info("Body left [ploid=" + _plobj.getOid() +
                 ", oid=" + bodyOid + "].");
    }

    /**
     * Registers a particular message handler instance to be used when
     * processing message events with the specified name.
     *
     * @param name the message name of the message events that should be
     * handled by this handler.
     * @param handler the handler to be registered.
     */
    public void registerMessageHandler (String name, MessageHandler handler)
    {
        // create our handler map if necessary
        if (_msghandlers == null) {
            _msghandlers = new HashMap();
        }
        _msghandlers.put(name, handler);
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
        // if this is a message event, see if we have a handler for it
        if (event instanceof MessageEvent) {
            MessageEvent mevt = (MessageEvent)event;
            MessageHandler handler = (MessageHandler)
                _msghandlers.get(mevt.getName());
            if (handler != null) {
                handler.handleEvent(mevt, (PlaceObject)target);
            }

        } else if (event instanceof ObjectAddedEvent) {
            ObjectAddedEvent oae = (ObjectAddedEvent)event;
            if (oae.getName().equals(PlaceObject.OCCUPANTS)) {
                bodyEntered(oae.getOid());
            }

        } else if (event instanceof ObjectRemovedEvent) {
            ObjectRemovedEvent ore = (ObjectRemovedEvent)event;
            if (ore.getName().equals(PlaceObject.OCCUPANTS)) {
                bodyLeft(ore.getOid());
            }
        }

        return true;
    }

    /**
     * An interface used to allow the registration of standard message
     * handlers to be invoked by the place manager when particular types
     * of message events are received.
     */
    protected static interface MessageHandler
    {
        /**
         * Invokes this message handler on the supplied event.
         *
         * @param event the message event received.
         * @param target the place object on which the message event was
         * received.
         */
        public void handleEvent (MessageEvent event, PlaceObject target);
    }

    /** A reference to the place object that we manage. */
    protected PlaceObject _plobj;

    /** A reference to the place registry with which we're registered. */
    protected PlaceRegistry _registry;

    /** The configuration provided for this place manager. */
    protected Properties _config;

    /** Message handlers are used to process message events. */
    protected HashMap _msghandlers;
}
