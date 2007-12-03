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

package com.threerings.presents.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.logging.Level;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.ResultListener;
import com.samskivert.util.Throttle;
import com.threerings.util.Name;
import com.threerings.util.StreamableArrayList;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.ProxySubscriber;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.net.BootstrapNotification;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.EventNotification;
import com.threerings.presents.net.UpstreamMessage;

import com.threerings.presents.net.ForwardEventRequest;
import com.threerings.presents.net.LogoffRequest;
import com.threerings.presents.net.PingRequest;
import com.threerings.presents.net.SubscribeRequest;
import com.threerings.presents.net.UnsubscribeRequest;

import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.FailureResponse;
import com.threerings.presents.net.ObjectResponse;
import com.threerings.presents.net.PongResponse;
import com.threerings.presents.net.UnsubscribeResponse;

import com.threerings.presents.server.net.Connection;
import com.threerings.presents.server.net.MessageHandler;

import static com.threerings.presents.Log.log;

/**
 * Represents a client session in the server. It is associated with a connection instance (while
 * the client is connected) and acts as the intermediary for the remote client in terms of passing
 * along events forwarded by the client, ensuring that subscriptions are maintained on behalf of
 * the client and that events are forwarded to the client.
 *
 * <p><em>A note on synchronization:</em> the client object is structured so that its
 * <code>Subscriber</code> implementation (which is called from the dobjmgr thread) can proceed
 * without synchronization. This does not overlap with its other client duties which are called
 * from the conmgr thread and therefore also need not be synchronized.
 */
public class PresentsClient
    implements MessageHandler, ClientResolutionListener
{
    /** Used by {@link #setUsername} to report success or failure. */
    public static interface UserChangeListener
    {
        /** Called when the new client object has been resolved and the new client object reported
         * to the client, but the old one has not yet been destroyed. Any events delivered on this
         * callback to the old client object will be delivered.
         *
         * @param rl when this method is finished with its business and the old client object can
         * be destroyed, the result listener should be called. */
        public void changeReported (ClientObject newObji, ResultListener rl);

        /** Called when the user change is completed, the old client object is destroyed and all
         * updates are committed. */
        public void changeCompleted (ClientObject newObj);

        /** Called if some failure occurs during the user change process. */
        public void changeFailed (Exception cause);
    }

    /**
     * Returns the credentials used to authenticate this client.
     */
    public Credentials getCredentials ()
    {
        return _areq.getCredentials();
    }

    /**
     * Returns the time zone in which this client is operating.
     */
    public TimeZone getTimeZone ()
    {
        return _areq.getTimeZone();
    }

    /**
     * Returns true if this client has been disconnected for sufficiently long that its session
     * should be forcibly ended.
     */
    public boolean checkExpired (long now)
    {
        return (getConnection() == null && (now - _networkStamp > FLUSH_TIME));
    }

    /**
     * Returns the time at which this client started their network session.
     */
    public long getSessionStamp ()
    {
        return _sessionStamp;
    }

    /**
     * Returns the time at which this client most recently connected or disconnected.
     */
    public long getNetworkStamp ()
    {
        return _networkStamp;
    }

    /**
     * Returns the username with which this client instance is associated.
     */
    public Name getUsername ()
    {
        return _username;
    }

    /**
     * Returns the address of the connected client or null if this client is not connected.
     */
    public InetAddress getInetAddress ()
    {
        Connection conn = getConnection();
        return (conn == null) ? null : conn.getInetAddress();
    }

    /**
     * Configures this client with a custom class loader that will be used when unserializing
     * classes from the network.
     */
    public void setClassLoader (ClassLoader loader)
    {
        _loader = loader;
        Connection conn = getConnection();
        if (conn != null) {
            conn.setClassLoader(loader);
        }
    }

    /**
     * <em>Danger:</em> this method is not for general consumption. This changes the username of
     * the client, but should only be done very early in a user's session, when you know that no
     * one has mapped the user based on their username or has in any other way made use of their
     * username in a way that will break. However, it should not be done <em>too</em> early in the
     * session. The client must be fully resolved.
     *
     * <p> It exists to support systems wherein a user logs in with an account username and then
     * chooses a "screen name" by which they will play (often from a small set of available
     * "characters" available per account). This will take care of remapping the username to client
     * object mappings that were made by the Presents services when the user logs on, but anything
     * else that has had its grubby mits on the username will be left to its own devices, hence the
     * care that must be exercised when using this method.
     *
     * @param ucl an entity that will (optionally) be notified when the username conversion process
     * is complete.
     */
    public void setUsername (Name username, final UserChangeListener ucl)
    {
        ClientResolutionListener clr = new ClientResolutionListener() {
            public void clientResolved (final Name username, final ClientObject clobj)
            {
                // if they old client object is gone by now, they ended their session while we were
                // switching, so freak out
                if (_clobj == null) {
                    log.warning("Client disappeared before we could complete the switch to a " +
                                "new client object [ousername=" + _username +
                                ", nusername=" + username + "].");
                    _cmgr.releaseClientObject(username);
                    Exception error = new Exception("Early withdrawal");
                    resolutionFailed(username, error);
                    return;
                }

                // let the client know that the rug has been yanked out from under their ass
                Object[] args = new Object[] { Integer.valueOf(clobj.getOid()) };
                _clobj.postMessage(ClientObject.CLOBJ_CHANGED, args);

                // call down to any derived classes
                clientObjectWillChange(_clobj, clobj);

                // let the caller know that we've got some new business
                if (ucl != null) {
                    ucl.changeReported(clobj, new ResultListener() {
                        public void requestCompleted (Object result) {
                            finishResolved(username, clobj);
                        }
                        public void requestFailed (Exception cause) {
                            finishResolved(username, clobj);
                        }
                    });
                } else {
                    finishResolved(username, clobj);
                }
            }

            /**
             * Finish the final phase of the switch.
             */
            protected void finishResolved (Name username, ClientObject clobj)
            {
                // release our old client object; this will destroy it
                _cmgr.releaseClientObject(_username);

                // update our internal fields
                _username = username;
                _clobj = clobj;

                // call down to any derived classes
                clientObjectDidChange(_clobj);

                // let our listener know we're groovy
                if (ucl != null) {
                    ucl.changeCompleted(_clobj);
                }
            }

            public void resolutionFailed (Name username, Exception reason) {
                log.log(Level.WARNING, "Unable to resolve new client object [oldname=" + _username +
                        ", newname=" + username + ", reason=" + reason + "].", reason);

                // let our listener know we're hosed
                if (ucl != null) {
                    ucl.changeFailed(reason);
                }
            }
        };

        // resolve the new client object
        _cmgr.resolveClientObject(username, clr);
    }

    /**
     * Returns the client object that is associated with this client.
     */
    public ClientObject getClientObject ()
    {
        return _clobj;
    }

    /**
     * Forcibly terminates a client's session. This must be called from the dobjmgr thread.
     */
    public void endSession ()
    {
        // queue up a request for our connection to be closed (if we have a connection, that is)
        Connection conn = getConnection();
        if (conn != null) {
            // go ahead and clear out our connection now to prevent funniness
            setConnection(null);
            // have the connection manager close our connection when it is next convenient
            PresentsServer.conmgr.closeConnection(conn);
        }

        // if we don't have a client object, we failed to resolve in the first place, in which case
        // we have to cope as best we can
        if (_clobj != null) {
            // and clean up after ourselves
            try {
                sessionDidEnd();
            } catch (Exception e) {
                log.log(Level.WARNING, "Choked in sessionDidEnd " + this + ".", e);
            }

            // release (and destroy) our client object
            _cmgr.releaseClientObject(_username);
        }

        // let the client manager know that we're audi 5000
        _cmgr.clientSessionDidEnd(this);

        // clear out the client object so that we know the session is over
        _clobj = null;
    }

    /**
     * This is called when the server is shut down in the middle of a client session. In this
     * circumstance, {@link #endSession} will <em>not</em> be called and so any persistent data
     * that might normally be flushed at the end of a client's session should likely be flushed
     * here.
     */
    public void shutdown  ()
    {
        // if the client is connected, we need to fake the computation of their final connect time
        // because we won't be closing their socket normally
        if (getConnection() != null) {
            long now = System.currentTimeMillis();
            _connectTime += ((now - _networkStamp) / 1000);
        }
    }

    // from interface ClientResolutionListener
    public void clientResolved (Name username, ClientObject clobj)
    {
        // if our connection was closed while we were resolving our client object, then just
        // abandon ship
        if (getConnection() == null) {
            log.info("Session ended before client object could be resolved: " + this + ".");
            return;
        }

        // we'll be keeping this bad boy
        _clobj = clobj;

        // finish up our regular business
        sessionWillStart();
        sendBootstrap();

        // let the client manager know that we're operational
        _cmgr.clientSessionDidStart(this);
    }

    // from interface ClientResolutionListener
    public void resolutionFailed (Name username, Exception reason)
    {
        // urk; nothing to do but complain and get the f**k out of dodge
        log.log(Level.WARNING, "Unable to resolve client [username=" + username + "].", reason);

        // end the session now to prevent danglage
        endSession();
    }

    // from interface MessageHandler
    public void handleMessage (UpstreamMessage message)
    {
        _messagesIn++; // count 'em up!

        // if the client has been getting crazy with the cheeze whiz, stick a fork in them; the
        // first time through we end our session, subsequently _throttle is null and we just drop
        // any messages that come in until we've fully shutdown
        if (_throttle == null) {
//             log.info("Dropping message from force-quit client [conn=" + _conn +
//                      ", msg=" + message + "].");
            return;

        } else if (_throttle.throttleOp(message.received)) {
            handleThrottleExceeded();
        }

        // we dispatch to a message dispatcher that is specialized for the particular class of
        // message that we received
        MessageDispatcher disp = _disps.get(message.getClass());
        if (disp == null) {
            log.warning("No dispatcher for message [msg=" + message + "].");
            return;
        }

        // otherwise pass the message on to the dispatcher
        disp.dispatch(this, message);
    }

    /**
     * Called when {@link #setUsername} has been called and the new client object is about to be
     * applied to this client. The old client object will not yet have been destroyed, so any final
     * events can be sent along prior to the new object being put into effect.
     */
    protected void clientObjectWillChange (
        ClientObject oldClobj, ClientObject newClobj)
    {
    }

    /**
     * Called after the new client object has been committed to this client due to a call to {@link
     * #setUsername}.
     */
    protected void clientObjectDidChange (ClientObject newClobj)
    {
    }

    /**
     * Initializes this client instance with the specified username, connection instance and client
     * object and begins a client session.
     */
    protected void startSession (
        ClientManager cmgr, AuthRequest req, Connection conn, Object authdata)
    {
        _cmgr = cmgr;
        _areq = req;
        _authdata = authdata;
        setConnection(conn);

        // obtain our starting username
        assignStartingUsername();

        // resolve our client object before we get fully underway
        cmgr.resolveClientObject(_username, this);

        // make a note of our session start time
        _sessionStamp = System.currentTimeMillis();
    }

    /**
     * This is factored out to allow derived classes to use a different starting username than the
     * one supplied in the user's credentials.  Generally one only wants to munge the starting
     * username if the user will subsequently choose a "screen name" and it is desirable to avoid
     * collision between the authentication user namespace and the screen namespace.
     */
    protected void assignStartingUsername ()
    {
        _username = getCredentials().getUsername();
    }

    /**
     * Called by the client manager when a new connection arrives that authenticates as this
     * already established client. This must only be called from the congmr thread.
     */
    protected void resumeSession (AuthRequest req, Connection conn)
    {
        // check to see if we've already got a connection object, in which case it's probably stale
        Connection oldconn = getConnection();
        if (oldconn != null && !oldconn.isClosed()) {
            log.info("Closing stale connection [old=" + oldconn + ", new=" + conn + "].");
            // close the old connection (which results in everything being properly unregistered)
            oldconn.close();
        }

        // note our new auth request (so that we can deliver the proper bootstrap services)
        _areq = req;

        // start using the new connection
        setConnection(conn);

        // if a client connects, drops the connection and reconnects within the span of a very
        // short period of time, we'll find ourselves in resumeSession() before their client object
        // was resolved from the initial connection; in such a case, we can simply bail out here
        // and let the original session establishment code take care of initializing this resumed
        // session
        if (_clobj == null) {
            log.info("Rapid-fire reconnect caused us to arrive in resumeSession() before the " +
                     "original session resolved its client object: " + this + ".");
            return;
        }

        // we need to get onto the dobj thread so that we can finalize resumption of the session
        PresentsServer.omgr.postRunnable(new Runnable() {
            public void run () {
                // now that we're on the dobjmgr thread we can resume our session resumption
                finishResumeSession();
            }
        });
    }

    /**
     * This is called from the dobjmgr thread to complete the session resumption. We call some call
     * backs and send the bootstrap info to the client.
     */
    protected void finishResumeSession ()
    {
        // if we have no client object for whatever reason; we're hosed, shut ourselves down
        if (_clobj == null) {
            log.info("Missing client object for resuming session. Killing session.");
            endSession();
            return;
        }

        // let derived classes do any session resuming
        sessionWillResume();

        // send off a bootstrap notification immediately as we've already got our client object
        sendBootstrap();

        log.info("Session resumed " + this + ".");
    }

    /**
     * Queues up a runnable on the object manager thread where we can safely end the session.
     */
    protected void safeEndSession ()
    {
        PresentsServer.omgr.postRunnable(new Runnable() {
            public void run () {
                endSession();
            }
        });
    }

    /**
     * Creates our incoming messaeg throttle.
     */
    protected Throttle createIncomingMessageThrottle ()
    {
        // more than 100 messages in 10 seconds and you're audi like 5000
        return new Throttle(100, 10 * 1000L);
    }

    /**
     * Called when a client exceeds their allotted incoming messages per second throttle.
     */
    protected void handleThrottleExceeded ()
    {
        log.warning("Client sent more than 100 messages in 10 seconds, disconnecting " + this + ".");
        safeEndSession();
        _throttle = null;
    }

    /**
     * Makes a note that this client is no longer subscribed to this object. The subscription map
     * is used to clean up after the client when it goes away.
     */
    protected synchronized void unmapSubscrip (int oid)
    {
        ClientProxy rec;
        synchronized (_subscrips) {
            rec = _subscrips.remove(oid);
        }
        if (rec != null) {
            rec.unsubscribe();
        } else {
            log.warning("Requested to unmap non-existent subscription [oid=" + oid + "].");
        }
    }

    /**
     * Clears out the tracked client subscriptions. Called when the client goes away and shouldn't
     * be called otherwise.
     */
    protected void clearSubscrips (boolean verbose)
    {
        for (ClientProxy rec : _subscrips.values()) {
            if (verbose) {
                log.info("Clearing subscription [client=" + this +
                         ", obj=" + rec.object.getOid() + "].");
            }
            rec.unsubscribe();
        }
        _subscrips.clear();
    }

    /**
     * Called when the client session is first started. The client object has been created at this
     * point and after this method is executed, the bootstrap information will be sent to the
     * client which will trigger the start of the session. Derived classes that override this
     * method should be sure to call <code>super.sessionWillStart</code>.
     *
     * <p><em>Note:</em> This function will be called on the dobjmgr thread which means that object
     * manipulations are OK, but client instance manipulations must done carefully.
     */
    protected void sessionWillStart ()
    {
        // configure a specific access controller for the client object
        _clobj.setAccessController(PresentsObjectAccess.CLIENT);
    }

    /**
     * Called when the client resumes a session (after having disconnected and reconnected). After
     * this method is executed, the bootstrap information will be sent to the client which will
     * trigger the resumption of the session. Derived classes that override this method should be
     * sure to call <code>super.sessionWillResume</code>.
     *
     * <p><em>Note:</em> This function will be called on the dobjmgr thread which means that object
     * manipulations are OK, but client instance manipulations must done carefully.
     */
    protected void sessionWillResume ()
    {
    }

    /**
     * Called when the client session ends (either because the client logged off or because the
     * server forcibly terminated the session).  Derived classes that override this method should
     * be sure to call <code>super.sessionDidEnd</code>.
     *
     * <p><em>Note:</em> This function will be called on the dobjmgr thread which means that object
     * manipulations are OK, but client instance manipulations must done carefully.
     */
    protected void sessionDidEnd ()
    {
        // clear out our subscriptions so that we don't get a complaint about inability to forward
        // the object destroyed event we're about to generate
        clearSubscrips(false);
    }

    /**
     * Called to inform derived classes when the client has subscribed to a distributed object.
     */
    protected void subscribedToObject (DObject object)
    {
    }

    /**
     * Called to inform derived classes when the client has unsubscribed from a distributed object.
     */
    protected void unsubscribedFromObject (DObject object)
    {
    }

    /**
     * This is called once we have a handle on the client distributed object. It sends a bootstrap
     * notification to the client with all the information it will need to interact with the
     * server.
     */
    protected void sendBootstrap ()
    {
//         log.info("Sending bootstrap " + this + ".");

        // create and populate our bootstrap data
        BootstrapData data = createBootstrapData();
        populateBootstrapData(data);

        // create a send bootstrap notification
        postMessage(new BootstrapNotification(data));
    }

    /**
     * Derived client classes can override this member to create derived bootstrap data classes
     * that contain extra bootstrap information, if desired.
     */
    protected BootstrapData createBootstrapData ()
    {
        return new BootstrapData();
    }

    /**
     * Derived client classes can override this member to populate the bootstrap data with
     * additional information. They should be sure to call <code>super.populateBootstrapData</code>
     * before doing their own populating, however.
     *
     * <p><em>Note:</em> This function will be called on the dobjmgr thread which means that object
     * manipulations are OK, but client instance manipulations must be done carefully.
     */
    protected void populateBootstrapData (BootstrapData data)
    {
        // give them the client object id
        data.clientOid = _clobj.getOid();

        // fill in the list of bootstrap services
        if (_areq.getBootGroups() == null) {
            log.warning("Client provided no invocation service boot groups? " + this);
            data.services = new StreamableArrayList<InvocationMarshaller>();
        } else {
            data.services = PresentsServer.invmgr.getBootstrapServices(_areq.getBootGroups());
        }
    }

    /**
     * Called by the connection manager when this client's connection is unmapped. That may be
     * because of a connection failure (in which case this call will be followed up by a call to
     * <code>connectionFailed</code>) or it may be because of an orderly closing of the
     * connection. In either case, the client can deal with its lack of a connection in this
     * method. This is invoked by the conmgr thread and should behave accordingly.
     */
    protected void wasUnmapped ()
    {
        // clear out our connection reference
        setConnection(null);

        // clear out our subscriptions. we need to do this on the dobjmgr thread. it is important
        // that we do this *after* we clear out our connection reference. once the connection ref
        // is null, no more subscriptions will be processed (even those that were queued up before
        // the connection went away)
        PresentsServer.omgr.postRunnable(new Runnable() {
            public void run () {
                sessionConnectionClosed();
            }
        });
    }

    /**
     * Called on the dobjmgr thread when the connection associated with this session has been
     * closed and unmapped. If the user logged off before closing their connection, this will be
     * preceded by a call to {@link #sessionDidEnd}.
     */
    protected void sessionConnectionClosed ()
    {
        // clear out our dobj subscriptions in case they weren't cleared by a call to sessionDidEnd
        clearSubscrips(false);
    }

    /**
     * Called by the connection manager when this client's connection fails. This is invoked on the
     * conmgr thread and should behave accordingly.
     */
    protected void connectionFailed (IOException fault)
    {
        // nothing to do here, the client manager already complained about the failed connection
    }

    /**
     * Sets our connection reference in a thread safe way. Also establishes the back reference to
     * us as the connection's message handler.
     */
    protected synchronized void setConnection (Connection conn)
    {
        // if our connection is being cleared out, record the amount of time we were connected to
        // our total connected time
        long now = System.currentTimeMillis();
        if (_conn != null && conn == null) {
            _connectTime += ((now - _networkStamp) / 1000);
        }

        // keep a handle to the new connection
        _conn = conn;

        // tell the connection to pass messages on to us (if we're setting a connection rather than
        // clearing one out)
        if (_conn != null) {
            _conn.setMessageHandler(this);

            // configure any active custom class loader
            if (_loader != null) {
                _conn.setClassLoader(_loader);
            }
        }

        // make a note that our network status changed
        _networkStamp = now;
    }

    /**
     * The connection instance must be accessed via this member function because it is read from
     * both the dobjmgr and conmgr threads and is modified by the conmgr thread.
     *
     * @return The connection instance associated with this client or null if the client is not
     * currently connected.
     */
    protected synchronized Connection getConnection ()
    {
        return _conn;
    }

    /** Callable from non-dobjmgr thread, this queues up a runnable on the dobjmgr thread to post
     * the supplied message to this client. */
    protected final void safePostMessage (final DownstreamMessage msg)
    {
        PresentsServer.omgr.postRunnable(new Runnable() {
            public void run () {
                postMessage(msg);
            }
        });
    }

    /** Queues a message for delivery to the client. */
    protected final boolean postMessage (DownstreamMessage msg)
    {
        Connection conn = getConnection();
        if (conn != null) {
            conn.postMessage(msg);
            _messagesOut++; // count 'em up!
            return true;
        }

        // don't log dropped messages unless we're dropping a lot of them (meaning something is
        // still queueing messages up for this dead client even though it shouldn't be)
        if (++_messagesDropped % 50 == 0) {
            log.warning("Dropping many messages? [client=" + this +
                        ", count=" + _messagesDropped + "].");
        }

        // make darned sure we don't have any remaining subscriptions
        if (_subscrips.size() > 0) {
//             log.warning("Clearing stale subscriptions [client=" + this +
//                         ", subscrips=" + _subscrips.size() + "].");
            clearSubscrips(_messagesDropped > 10);
        }
        return false;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuilder buf = new StringBuilder("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * Derived classes override this to augment stringification.
     */
    protected void toString (StringBuilder buf)
    {
        buf.append("username=").append(_username);
        buf.append(", conn=").append(_conn);
        buf.append(", cloid=").append((_clobj == null) ? -1 : _clobj.getOid());
        buf.append(", in=").append(_messagesIn);
        buf.append(", out=").append(_messagesOut);
    }

    /**
     * Creates a properly initialized inner-class proxy subscriber.
     */
    protected ClientProxy createProxySubscriber ()
    {
        return new ClientProxy();
    }

    /** Used to track information about an object subscription. */
    protected class ClientProxy implements ProxySubscriber
    {
        public DObject object;

        public void unsubscribe ()
        {
            object.removeSubscriber(this);
            unsubscribedFromObject(object);
        }

        // from interface ProxySubscriber
        public void objectAvailable (DObject object)
        {
            if (postMessage(new ObjectResponse<DObject>(object))) {
                _firstEventId = PresentsServer.omgr.getNextEventId(false);
                this.object = object;
                synchronized (_subscrips) {
                    // make a note of this new subscription
                    _subscrips.put(object.getOid(), this);
                }
                subscribedToObject(object);

            } else {
                // if we failed to send the object response, unsubscribe
                object.removeSubscriber(this);
            }
        }

        // from interface ProxySubscriber
        public void requestFailed (int oid, ObjectAccessException cause)
        {
            postMessage(new FailureResponse(oid, cause.getMessage()));
        }

        // from interface ProxySubscriber
        public void eventReceived (DEvent event)
        {
            if (event instanceof PresentsDObjectMgr.AccessObjectEvent) {
                log.warning("Ignoring event that shouldn't be forwarded " + event + ".");
                Thread.dumpStack();
                return;
            }

            // if this message was posted before we received this object and has already been
            // applied, then this event has already been applied to this object and we should not
            // forward it, it is equivalent to all the events applied to the object before we
            // became a subscriber
            if (event.eventId < _firstEventId) {
                return;
            }

            postMessage(new EventNotification(event));
        }

        protected long _firstEventId;
    }

    /**
     * Message dispatchers are used to dispatch each different type of upstream message. We can
     * look the dispatcher up in a table and invoke it through an overloaded member which is faster
     * (so we think) than doing a bunch of instanceofs.
     */
    protected static interface MessageDispatcher
    {
        /**
         * Dispatch the supplied message for the specified client.
         */
        public void dispatch (PresentsClient client, UpstreamMessage mge);
    }

    /**
     * Processes subscribe requests.
     */
    protected static class SubscribeDispatcher implements MessageDispatcher
    {
        public void dispatch (PresentsClient client, UpstreamMessage msg)
        {
            SubscribeRequest req = (SubscribeRequest)msg;
//             log.info("Subscribing [client=" + client + ", oid=" + req.getOid() + "].");

            // forward the subscribe request to the omgr for processing
            PresentsServer.omgr.subscribeToObject(req.getOid(), client.createProxySubscriber());
        }
    }

    /**
     * Processes unsubscribe requests.
     */
    protected static class UnsubscribeDispatcher implements MessageDispatcher
    {
        public void dispatch (PresentsClient client, UpstreamMessage msg)
        {
            UnsubscribeRequest req = (UnsubscribeRequest)msg;
            int oid = req.getOid();
//             log.info("Unsubscribing " + client + " [oid=" + oid + "].");

            // unsubscribe from the object and clear out our proxy
            client.unmapSubscrip(oid);

            // post a response to the client letting them know that we will no longer send them
            // events regarding this object
            client.safePostMessage(new UnsubscribeResponse(oid));
        }
    }

    /**
     * Processes forward event requests.
     */
    protected static class ForwardEventDispatcher implements MessageDispatcher
    {
        public void dispatch (PresentsClient client, UpstreamMessage msg)
        {
            ForwardEventRequest req = (ForwardEventRequest)msg;
            DEvent fevt = req.getEvent();

            // freak not out if a message arrives from a client just after their session ended
            ClientObject clobj = client.getClientObject();
            if (clobj == null) {
                log.info("Dropping event that arrived after client disconnected " + fevt + ".");
                return;
            }

            // fill in the proper source oid
            fevt.setSourceOid(clobj.getOid());

//             log.info("Forwarding event [client=" + client + ", event=" + fevt + "].");

            // forward the event to the omgr for processing
            PresentsServer.omgr.postEvent(fevt);
        }
    }

    /**
     * Processes ping requests.
     */
    protected static class PingDispatcher implements MessageDispatcher
    {
        public void dispatch (PresentsClient client, UpstreamMessage msg)
        {
            // send a pong response
            PingRequest req = (PingRequest)msg;
            client.safePostMessage(new PongResponse(req.getUnpackStamp()));
        }
    }

    /**
     * Processes logoff requests.
     */
    protected static class LogoffDispatcher implements MessageDispatcher
    {
        public void dispatch (final PresentsClient client, UpstreamMessage msg)
        {
            log.fine("Client requested logoff " + client + ".");
            client.safeEndSession();
        }
    }

    protected ClientManager _cmgr;
    protected AuthRequest _areq;
    protected Object _authdata;
    protected Name _username;
    protected Connection _conn;
    protected ClientObject _clobj;
    protected HashIntMap<ClientProxy> _subscrips = new HashIntMap<ClientProxy>();
    protected ClassLoader _loader;

    /** The time at which this client started their session. */
    protected long _sessionStamp;

    /** The time at which this client most recently connected or disconnected. */
    protected long _networkStamp;

    /** The total number of seconds for which the user was connected to the server in this
     * session. */
    protected int _connectTime;

    /** Prevent the client from sending too many messages too frequently. */
    protected Throttle _throttle = createIncomingMessageThrottle();

    // keep these for kicks and giggles
    protected int _messagesIn;
    protected int _messagesOut;
    protected int _messagesDropped;

    /** A mapping of message dispatchers. */
    protected static HashMap<Class<?>,MessageDispatcher> _disps =
        new HashMap<Class<?>,MessageDispatcher>();

    /** The amount of time after disconnection a user is allowed before their session is forcibly
     * ended. */
    protected static final long FLUSH_TIME = 7 * 60 * 1000L;

    // register our message dispatchers
    static {
        _disps.put(SubscribeRequest.class, new SubscribeDispatcher());
        _disps.put(UnsubscribeRequest.class, new UnsubscribeDispatcher());
        _disps.put(ForwardEventRequest.class, new ForwardEventDispatcher());
        _disps.put(PingRequest.class, new PingDispatcher());
        _disps.put(LogoffRequest.class, new LogoffDispatcher());
    }
}
