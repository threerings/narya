//
// $Id: PlaceManager.java,v 1.18 2001/10/12 00:03:02 mdb Exp $

package com.threerings.crowd.server;

import java.util.HashMap;
import java.util.Properties;

import com.threerings.presents.dobj.*;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.*;

/**
 * The place manager is the server-side entity that handles all
 * place-related interaction. It subscribes to the place object and reacts
 * to message and other events. Behavior specific to a place (or class of
 * places) should live in the place manager. An intelligently constructed
 * hierarchy of place manager classes working in concert with invocation
 * services should provide the majority of the server-side functionality
 * of an application built on the Presents platform.
 *
 * <p> The base place manager class takes care of the necessary
 * interactions with the place registry to manage place registration. It
 * handles the place-related component of chatting. It also provides the
 * basis for place-based access control.
 *
 * <p> A derived class is expected to handle initialization, cleanup and
 * operational functionality via the calldown functions {@link
 * #didStartup}, {@link #willShutdown}, and {@link #didShutdown} as well
 * as through event listeners.
 */
public class PlaceManager
    implements MessageListener, OidListListener
{
    /**
     * Returns a reference to our place configuration object.
     */
    public PlaceConfig getConfig ()
    {
        return _config;
    }

    /**
     * Returns the place object managed by this place manager.
     */
    public PlaceObject getPlaceObject ()
    {
        return _plobj;
    }

    /**
     * A place manager derived class is likely to have a corresponding
     * derived class of {@link PlaceObject} that it will be managing.
     * Derived classes should override this method and return the class
     * object for the place object derived class they desire to use. The
     * place registry will use this method to create the proper place
     * object during the place creation process.
     *
     * @return the class of the class, derived from {@link PlaceObject},
     * that this manager wishes to manage.
     *
     * @see PlaceRegistry#createPlace
     */
    protected Class getPlaceObjectClass ()
    {
        return PlaceObject.class;
    }

    /**
     * Called by the place registry after creating this place manager.
     */
    public void init (PlaceRegistry registry, PlaceConfig config)
    {
        _registry = registry;
        _config = config;

        // let derived classes do initialization stuff
        didInit();
    }

    /**
     * Called after this place manager has been initialized with its
     * configuration information but before it has been started up with
     * its place object reference. Derived classes can override this
     * function and perform any basic initialization that they desire.
     * They should of course be sure to call <code>super.didInit()</code>.
     */
    protected void didInit ()
    {
    }

    /**
     * Called by the place manager after the place object has been
     * successfully created.
     */
    public void startup (PlaceObject plobj)
    {
        // keep track of this
        _plobj = plobj;

        // configure the occupant info set
        plobj.occupantInfo.setElementType(getOccupantInfoClass());

        // we'll need to hear about place object events
        plobj.addListener(this);

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

    // not called at present but will eventually be part of the shutdown
    // and cleanup process
    protected void willShutdown ()
    {
    }

    // not called at present but will eventually be part of the shutdown
    // and cleanup process
    protected void didShutdown ()
    {
    }

    /**
     * When the manager starts up, it configures its place object occupant
     * info set by setting the type of occupant info objects it will
     * contain. Managers that wish to use derived occupant info classes
     * should override this function and return a reference to their
     * derived class.
     */
    protected Class getOccupantInfoClass ()
    {
        return OccupantInfo.class;
    }

    /**
     * Builds an occupant info record for the specified body object. This
     * is called by the location services when a body enters a place. It
     * should not be overridden by derived classes, they should override
     * {@link #populateOccupantInfo}, which is set up for that sort of
     * thing.
     */
    public OccupantInfo buildOccupantInfo (BodyObject body)
    {
        // create a new occupant info instance
        try {
            OccupantInfo info = (OccupantInfo)
                getOccupantInfoClass().newInstance();
            populateOccupantInfo(info, body);
            return info;

        } catch (Exception e) {
            Log.warning("Failure building occupant info " +
                        "[body=" + body + "].");
            Log.logStackTrace(e);
            return null;
        }
    }

    /**
     * Derived classes should override this method if they are making use
     * of a derived occupant info class. They should call the super
     * implementation and then populate the occupant info fields in their
     * extended object.
     */
    protected void populateOccupantInfo (OccupantInfo info, BodyObject body)
    {
        // the base occupant info is only their username
        info.bodyOid = new Integer(body.getOid());
        info.username = body.username;
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

        // if their occupant info hasn't been removed (which may be the
        // case if they logged off rather than left via a MoveTo request),
        // we need to get it on out of here
        Object key = new Integer(bodyOid);
        if (_plobj.occupantInfo.containsKey(key)) {
            _plobj.removeFromOccupantInfo(key);
        }
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
     * Dispatches message events to registered message handlers. Derived
     * classes should probably register message handlers rather than
     * override this method directly.
     */
    public void messageReceived (MessageEvent event)
    {
        MessageHandler handler = null;
        if (_msghandlers != null) {
            handler = (MessageHandler)_msghandlers.get(event.getName());
        }
        if (handler != null) {
            handler.handleEvent(event, _plobj);
        }
    }

    /**
     * Handles occupant arrival into the place. Derived classes may need
     * to override this method to handle other oid lists in their derived
     * place objects. They should be sure to call
     * <code>super.objectAdded</code> if the event is one they don't
     * explicitly handle.
     */
    public void objectAdded (ObjectAddedEvent event)
    {
        if (event.getName().equals(PlaceObject.OCCUPANTS)) {
            bodyEntered(event.getOid());
        }
    }

    /**
     * Handles occupant departure from the place. Derived classes may need
     * to override this method to handle other oid lists in their derived
     * place objects. They should be sure to call
     * <code>super.objectRemoved</code> if the event is one they don't
     * explicitly handle.
     */
    public void objectRemoved (ObjectRemovedEvent event)
    {
        if (event.getName().equals(PlaceObject.OCCUPANTS)) {
            bodyLeft(event.getOid());
        }
    }

    /**
     * Generates a string representation of this manager. Does so in a way
     * that makes it easier for derived classes to add to the string
     * representation.
     *
     * @see #toString(StringBuffer)
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        toString(buf);
        buf.append("]");
        return buf.toString();
    }

    /**
     * An extensible way to add to the string representation of this
     * class. Override this (being sure to call super) and append your
     * info to the buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("place=").append(_plobj);
        buf.append(", config=").append(_config);
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

    /** A reference to the configuration for our place. */
    protected PlaceConfig _config;

    /** A reference to the place registry with which we're registered. */
    protected PlaceRegistry _registry;

    /** Message handlers are used to process message events. */
    protected HashMap _msghandlers;
}
