//
// $Id: PlaceManager.java,v 1.40 2002/10/31 01:12:08 shaper Exp $

package com.threerings.crowd.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import com.samskivert.util.HashIntMap;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationManager;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;
import com.threerings.presents.dobj.ObjectAddedEvent;
import com.threerings.presents.dobj.ObjectDeathListener;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.dobj.ObjectRemovedEvent;
import com.threerings.presents.dobj.OidListListener;
import com.threerings.presents.dobj.SetListener;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.crowd.chat.SpeakDispatcher;
import com.threerings.crowd.chat.SpeakMarshaller;
import com.threerings.crowd.chat.SpeakProvider;

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
 * operational functionality via the calldown functions {@link #didInit},
 * {@link #didStartup}, and {@link #didShutdown} as well as through event
 * listeners.
 */
public class PlaceManager
    implements MessageListener, OidListListener, ObjectDeathListener,
               SetListener, SpeakProvider.SpeakerValidator
{
    /**
     * An interface used to allow the registration of standard message
     * handlers to be invoked by the place manager when particular types
     * of message events are received.
     */
    public static interface MessageHandler
    {
        /**
         * Invokes this message handler on the supplied event.
         *
         * @param event the message event received.
         * @param pmgr the place manager for which the message is being
         * handled.
         */
        public void handleEvent (MessageEvent event, PlaceManager pmgr);
    }

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
     * Returns the occupant info record for the user with the specified
     * body oid, if they are an occupant of this room. Returns null
     * otherwise.
     */
    public OccupantInfo getOccupantInfo (int bodyOid)
    {
        return (OccupantInfo)_occInfo.get(bodyOid);
    }

    /**
     * Applies the supplied occupant operation to each occupant currently
     * present in this place.
     */
    public void applyToOccupants (OccupantOp op)
    {
        if (_plobj != null) {
            Iterator iter = _plobj.occupantInfo.entries();
            while (iter.hasNext()) {
                op.apply((OccupantInfo)iter.next());
            }
        }
    }

    /**
     * Updates the occupant info for this room occupant. <em>Note:</em>
     * This must be used rather than setting the occupant info directly to
     * avoid possible complications due to rapid fire changes to a user's
     * occupant info. The occupant info record supplied to this method
     * must be one returned from {@link #getOccupantInfo}. For example:
     *
     * <pre>
     * OccupantInfo info = _plmgr.getOccupantInfo(bodyOid);
     * // ... modifications made to 'info'
     * _plmgr.updateOccupantInfo(info);
     * </pre>
     */
    public void updateOccupantInfo (OccupantInfo occInfo)
    {
        // update the canonical copy
        _occInfo.put(occInfo.getBodyOid(), occInfo);
        // clone the canonical copy and send out an event updating the
        // distributed set with that clone
        _plobj.updateOccupantInfo((OccupantInfo)occInfo.clone());
    }

    /**
     * A place manager derived class is likely to have a corresponding
     * derived class of {@link com.threerings.crowd.data.PlaceObject} that
     * it will be managing.  Derived classes should override this method
     * and return the class object for the place object derived class they
     * desire to use. The place registry will use this method to create
     * the proper place object during the place creation process.
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
    public void init (
        PlaceRegistry registry, InvocationManager invmgr,
        DObjectManager omgr, PlaceConfig config)
    {
        _registry = registry;
        _invmgr = invmgr;
        _omgr = omgr;
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
        // initialize our delegates
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                delegate.didInit(_config);
            }
        });
    }

    /**
     * Provides an opportunity for place managers to ratify the creation
     * of a place based on whatever criterion they may require (based on
     * information available to the manager at this post-init() but
     * pre-startup() phase of initialization).
     *
     * @return If a permissions check is to fail, the manager should
     * return a translatable string explaining the failure.
     * <code>null</code> should be returned if initialization is to be
     * allowed to proceed.
     */
    public String checkPermissions ()
    {
        return null;
    }

    /**
     * Called if the permissions check failed, to give place managers a
     * chance to do any cleanup that might be necessary due to their early
     * initialization or permissions checking code.
     */
    protected void permissionsFailed ()
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

        // create and register a speaker service instance that clients can
        // use to speak in this place
        SpeakMarshaller speakService =
            (SpeakMarshaller)_invmgr.registerDispatcher(
                new SpeakDispatcher(new SpeakProvider(_plobj, this)), false);
        plobj.setSpeakService(speakService);

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
        // let our delegates know that we've started up
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                delegate.didStartup(_plobj);
            }
        });
    }

    /**
     * Causes the place object being managed by this place manager to be
     * destroyed and the place manager to shut down.
     */
    public void shutdown ()
    {
        // destroy the object and everything will follow from that
        CrowdServer.omgr.destroyObject(_plobj.getOid());

        // clear out our services
        _invmgr.clearDispatcher(_plobj.speakService);
    }

    /**
     * Called when this place has been destroyed and the place manager has
     * shut down (via a call to {@link #shutdown}). Derived classes can
     * override this method and perform any necessary shutdown time
     * processing.
     */
    protected void didShutdown ()
    {
        // let our delegates know that we've shut down
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                delegate.didShutdown();
            }
        });
    }

    /**
     * Returns the appropriate derived class of {@link OccupantInfo} that
     * will be used to provide occupant info for this body. An occupant
     * info record is created when a body enters a place.
     *
     * @param body the body that is entering the place and for whom we are
     * creating an occupant info record.
     */
    protected Class getOccupantInfoClass (BodyObject body)
    {
        return OccupantInfo.class;
    }

    /**
     * Builds an occupant info record for the specified body object and
     * inserts it into our place object. This is called by the location
     * services when a body enters a place. It should not be overridden by
     * derived classes, they should override {@link
     * #populateOccupantInfo}, which is set up for that sort of thing.
     */
    public OccupantInfo buildOccupantInfo (BodyObject body)
    {
        try {
            // create a new occupant info instance
            OccupantInfo info = (OccupantInfo)
                getOccupantInfoClass(body).newInstance();

            // configure it with the appropriate values
            populateOccupantInfo(info, body);

            // insert the occupant info into our canonical table
            _occInfo.put(info.getBodyOid(), info);

            // clone the canonical copy and insert it into the DSet
            _plobj.addToOccupantInfo((OccupantInfo)info.clone());

            return info;

        } catch (Exception e) {
            Log.warning("Failure building occupant info " +
                        "[where=" + where() + ", body=" + body + "].");
            Log.logStackTrace(e);
            return null;
        }
    }

    /**
     * Returns a string that can be used in log messages to identify the
     * place as sensibly as possible to the developer who has to puzzle
     * over log output trying to figure out what's going on. Derived place
     * managers can override this and augment the default value (which is
     * simply the place object id) with useful identifying information.
     */
    protected String where ()
    {
        return String.valueOf(_plobj.getOid());
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
    protected void bodyEntered (final int bodyOid)
    {
        Log.debug("Body entered [where=" + where() + ", oid=" + bodyOid + "].");

        // let our delegates know what's up
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                delegate.bodyEntered(bodyOid);
            }
        });
    }

    /**
     * Called when a body object leaves this place.
     */
    protected void bodyLeft (final int bodyOid)
    {
        Log.debug("Body left [where=" + where() + ", oid=" + bodyOid + "].");

        // if their occupant info hasn't been removed (which may be the
        // case if they logged off rather than left via a MoveTo request),
        // we need to get it on out of here
        Object key = new Integer(bodyOid);
        if (_plobj.occupantInfo.containsKey(key)) {
            _plobj.removeFromOccupantInfo(key);
        }

        // clear out their canonical (local) occupant info record
        _occInfo.remove(bodyOid);

        // let our delegates know what's up
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                delegate.bodyLeft(bodyOid);
            }
        });

        // if that leaves us with zero occupants, maybe do something
        if (_plobj.occupants.size() == 0) {
            placeBecameEmpty();
        }
    }

    /**
     * Called when a body's occupant info is updated.
     */
    protected void bodyUpdated (final OccupantInfo info)
    {
        // let our delegates know what's up
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                delegate.bodyUpdated(info);
            }
        });
    }

    /**
     * Called when we transition from having bodies in the place to not
     * having any bodies in the place. Some places may take this as a sign
     * to pack it in, others may wish to stick around. In any case, they
     * can override this method to do their thing.
     */
    protected void placeBecameEmpty ()
    {
        // let our delegates know what's up
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                delegate.placeBecameEmpty();
            }
        });
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
            handler.handleEvent(event, this);
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
     * Handles place destruction. We shut ourselves down and ask the place
     * registry to unmap us.
     */
    public void objectDestroyed (ObjectDestroyedEvent event)
    {
        // unregister ourselves
        _registry.unmapPlaceManager(this);

        // let our derived classes and delegates shut themselves down
        didShutdown();
    }

    // documentation inherited from interface
    public void entryAdded (EntryAddedEvent event)
    {
    }

    // documentation inherited from interface
    public void entryUpdated (EntryUpdatedEvent event)
    {
        if (event.getName().equals(PlaceObject.OCCUPANT_INFO)) {
            bodyUpdated((OccupantInfo)event.getEntry());
        }
    }

    // documentation inherited from interface
    public void entryRemoved (EntryRemovedEvent event)
    {
    }

    // documentation inherited from interface
    public boolean isValidSpeaker (DObject speakObj, ClientObject speaker)
    {
        // only allow people in the room to speak
        return _plobj.occupants.contains(speaker.getOid());
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
     * Adds the supplied delegate to the list for this manager.
     */
    protected void addDelegate (PlaceManagerDelegate delegate)
    {
        if (_delegates == null) {
            _delegates = new ArrayList();
        }
        _delegates.add(delegate);
    }

    /**
     * Used to call methods in delegates.
     */
    protected static interface DelegateOp
    {
        public void apply (PlaceManagerDelegate delegate);
    }

    /**
     * Applies the supplied operation to the registered delegates.
     */
    protected void applyToDelegates (DelegateOp op)
    {
        if (_delegates != null) {
            int dcount = _delegates.size();
            for (int i = 0; i < dcount; i++) {
                op.apply((PlaceManagerDelegate)_delegates.get(i));
            }
        }
    }

    /** A reference to the place registry with which we're registered. */
    protected PlaceRegistry _registry;

    /** The invocation manager with whom we register our game invocation
     * services. */
    protected InvocationManager _invmgr;

    /** A distributed object manager for doing dobj stuff. */
    protected DObjectManager _omgr;

    /** A reference to the place object that we manage. */
    protected PlaceObject _plobj;

    /** A reference to the configuration for our place. */
    protected PlaceConfig _config;

    /** Message handlers are used to process message events. */
    protected HashMap _msghandlers;

    /** A list of the delegates in use by this manager. */
    protected ArrayList _delegates;

    /** Used to keep a canonical copy of the occupant info records. */
    protected HashIntMap _occInfo = new HashIntMap();
}
