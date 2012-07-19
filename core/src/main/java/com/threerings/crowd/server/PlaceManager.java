//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.Interval;
import com.samskivert.util.MethodFinder;
import com.samskivert.util.StringUtil;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.DynamicListener;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;
import com.threerings.presents.dobj.NamedSetAdapter;
import com.threerings.presents.dobj.ObjectAddedEvent;
import com.threerings.presents.dobj.ObjectDeathListener;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.dobj.ObjectRemovedEvent;
import com.threerings.presents.dobj.OidListListener;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.dobj.SetAdapter;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.SpeakMarshaller;
import com.threerings.crowd.chat.server.SpeakHandler;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.Place;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.CrowdObjectAccess.PlaceAccessController;

import static com.threerings.crowd.Log.log;

/**
 * The place manager is the server-side entity that handles all place-related interaction. It
 * subscribes to the place object and reacts to message and other events. Behavior specific to a
 * place (or class of places) should live in the place manager. An intelligently constructed
 * hierarchy of place manager classes working in concert with invocation services should provide
 * the majority of the server-side functionality of an application built on the Presents platform.
 *
 * <p> The base place manager class takes care of the necessary interactions with the place
 * registry to manage place registration. It handles the place-related component of chatting. It
 * also provides the basis for place-based access control.
 *
 * <p> A derived class is expected to handle initialization, cleanup and operational functionality
 * via the calldown functions {@link #didInit}, {@link #didStartup}, and {@link #didShutdown} as
 * well as through event listeners.
 */
public class PlaceManager
    implements MessageListener, SpeakHandler.SpeakerValidator
{
    /**
     * An interface used to allow the registration of standard message handlers to be invoked by
     * the place manager when particular types of message events are received.
     *
     * @deprecated Use dynamically bound methods instead. See {@link DynamicListener}.
     */
    @Deprecated
    public static interface MessageHandler
    {
        /**
         * Invokes this message handler on the supplied event.
         *
         * @param event the message event received.
         * @param pmgr the place manager for which the message is being handled.
         */
        void handleEvent (MessageEvent event, PlaceManager pmgr);
    }

    /**
     * Used to call methods on this place manager's delegates.
     */
    public static abstract class DelegateOp
    {
        public DelegateOp (Class<? extends PlaceManagerDelegate> delegateClass) {
            _delegateClass = delegateClass;
        }

        /** Applies an operation to the supplied delegate. */
        public abstract void apply (PlaceManagerDelegate delegate);

        public boolean shouldApply (PlaceManagerDelegate delegate) {
            return _delegateClass.isInstance(delegate);
        }

        protected Class<? extends PlaceManagerDelegate> _delegateClass;
    }

    /**
     * Returns a reference to our place configuration object.
     */
    public PlaceConfig getConfig ()
    {
        return _config;
    }

    /**
     * Returns a {@link Place} instance that identifies this place.
     */
    public Place getLocation ()
    {
        return new Place(_plobj.getOid());
    }

    /**
     * Returns the place object managed by this place manager.
     */
    public PlaceObject getPlaceObject ()
    {
        return _plobj;
    }

    /**
     * Applies the supplied occupant operation to each occupant currently present in this place.
     */
    public void applyToOccupants (OccupantOp op)
    {
        if (_plobj != null) {
            for (OccupantInfo info : _plobj.occupantInfo) {
                op.apply(info);
            }
        }
    }

    /**
     * Calls the supplied updater on the canonical occupant info record for the specified body
     * (which must be an occupant of this place) and broadcasts the update to all other occupants.
     *
     * @return true if the updater was called and the update sent, false if the body could not be
     * located (was not an occupant of this place) or the updater made no modifications.
     *
     * @exception ClassCastException thrown if the type of the supplied updater does not match the
     * type of {@link OccupantInfo} record used for the occupant. Caveat utilitor.
     */
    public <T extends OccupantInfo> boolean updateOccupantInfo (
        int bodyOid, OccupantInfo.Updater<T> updater)
    {
        @SuppressWarnings("unchecked") T info = (T)_occInfo.get(bodyOid);
        if (info == null || !updater.update(info)) {
            return false;
        }
        // update the canonical copy
        _occInfo.put(info.getBodyOid(), info);
        // clone the canonical copy and send an event updating the distributed set with that clone
        _plobj.updateOccupantInfo(info.clone());
        return true;
    }

    /**
     * Called by the place registry after creating this place manager.
     */
    public void init (PlaceRegistry registry, InvocationManager invmgr, RootDObjectManager omgr,
                      BodyLocator locator, PlaceConfig config)
    {
        _registry = registry;
        _invmgr = invmgr;
        _omgr = omgr;
        _locator = locator;
        _config = config;

        // initialize our delegates
        applyToDelegates(new DelegateOp(PlaceManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                delegate.init(PlaceManager.this, _omgr, _invmgr);
            }
        });

        // let derived classes do initialization stuff
        try {
            didInit();
        } catch (Throwable t) {
            log.warning("Manager choked in didInit()", "where", where(), t);
        }
    }

    /**
     * Adds the supplied delegate to the list for this manager.
     */
    public void addDelegate (PlaceManagerDelegate delegate)
    {
        if (_delegates == null) {
            _delegates = Lists.newArrayList();
        }
        if (_omgr != null) {
            delegate.init(this, _omgr, _invmgr);
            delegate.didInit(_config);
        }
        _delegates.add(delegate);
    }

    /**
     * Applies the supplied operation to this manager's registered delegates.
     */
    public void applyToDelegates (DelegateOp op)
    {
        if (_delegates != null) {
            for (int ii = 0, ll = _delegates.size(); ii < ll; ii++) {
                PlaceManagerDelegate delegate = _delegates.get(ii);
                if (op.shouldApply(delegate)) {
                    op.apply(delegate);
                }
            }
        }
    }

    /**
     * Provides an opportunity for place managers to ratify the creation of a place based on
     * whatever criterion they may require (based on information available to the manager at this
     * post-init() but pre-startup() phase of initialization).
     *
     * @return If a permissions check is to fail, the manager should return a translatable string
     * explaining the failure.  <code>null</code> should be returned if initialization is to be
     * allowed to proceed.
     */
    public String checkPermissions ()
    {
        return null;
    }

    /**
     * Called by the place manager after the place object has been successfully created.
     */
    public void startup (PlaceObject plobj)
    {
        // keep track of this
        _plobj = plobj;

        // we usually want to create and register a speaker service instance that clients can use
        // to speak in this place
        if (shouldCreateSpeakService()) {
            plobj.setSpeakService(addProvider(createSpeakHandler(plobj), SpeakMarshaller.class));
        }

        // we'll need to hear about place object events
        plobj.addListener(this);
        plobj.addListener(_bodyUpdater);
        plobj.addListener(_occListener);
        plobj.addListener(_deathListener);

        // configure this place's access controller
        plobj.setAccessController(getAccessController());

        // let our derived classes do their thang
        try {
            didStartup();
        } catch (Throwable t) {
            log.warning("Manager choked in didStartup()", "where", where(), t);
        }

        // since we start empty, we need to immediately assume shutdown
        checkShutdownInterval();
    }

    /**
     * Causes the place object being managed by this place manager to be destroyed and the place
     * manager to shut down.
     */
    public void shutdown ()
    {
        // destroy the object and everything will follow from that
        _omgr.destroyObject(_plobj.getOid());

        // make sure we don't have any shutdowner in the queue
        cancelShutdowner();
    }

    /**
     * Provides an opportunity for the place manager to prevent bodies from entering.
     *
     * @return <code>null</code> if the body can enter, otherwise a translatable message explaining
     * the reason the body is blocked from entering
     */
    public String ratifyBodyEntry (BodyObject body)
    {
        return null;
    }

    /**
     * This is called to inform the manager that a body is on the way in. This is called at the
     * very beginning of the entry process before the client is informed that it is allowed to
     * enter. This will be followed by a call to {@link #bodyEntered} once all events relating to
     * body entry have been processed.
     */
    public void bodyWillEnter (BodyObject body)
    {
        // create a new occupant info instance and insert it into our canonical table
        OccupantInfo info = body.createOccupantInfo(_plobj);
        _occInfo.put(info.getBodyOid(), info);

        _plobj.startTransaction();
        try {
            addOccupantInfo(body, info.clone());
        } finally {
            _plobj.commitTransaction();
        }
    }

    /**
     * Called to inform a manager that a body is about to leave this place. This will be followed
     * by a call to {@link #bodyLeft} once all events relating to body entry have been processed.
     */
    public void bodyWillLeave (BodyObject body)
    {
        _plobj.startTransaction();
        try {
            // remove their occupant info (which is keyed on oid)
            _plobj.removeFromOccupantInfo(body.getOid());
            // and remove them from the occupant list
            _plobj.removeFromOccupants(body.getOid());
        } finally {
            _plobj.commitTransaction();
        }
    }

    /**
     * Registers a particular message handler instance to be used when processing message events
     * with the specified name.
     *
     * @param name the message name of the message events that should be handled by this handler.
     * @param handler the handler to be registered.
     *
     * @deprecated Use dynamically bound methods instead. See {@link DynamicListener}.
     */
    @Deprecated
    public void registerMessageHandler (String name, MessageHandler handler)
    {
        // create our handler map if necessary
        if (_msghandlers == null) {
            _msghandlers = Maps.newHashMap();
        }
        _msghandlers.put(name, handler);
    }

    // from interface MessageListener
    public void messageReceived (MessageEvent event)
    {
        if (_msghandlers != null) {
            MessageHandler handler = _msghandlers.get(event.getName());
            if (handler != null) {
                handler.handleEvent(event, this);
            }
        }

        // If the message is directed at us, see if it's a request for a method invocation
        if (event.isPrivate()) { // aka if (event instanceof ServerMessageEvent)
            // the first argument should be the client object of the caller or null if it is
            // a server-originated event
            int srcoid = event.getSourceOid();
            DObject source = (srcoid <= 0) ? null : _omgr.getObject(srcoid);
            String method = event.getName();
            Object[] args = event.getArgs(), nargs;

            // validate that this call is allowed
            if (!allowManagerCall(method)) {
                log.warning("Client tried to invoke forbidden manager call!",
                    "source", source, "method", method, "args", args);
                return;
            }

            if (args == null) {
                nargs = new Object[] { source };
            } else {
                nargs = new Object[args.length+1];
                nargs[0] = source;
                System.arraycopy(args, 0, nargs, 1, args.length);
            }

            // Lazily create our dispatcher now that it's actually getting a message
            if (_dispatcher == null) {
                Class<?> clazz = getClass();
                MethodFinder finder = _dispatcherFinders.get(clazz);
                if (finder == null) {
                    finder = new MethodFinder(clazz);
                    _dispatcherFinders.put(clazz, finder);
                }
                _dispatcher = new DynamicListener<DSet.Entry>(this, finder);
            }
            _dispatcher.dispatchMethod(method, nargs);
        }
    }

    // documentation inherited from interface
    public boolean isValidSpeaker (DObject speakObj, ClientObject speaker, byte mode)
    {
        // have a whitelist for valid modes (no broadcasting, that's done elsewhere)
        switch (mode) {
        default:
            return false;
        case ChatCodes.DEFAULT_MODE:
        case ChatCodes.THINK_MODE:
        case ChatCodes.EMOTE_MODE:
        case ChatCodes.SHOUT_MODE:
            break;
        }
        // only allow people in the room to speak.
        return _plobj.occupants.contains(speaker.getOid());
    }

    /**
     * Returns a string that can be used in log messages to identify the place as sensibly as
     * possible to the developer who has to puzzle over log output trying to figure out what's
     * going on. Derived place managers can override this and augment the default value (which is
     * simply the place object id) with useful identifying information.
     */
    public String where ()
    {
        return (_plobj == null) ? StringUtil.shortClassName(this) + ":-1" : _plobj.which();
    }

    /**
     * Generates a string representation of this manager. Does so in a way that makes it easier for
     * derived classes to add to the string representation.
     *
     * @see #toString(StringBuilder)
     */
    @Override
    public String toString ()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        toString(buf);
        buf.append("]");
        return buf.toString();
    }

    /**
     * Do we want to allow client code to invoke the specified method?
     *
     * By default, we do not.
     */
    protected boolean allowManagerCall (String method)
    {
        return false;
    }

    /**
     * Derived classes will generally override this method to create a custom {@link PlaceObject}
     * derivation that contains extra information.
     */
    protected PlaceObject createPlaceObject ()
    {
        try {
            return getPlaceObjectClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @deprecated Use {@link #createPlaceObject}.
     */
    @Deprecated
    protected Class<? extends PlaceObject> getPlaceObjectClass ()
    {
        return PlaceObject.class;
    }

    /**
     * Called after this place manager has been initialized with its configuration information but
     * before it has been started up with its place object reference. Derived classes can override
     * this function and perform any basic initialization that they desire.  They should of course
     * be sure to call <code>super.didInit()</code>.
     */
    protected void didInit ()
    {
        // initialize our delegates
        applyToDelegates(new DelegateOp(PlaceManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                delegate.didInit(_config);
            }
        });
    }

    /**
     * Called if the permissions check failed, to give place managers a chance to do any cleanup
     * that might be necessary due to their early initialization or permissions checking code.
     */
    protected void permissionsFailed ()
    {
    }

    /**
     * @return true if we should create a speaker service for our place object so that clients can
     * use it to speak in this place.
     */
    protected boolean shouldCreateSpeakService ()
    {
        return true;
    }

    /**
     * Creates an access controller for this place's distributed object, which by default is {@link
     * PlaceAccessController}.
     */
    protected AccessController getAccessController ()
    {
        return _injector.getInstance(PlaceAccessController.class);
    }

    /**
     * Derived classes should override this (and be sure to call <code>super.didStartup()</code>)
     * to perform any startup time initialization. The place object will be available by the time
     * this method is executed.
     */
    protected void didStartup ()
    {
        // let our delegates know that we've started up
        applyToDelegates(new DelegateOp(PlaceManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                delegate.didStartup(_plobj);
            }
        });
    }

    /**
     * Called when this place has been destroyed and the place manager has shut down (via a call to
     * {@link #shutdown}). Derived classes can override this method and perform any necessary
     * shutdown time processing.
     */
    protected void didShutdown ()
    {
        // clear out our listenership
        _plobj.removeListener(this);
        _plobj.removeListener(_bodyUpdater);
        _plobj.removeListener(_occListener);
        _plobj.removeListener(_deathListener);

        // clear out our invocation service registrations
        for (InvocationMarshaller<?> marsh : _marshallers) {
            _invmgr.clearDispatcher(marsh);
        }

        // let our delegates know that we've shut down
        applyToDelegates(new DelegateOp(PlaceManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                delegate.didShutdown();
            }
        });

        // if shutting down emptied the place and scheduled the shutdowner, clear that out
        cancelShutdowner();
    }

    /**
     * Registers an invocation provider and notes the registration such that it will be
     * automatically cleared when this manager shuts down.
     */
    protected <T extends InvocationMarshaller<?>> T addProvider (
        InvocationProvider prov, Class<T> mclass)
    {
        T marsh = _invmgr.registerProvider(prov, mclass);
        _marshallers.add(marsh);
        return marsh;
    }

    /**
     * Registers an invocation dispatcher and notes the registration such that it will be
     * automatically cleared when this manager shuts down.
     */
    protected <T extends InvocationMarshaller<?>> T addDispatcher (InvocationDispatcher<T> disp)
    {
        T marsh = _invmgr.registerDispatcher(disp);
        _marshallers.add(marsh);
        return marsh;
    }

    /**
     * Called when a body object enters this place.
     */
    protected void bodyEntered (final int bodyOid)
    {
        log.debug("Body entered", "where", where(), "oid", bodyOid);

        // let our delegates know what's up
        applyToDelegates(new DelegateOp(PlaceManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                delegate.bodyEntered(bodyOid);
            }
        });

        // if we were on the road to shutting down, step off
        cancelShutdowner();
    }

    /**
     * Called when a body object leaves this place.
     */
    protected void bodyLeft (final int bodyOid)
    {
        log.debug("Body left", "where", where(), "oid", bodyOid);

        // if their occupant info hasn't been removed (which may be the case if they logged off
        // rather than left via a MoveTo request), we need to get it on out of here
        Integer key = Integer.valueOf(bodyOid);
        if (_plobj.occupantInfo.containsKey(key)) {
            _plobj.removeFromOccupantInfo(key);
        }

        // clear out their canonical (local) occupant info record
        OccupantInfo leaver = _occInfo.remove(bodyOid);

        // let our delegates know what's up
        applyToDelegates(new DelegateOp(PlaceManagerDelegate.class) {
            @Override public void apply (PlaceManagerDelegate delegate) {
                delegate.bodyLeft(bodyOid);
            }
        });

        // if that leaves us with zero occupants, maybe do something
        if (shouldDeclareEmpty(leaver)) {
            placeBecameEmpty();
        }
    }

    /**
     * Adds this occupant's info to the {@link PlaceObject}. This is called in a transaction on the
     * place object so if a derived class needs to add additional information for an occupant it
     * should override this method. It may opt to add the information before calling super if it
     * wishes to rely on its information being configured when {@link #bodyEntered} is called.
     */
    protected void addOccupantInfo (BodyObject body, OccupantInfo info)
    {
        // clone the canonical copy and insert it into the DSet
        _plobj.addToOccupantInfo(info);

        // add the body oid to our place object's occupant list
        _plobj.addToOccupants(body.getOid());
    }

    /**
     * Returns whether the location should be marked as empty and potentially shutdown.
     */
    protected boolean shouldDeclareEmpty (OccupantInfo leaver)
    {
        return (_plobj.occupants.size() == 0);
    }

    /**
     * Called when a body's occupant info is updated.
     */
    protected void bodyUpdated (final OccupantInfo info)
    {
        // let our delegates know what's up
        applyToDelegates(new DelegateOp(PlaceManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                delegate.bodyUpdated(info);
            }
        });
    }

    /**
     * Called when we transition from having bodies in the place to not having any bodies in the
     * place. Some places may take this as a sign to pack it in, others may wish to stick
     * around. In any case, they can override this method to do their thing.
     */
    protected void placeBecameEmpty ()
    {
        // let our delegates know what's up
        applyToDelegates(new DelegateOp(PlaceManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                delegate.placeBecameEmpty();
            }
        });

//         Log.info("Place became empty " + where() + ".");

        checkShutdownInterval();
    }

    /**
     * Called on startup and when the place is empty.
     */
    protected void checkShutdownInterval ()
    {
        // queue up a shutdown interval, unless we've already got one.
        long idlePeriod = idleUnloadPeriod();
        if (idlePeriod > 0L && _shutdownInterval == null) {
            (_shutdownInterval = _omgr.newInterval(new Runnable() {
                public void run () {
                    log.debug("Unloading idle place '" + where() + "'.");
                    shutdown();
                }
            })).schedule(idlePeriod);
        }
    }

    /**
     * Cancels any registered shutdown interval.
     */
    protected void cancelShutdowner ()
    {
        if (_shutdownInterval != null) {
            _shutdownInterval.cancel();
            _shutdownInterval = null;
        }
    }

    /**
     * Returns the period (in milliseconds) of emptiness after which this place manager will unload
     * itself and shutdown. Returning <code>0</code> indicates that the place should never be
     * shutdown.
     */
    protected long idleUnloadPeriod ()
    {
        return 5 * 60 * 1000L;
    }

    /**
     * An extensible way to add to the string representation of this class. Override this (being
     * sure to call super) and append your info to the buffer.
     */
    protected void toString (StringBuilder buf)
    {
        buf.append("place=").append(_plobj);
        buf.append(", config=").append(_config);
    }

    /**
     * Creates the speak handler for this place. Derived classes can customize the speak handler if
     * they so desire.
     */
    protected SpeakHandler createSpeakHandler (PlaceObject plobj)
    {
        return new SpeakHandler(_locator, plobj, this);
    }

    /** Listens for occupant updates. */
    protected SetAdapter<OccupantInfo> _bodyUpdater =
        new NamedSetAdapter<OccupantInfo>(PlaceObject.OCCUPANT_INFO) {
        @Override
        public void namedEntryUpdated (EntryUpdatedEvent<OccupantInfo> event) {
            bodyUpdated(event.getEntry());
        }
    };

    /** Listens for body entry and departure. */
    protected OidListListener _occListener = new OidListListener() {
        public void objectAdded (ObjectAddedEvent event) {
            if (event.getName().equals(PlaceObject.OCCUPANTS)) {
                bodyEntered(event.getOid());
            }
        }
        public void objectRemoved (ObjectRemovedEvent event) {
            if (event.getName().equals(PlaceObject.OCCUPANTS)) {
                bodyLeft(event.getOid());
            }
        }
    };

    /** Listens for death of our place object. */
    protected ObjectDeathListener _deathListener = new ObjectDeathListener() {
        public void objectDestroyed (ObjectDestroyedEvent event) {
            // unregister ourselves
            _registry.unmapPlaceManager(PlaceManager.this);

            // let our derived classes and delegates shut themselves down
            try {
                didShutdown();
            } catch (Throwable t) {
                log.warning("Manager choked in didShutdown()", "where", where(), t);
            }
        }
    };

    /** We use this to inject dependencies into our access controller. */
    @Inject protected Injector _injector;

    /** A reference to the place registry with which we're registered. */
    protected PlaceRegistry _registry;

    /** The invocation manager with whom we register our game invocation services. */
    protected InvocationManager _invmgr;

    /** A distributed object manager for doing dobj stuff. */
    protected RootDObjectManager _omgr;

    /** Used to look up body objects by name. */
    protected BodyLocator _locator;

    /** A reference to the place object that we manage. */
    protected PlaceObject _plobj;

    /** A reference to the configuration for our place. */
    protected PlaceConfig _config;

    /** Message handlers are used to process message events. */
    protected Map<String, MessageHandler> _msghandlers;

    /** A list of the delegates in use by this manager. */
    protected List<PlaceManagerDelegate> _delegates;

    /** A list of services registered with {@link #addProvider} which will be automatically
     * cleared when this manager shuts down. */
    protected List<InvocationMarshaller<?>> _marshallers = Lists.newArrayList();

    /** Used to keep a canonical copy of the occupant info records. */
    protected HashIntMap<OccupantInfo> _occInfo = new HashIntMap<OccupantInfo>();

    /** The interval currently registered to shut this place down after a certain period of
     * idility, or null if no interval is currently registered. */
    protected Interval _shutdownInterval;

    /** Used to do method lookup magic when we receive message events. */
    protected DynamicListener<?> _dispatcher;

    /** Maps from a PlaceManager subclass to a MethodFinder for it. When there are many many
     * instances of a PlaceManager in existence, having a MethodFinder instance for each gets quite
     * expensive. */
    protected static Map<Class<?>, MethodFinder> _dispatcherFinders = Maps.newHashMap();
}
