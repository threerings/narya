//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.Interval;
import com.samskivert.util.StringUtil;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.PresentsDObjectMgr;

import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.DynamicListener;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;
import com.threerings.presents.dobj.ObjectAddedEvent;
import com.threerings.presents.dobj.ObjectDeathListener;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.dobj.ObjectRemovedEvent;
import com.threerings.presents.dobj.OidListListener;
import com.threerings.presents.dobj.ServerMessageEvent;
import com.threerings.presents.dobj.SetAdapter;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.crowd.chat.data.SpeakMarshaller;
import com.threerings.crowd.chat.server.SpeakDispatcher;
import com.threerings.crowd.chat.server.SpeakProvider;

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
    implements MessageListener, OidListListener, ObjectDeathListener, SpeakProvider.SpeakerValidator
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
     * Returns the occupant info record for the user with the specified body oid, if they are an
     * occupant of this room. Returns null otherwise.
     */
    public OccupantInfo getOccupantInfo (int bodyOid)
    {
        return _occInfo.get(bodyOid);
    }

    /**
     * Applies the supplied occupant operation to each occupant currently present in this place.
     */
    public void applyToOccupants (OccupantOp op)
    {
        if (_plobj != null) {
            Iterator iter = _plobj.occupantInfo.iterator();
            while (iter.hasNext()) {
                op.apply((OccupantInfo)iter.next());
            }
        }
    }

    /**
     * Updates the occupant info for this room occupant. <em>Note:</em> This must be used rather
     * than setting the occupant info directly to avoid possible complications due to rapid fire
     * changes to a user's occupant info. The occupant info record supplied to this method must be
     * one returned from {@link #getOccupantInfo}. For example:
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
        // clone the canonical copy and send out an event updating the distributed set with that
        // clone
        _plobj.updateOccupantInfo((OccupantInfo)occInfo.clone());
    }

    /**
     * Called by the place registry after creating this place manager.
     */
    public void init (PlaceRegistry registry, InvocationManager invmgr,
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
     * Called after this place manager has been initialized with its configuration information but
     * before it has been started up with its place object reference. Derived classes can override
     * this function and perform any basic initialization that they desire.  They should of course
     * be sure to call <code>super.didInit()</code>.
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
     * Adds the supplied delegate to the list for this manager.
     */
    public void addDelegate (PlaceManagerDelegate delegate)
    {
        if (_delegates == null) {
            _delegates = new ArrayList<PlaceManagerDelegate>();
        }
        _delegates.add(delegate);
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
            plobj.setSpeakService((SpeakMarshaller)_invmgr.registerDispatcher(
                                      new SpeakDispatcher(new SpeakProvider(plobj, this))));
        }

        // we'll need to hear about place object events
        plobj.addListener(this);
        plobj.addListener(_bodyUpdater);

        // configure this place's access controller
        plobj.setAccessController(getAccessController());

        // let our derived classes do their thang
        didStartup();

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
        CrowdServer.omgr.destroyObject(_plobj.getOid());

        // clear out our services
        if (_plobj.speakService != null) {
            _invmgr.clearDispatcher(_plobj.speakService);
        }

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
     * Builds an {@link OccupantInfo} record for the specified body object and inserts it into our
     * place object. This is called by the location services when a body enters a place. If a
     * derived class wishes to perform custom actions when an occupant is being inserted into a
     * room, they should override {@link #insertOccupantInfo}, if they want to react to a body
     * having entered, they should override {@link #bodyEntered}.
     */
    public OccupantInfo buildOccupantInfo (BodyObject body)
    {
        try {
            // create a new occupant info instance
            OccupantInfo info = body.createOccupantInfo(_plobj);

            // insert the occupant info into our canonical table; this is done in a method so that
            // derived classes
            insertOccupantInfo(info, body);

            // clone the canonical copy and insert it into the DSet
            _plobj.addToOccupantInfo((OccupantInfo)info.clone());

            return info;

        } catch (Exception e) {
            Log.warning("Failure building occupant info [where=" + where() +
                        ", body=" + body + "].");
            Log.logStackTrace(e);
            return null;
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
            _msghandlers = new HashMap<String,MessageHandler>();
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

        // If the message is directed at us, see if it's a request for
        // a method invocation
        if (event.isPrivate()) { // aka if (event instanceof ServerMessageEvent)
            // the first argument should be the client object of the caller or null if it is
            // a server-originated event
            int srcoid = event.getSourceOid();
            DObject source = (srcoid <= 0) ? null : CrowdServer.omgr.getObject(srcoid);
            Object[] args = event.getArgs(), nargs;
            if (args == null) {
                nargs = new Object[] { source };
            } else {
                nargs = new Object[args.length+1];
                nargs[0] = source;
                System.arraycopy(args, 0, nargs, 1, args.length);
            }
            _dispatcher.dispatchMethod(event.getName(), nargs);
        }
    }

    // from interface OidListListener
    public void objectAdded (ObjectAddedEvent event)
    {
        if (event.getName().equals(PlaceObject.OCCUPANTS)) {
            bodyEntered(event.getOid());
        }
    }

    // from interface OidListListener
    public void objectRemoved (ObjectRemovedEvent event)
    {
        if (event.getName().equals(PlaceObject.OCCUPANTS)) {
            bodyLeft(event.getOid());
        }
    }

    // from interface ObjectDeathListener
    public void objectDestroyed (ObjectDestroyedEvent event)
    {
        // unregister ourselves
        _registry.unmapPlaceManager(this);

        // let our derived classes and delegates shut themselves down
        didShutdown();
    }

    // documentation inherited from interface
    public boolean isValidSpeaker (DObject speakObj, ClientObject speaker, byte mode)
    {
        // only allow people in the room to speak
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
    public String toString ()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        toString(buf);
        buf.append("]");
        return buf.toString();
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
     * CrowdObjectAccess#PLACE}.
     */
    protected AccessController getAccessController ()
    {
        return CrowdObjectAccess.PLACE;
    }

    /**
     * Derived classes should override this (and be sure to call <code>super.didStartup()</code>)
     * to perform any startup time initialization. The place object will be available by the time
     * this method is executed.
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
     * Called when this place has been destroyed and the place manager has shut down (via a call to
     * {@link #shutdown}). Derived classes can override this method and perform any necessary
     * shutdown time processing.
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
     * Called when an occupant is being added to this place. This will be before the call to {@link
     * #bodyEntered} and gives the derived class a chance to set up additional information about
     * the occupant that might not be tracked in the occupant info.
     */
    protected void insertOccupantInfo (OccupantInfo info, BodyObject body)
    {
        _occInfo.put(info.getBodyOid(), info);
    }

    /**
     * Called when a body object enters this place.
     */
    protected void bodyEntered (final int bodyOid)
    {
        if (Log.log.getLevel() == Level.FINE) {
            Log.debug("Body entered [where=" + where() + ", oid=" + bodyOid + "].");
        }

        // let our delegates know what's up
        applyToDelegates(new DelegateOp() {
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
        if (Log.log.getLevel() == Level.FINE) {
            Log.debug("Body left [where=" + where() + ", oid=" + bodyOid + "].");
        }

        // if their occupant info hasn't been removed (which may be the case if they logged off
        // rather than left via a MoveTo request), we need to get it on out of here
        Integer key = Integer.valueOf(bodyOid);
        if (_plobj.occupantInfo.containsKey(key)) {
            _plobj.removeFromOccupantInfo(key);
        }

        // clear out their canonical (local) occupant info record
        OccupantInfo leaver = _occInfo.remove(bodyOid);

        // let our delegates know what's up
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceManagerDelegate delegate) {
                delegate.bodyLeft(bodyOid);
            }
        });

        // if that leaves us with zero occupants, maybe do something
        if (shouldDeclareEmpty(leaver)) {
            placeBecameEmpty();
        }
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
        applyToDelegates(new DelegateOp() {
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
        applyToDelegates(new DelegateOp() {
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
            _shutdownInterval = new Interval((PresentsDObjectMgr)_omgr) {
                public void expired () {
                    Log.debug("Unloading idle place '" + where () + "'.");
                    shutdown();
                }
            };
            _shutdownInterval.schedule(idlePeriod);
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
                op.apply(_delegates.get(i));
            }
        }
    }

    /** Listens for occupant updates. */
    protected SetAdapter _bodyUpdater = new SetAdapter() {
        public void entryUpdated (EntryUpdatedEvent event) {
            if (event.getName().equals(PlaceObject.OCCUPANT_INFO)) {
                bodyUpdated((OccupantInfo)event.getEntry());
            }
        }
    };

    /** A reference to the place registry with which we're registered. */
    protected PlaceRegistry _registry;

    /** The invocation manager with whom we register our game invocation services. */
    protected InvocationManager _invmgr;

    /** A distributed object manager for doing dobj stuff. */
    protected DObjectManager _omgr;

    /** A reference to the place object that we manage. */
    protected PlaceObject _plobj;

    /** A reference to the configuration for our place. */
    protected PlaceConfig _config;

    /** Message handlers are used to process message events. */
    protected HashMap<String,MessageHandler> _msghandlers;

    /** A list of the delegates in use by this manager. */
    protected ArrayList<PlaceManagerDelegate> _delegates;

    /** Used to keep a canonical copy of the occupant info records. */
    protected HashIntMap<OccupantInfo> _occInfo = new HashIntMap<OccupantInfo>();

    /** The interval currently registered to shut this place down after a certain period of
     * idility, or null if no interval is currently registered. */
    protected Interval _shutdownInterval;

    /** Used to do method lookup magic when we receive message events. */
    protected DynamicListener _dispatcher = new DynamicListener(this);
}
