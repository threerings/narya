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

package com.threerings.presents.server;

import java.net.InetAddress;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import java.io.IOException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;
import com.samskivert.util.ResultListener;
import com.samskivert.util.Throttle;

import com.threerings.util.Name;

import com.threerings.presents.annotation.AnyThread;
import com.threerings.presents.annotation.EventThread;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.dobj.ProxySubscriber;
import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.net.BootstrapNotification;
import com.threerings.presents.net.CompoundDownstreamMessage;
import com.threerings.presents.net.CompoundUpstreamMessage;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.EventNotification;
import com.threerings.presents.net.FailureResponse;
import com.threerings.presents.net.ForwardEventRequest;
import com.threerings.presents.net.LogoffRequest;
import com.threerings.presents.net.Message;
import com.threerings.presents.net.ObjectResponse;
import com.threerings.presents.net.PingRequest;
import com.threerings.presents.net.PongResponse;
import com.threerings.presents.net.SubscribeRequest;
import com.threerings.presents.net.ThrottleUpdatedMessage;
import com.threerings.presents.net.TransmitDatagramsRequest;
import com.threerings.presents.net.UnsubscribeRequest;
import com.threerings.presents.net.UnsubscribeResponse;
import com.threerings.presents.net.UpdateThrottleMessage;
import com.threerings.presents.net.UpstreamMessage;
import com.threerings.presents.server.net.PresentsConnection;

import com.threerings.nio.conman.Connection;
import com.threerings.nio.conman.ConnectionManager;

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
public class PresentsSession
    implements PresentsConnection.MessageHandler, ClientResolutionListener
{
    /** Used by {@link PresentsSession#setUsername} to report success or failure. */
    public static interface UserChangeListener
    {
        /** Called when the new client object has been resolved and the new client object reported
         * to the client, but the old one has not yet been destroyed. Any events delivered on this
         * callback to the old client object will be delivered.
         *
         * @param rl when this method is finished with its business and the old client object can
         * be destroyed, the result listener should be called. */
        void changeReported (ClientObject newObji, ResultListener<Void> rl);

        /** Called when the user change is completed, the old client object is destroyed and all
         * updates are committed. */
        void changeCompleted (ClientObject newObj);

        /** Called if some failure occurs during the user change process. */
        void changeFailed (Exception cause);
    }

    /**
     * Returns the credentials used to authenticate this session.
     */
    public Credentials getCredentials ()
    {
        return _areq.getCredentials();
    }

    /**
     * Returns the time zone in which the client is operating.
     */
    public TimeZone getTimeZone ()
    {
        return _areq.getTimeZone();
    }

    /**
     * Returns the shared secret for this session.
     */
    public byte[] getSecret ()
    {
        return (_areq == null) ? null : _areq.getSecret();
    }

    /**
     * Returns true if this session has been disconnected for sufficiently long that it should be
     * forcibly ended.
     */
    public boolean checkExpired (long now)
    {
        return (getConnection() == null && (now - _networkStamp > getFlushTime()));
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
     * Returns the username with which this client authenticated. Note: if {@link #setUsername} has
     * been called this may differ from {@link #getClientObject}.username.
     */
    public Name getAuthName ()
    {
        return _authname;
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
     * Checks whether the client has requested that datagram transmission be enabled.
     */
    public boolean getTransmitDatagrams ()
    {
        PresentsConnection conn = getConnection();
        return conn != null && conn.getTransmitDatagrams();
    }

    /**
     * Configures this session with a custom class loader that will be used when unserializing
     * classes from the network.
     */
    public void setClassLoader (ClassLoader loader)
    {
        _loader = loader;
        PresentsConnection conn = getConnection();
        if (conn != null) {
            conn.setClassLoader(loader);
        }
    }

    /**
     * Configures the rate at which incoming messages are throttled for this client. This will
     * communicate the new limit to the client and begin enforcing the limit when the client has
     * acknowledged the new limit.
     *
     * <p><em>Note:</em> this means that a hacked client can refuse to ACK message rate reductions
     * and continue to use the most generous rate ever assigned to it.  Don't increase the throttle
     * beyond the default for untrusted clients. This mechanism exists so that trusted clients can
     * have their throttle relaxed in a robust manner which will not result in disconnects if the
     * client happens to be at or near the throttle limit when the throttle is reduced.
     */
    @EventThread
    public void setIncomingMessageThrottle (int messagesPerSec)
    {
        // store and post update immediately if connected
        _messagesPerSec = messagesPerSec;
        if (getConnection() != null) {
            sendThrottleUpdate();
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
            public void clientResolved (final Name username, final ClientObject clobj) {
                // if they old client object is gone by now, they ended their session while we were
                // switching, so freak out
                if (_clobj == null) {
                    log.warning("Client disappeared before we could complete the switch to a " +
                                "new client object", "ousername", clobj.username,
                                "nusername", username);
                    _clmgr.releaseClientObject(username);
                    Exception error = new Exception("Early withdrawal");
                    resolutionFailed(username, error);
                    return;
                }

                // call down to any derived classes
                clientObjectWillChange(_clobj, clobj);

                // let the caller know that we've got some new business
                if (ucl != null) {
                    ucl.changeReported(clobj, new ResultListener<Void>() {
                        public void requestCompleted (Void result) {
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
            protected void finishResolved (Name username, ClientObject clobj) {
                // let the client know that the rug has been yanked out from under their ass
                Object[] args = new Object[] { clobj.getOid() };
                _clobj.postMessage(ClientObject.CLOBJ_CHANGED, args);

                // release our old client object; this will destroy it
                _clmgr.releaseClientObject(_clobj.username);

                // update our internal fields
                _clobj = clobj;

                // call down to any derived classes
                clientObjectDidChange(_clobj);

                // let our listener know we're groovy
                if (ucl != null) {
                    ucl.changeCompleted(_clobj);
                }
            }

            public void resolutionFailed (Name username, Exception reason) {
                Name oldName = (_clobj == null) ? null : _clobj.username;
                log.warning("Unable to resolve new client object",
                    "oldname", oldName, "newname", username, "reason", reason, reason);

                // let our listener know we're hosed
                if (ucl != null) {
                    ucl.changeFailed(reason);
                }
            }
        };

        // resolve the new client object
        _clmgr.resolveClientObject(username, clr);
    }

    /**
     * <em>Double Danger:</em> this method is not for general consumption. Like
     * {@code #setUsername}, this changes the username of the client, but unlike setUsername, it
     * does it in the existing client object. Care must be taken to ensure that any client or
     * server code either doesn't map things based on username before this call, or that it's
     * updated to reflect the change.
     *
     * @return - true if the client was successfully renamed, false otherwise
     */
    public boolean updateUsername (Name username)
    {
        return _clmgr.renameClientObject(_clobj.username, username);
    }

    /**
     * returns the client object that is associated with this client.
     */
    public ClientObject getClientObject ()
    {
        return _clobj;
    }

    /**
     * Forcibly terminates a client's session. This must be called from the dobjmgr thread.
     */
    @EventThread
    public void endSession ()
    {
        _clmgr.clientSessionWillEnd(this);

        // queue up a request for our connection to be closed (if we have a connection, that is)
        Connection conn = getConnection();
        if (conn != null) {
            // go ahead and clear out our connection now to prevent funniness
            setConnection(null);
            // have the connection manager close our connection when it is next convenient
            _conmgr.closeConnection(conn);
        }

        // if we don't have a client object, we failed to resolve in the first place, in which case
        // we have to cope as best we can
        if (_clobj != null) {
            // and clean up after ourselves
            try {
                sessionDidEnd();
            } catch (Exception e) {
                log.warning("Choked in sessionDidEnd " + this + ".", e);
            }

            // release (and destroy) our client object
            _clmgr.releaseClientObject(_clobj.username);

            // we only report that our session started if we managed to resolve our client object,
            // so we only report that it ended in the same circumstance
            _clmgr.clientSessionDidEnd(this);
        }

        // we always want to clear ourselves out of the client manager
        _clmgr.clearSession(this);

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
        // we'll be keeping this bad boy
        _clobj = clobj;

        // if our connection was closed while we were resolving our client object, then just
        // abandon ship
        if (getConnection() == null) {
            log.info("Session ended before client object could be resolved " + this + ".");
            endSession();
            return;
        }

        // Dump our secret into the client local for easy access
        clobj.getLocal(ClientLocal.class).secret = getSecret();

        // finish up our regular business
        sessionWillStart();
        sendBootstrap();

        // let the client manager know that we're operational
        _clmgr.clientSessionDidStart(this);
    }

    // from interface ClientResolutionListener
    public void resolutionFailed (Name username, Exception reason)
    {
        if (reason instanceof ClientResolver.ClientDisconnectedException) {
            log.info("Client disconnected during resolution", "who", who());

        } else {
            // urk; nothing to do but complain and get the f**k out of dodge
            log.warning("Unable to resolve client", "who", who(), reason);
        }

        // end the session now to prevent danglage
        endSession();
    }

    // from interface Connection.MessageHandler
    public void handleMessage (Message message)
    {
        // if the client has been getting crazy with the cheeze whiz, stick a fork in them; the
        // first time through we end our session, subsequently _throttle is null and we just drop
        // any messages that come in until we've fully shutdown
        if (_throttle == null) {
//             log.info("Dropping message from force-quit client", "conn", getConnection(),
//                      "msg", message);
            return;

        } else if (_throttle.throttleOp(message.received)) {
            handleThrottleExceeded();
        }

        dispatchMessage(message);
    }

    /**
     * Processes a message without throttling.
     */
    protected void dispatchMessage (Message message)
    {
        _messagesIn++; // count 'em up!

        // we dispatch to a message dispatcher that is specialized for the particular class of
        // message that we received
        MessageDispatcher disp = _disps.get(message.getClass());
        if (disp == null) {
            log.warning("No dispatcher for message", "msg", message);
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
    protected void clientObjectWillChange (ClientObject oldClobj, ClientObject newClobj)
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
     * Initializes this client instance with the specified username, connection instance and
     * client object and begins a client session.
     */
    protected void startSession (Name authname, AuthRequest req, PresentsConnection conn,
        Object authdata)
    {
        _authname = authname;
        _areq = req;
        _authdata = authdata;
        setConnection(conn);

        // resolve our client object before we get fully underway
        _clmgr.resolveClientObject(_authname, this);

        // make a note of our session start time
        _sessionStamp = System.currentTimeMillis();
    }

    /**
     * Called by the client manager when a new connection arrives that authenticates as this
     * already established client. This must only be called from the congmr thread.
     */
    protected void resumeSession (AuthRequest req, PresentsConnection conn)
    {
        // check to see if we've already got a connection object, in which case it's probably stale
        Connection oldconn = getConnection();
        if (oldconn != null && !oldconn.isClosed()) {
            log.info("Closing stale connection", "old", oldconn, "new", conn);
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
                     "original session resolved its client object " + this + ".");
            return;
        }

        // we need to get onto the dobj thread so that we can finalize resumption of the session
        _omgr.postRunnable(new Runnable() {
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
    @EventThread
    protected void finishResumeSession ()
    {
        // if we have no client object for whatever reason; we're hosed, shut ourselves down
        if (_clobj == null) {
            log.info("Missing client object for resuming session " + this + ".");
            endSession();
            return;
        }

        // make extra sure we have no lingering subscriptions
        clearSubscrips(false);

        // Update our client secret with the new auth request
        _clobj.getLocal(ClientLocal.class).secret = getSecret();

        // let derived classes do any session resuming
        sessionWillResume();

        // send off a bootstrap notification immediately as we've already got our client object
        sendBootstrap();

        // resend the throttle update if non-default
        if (_messagesPerSec != Client.DEFAULT_MSGS_PER_SECOND) {
            sendThrottleUpdate();
        }

        log.info("Session resumed " + this + ".");
    }

    /**
     * Sends the throttle update to the client.
     */
    protected void sendThrottleUpdate ()
    {
        synchronized(_pendingThrottles) {
            _pendingThrottles.add(_messagesPerSec);
        }
        postMessage(new UpdateThrottleMessage(_messagesPerSec), null);
        // when we get a ThrottleUpdatedMessage from the client, we'll apply the new throttle
    }

    /**
     * Queues up a runnable on the object manager thread where we can safely end the session.
     */
    @AnyThread
    protected void safeEndSession ()
    {
        if (!_omgr.isRunning()) {
            log.info("Dropping end session request as we're shutting down " + this + ".");
        } else {
            _omgr.postRunnable(new Runnable() {
                public void run () {
                    endSession();
                }
            });
        }
    }

    /**
     * Creates our incoming message throttle. Use {@link #setIncomingMessageThrottle} to adjust the
     * throttle for running clients.
     */
    protected Throttle createIncomingMessageThrottle ()
    {
        // see throttleUpdated() for details on these numbers
        return new Throttle(10*(Client.DEFAULT_MSGS_PER_SECOND+1), 10*1000L);
    }

    /**
     * Called when a client exceeds their allotted incoming messages per second throttle.
     */
    protected void handleThrottleExceeded ()
    {
        log.warning("Client exceeded incoming message throttle, disconnecting",
                    "client", this, "throttle", _throttle);
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
            boolean alreadyDestroyed = _destroyedSubs.remove(oid);
            if (!alreadyDestroyed) {
                log.warning("Missing subscription in unmap", "client", this, "oid", oid);
            }
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
                log.info("Clearing subscription", "client", this, "obj", rec.object.getOid());
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
    @EventThread
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
    @EventThread
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
    @EventThread
    protected void sessionDidEnd ()
    {
        // clear out our subscriptions so that we don't get a complaint about inability to forward
        // the object destroyed event we're about to generate
        clearSubscrips(false);
    }

    /**
     * Called to inform derived classes when the client has subscribed to a distributed object.
     */
    @EventThread
    protected void subscribedToObject (DObject object)
    {
    }

    /**
     * Called to inform derived classes when the client has unsubscribed from a distributed object.
     */
    @EventThread
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
        postMessage(new BootstrapNotification(data), null);
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
        // give them the connection id
        Connection conn = getConnection();
        if (conn != null) {
            data.connectionId = conn.getConnectionId();
        } else {
            log.warning("Connection disappeared before we could send bootstrap response.",
                        "client", this);
            return; // stop here as we're just going to throw away this bootstrap
        }

        // and the client object id
        data.clientOid = _clobj.getOid();

        // fill in the list of bootstrap services
        if (_areq.getBootGroups() == null) {
            log.warning("Client provided no invocation service boot groups? " + this);
            data.services = Lists.newArrayList();
        } else {
            data.services = _invmgr.getBootstrapServices(_areq.getBootGroups());
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

        // reset the throttle state in case the client reconnects
        _throttle = createIncomingMessageThrottle();
        synchronized (_pendingThrottles) {
            _pendingThrottles.clear();
        }

        // if we are being closed after the omgr has shutdown, then just stop here; the whole world
        // is about to come to a screeching halt anyway
        if (_omgr.isRunning()) {
            // clear out our subscriptions: we need to do this on the dobjmgr thread. it is
            // important that we do this *after* we clear out our connection reference. once the
            // connection ref is null, no more subscriptions will be processed (even those that
            // were queued up before the connection went away)
            _omgr.postRunnable(new Runnable() {
                public void run () {
                    sessionConnectionClosed();
                }
            });
        }
    }

    /**
     * Called on the dobjmgr thread when the connection associated with this session has been
     * closed and unmapped. If the user logged off before closing their connection, this will be
     * preceded by a call to {@link #sessionDidEnd}.
     */
    @EventThread
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
    protected synchronized void setConnection (PresentsConnection conn)
    {
        long now = System.currentTimeMillis();
        if (_conn != null) {
            // if our connection is being cleared out, record the amount of time we were connected
            // to our total connected time
            if (conn == null) {
                _connectTime += ((now - _networkStamp) / 1000);
                _messagesDropped = 0;
            }

            // make damn sure we don't get any more messages from the old connection
            _conn.clearMessageHandler();
        }

        // keep a handle to the new connection
        _conn = conn;

        // if we're setting a connection rather than clearing one out...
        if (_conn != null) {
            // tell the connection to pass messages on to us
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
    protected synchronized PresentsConnection getConnection ()
    {
        return _conn;
    }

    /**
     * Callable from non-dobjmgr thread, this queues up a runnable on the dobjmgr thread to post
     * the supplied message to this client.
     */
    protected final void safePostMessage (DownstreamMessage msg)
    {
        safePostMessage(msg, null);
    }

    /**
     * Callable from non-dobjmgr thread, this queues up a runnable on the dobjmgr thread to post
     * the supplied message to this client.
     */
    protected final void safePostMessage (
        final DownstreamMessage msg, final PresentsConnection expect)
    {
        _omgr.postRunnable(new Runnable() {
            public void run () {
                postMessage(msg, expect);
            }
        });
    }

    /**
     * Collects downstream messages in a compound message until finishCompoundMessage is called.
     */
    protected void startCompoundMessage ()
    {
        if (_compound == null) {
            _compound = new CompoundDownstreamMessage();
        }
        _compoundDepth++;
    }

    /**
     * Sends the compound message created in startCompoundMessage.
     */
    protected void finishCompoundMessage ()
    {
        if (--_compoundDepth == 0) {
            CompoundDownstreamMessage downstream = _compound;
            _compound = null;
            if (!downstream.msgs.isEmpty()) {
                postMessage(downstream, null);
            }
        }
    }

    /** Queues a message for delivery to the client. */
    protected boolean postMessage (DownstreamMessage msg, PresentsConnection expect)
    {
        PresentsConnection conn = getConnection();

        // make sure that the connection they expect us to be using is the one we're using; there
        // are circumstances were sufficient delay between request and response gives the client
        // time to drop their original connection and establish a new one, opening the door to
        // major confusion
        if (expect != null && conn != expect) {
            return false;
        }

        if (_compound != null) {
            _compound.msgs.add(msg);
            return true;
        }

        // make sure we have a connection at all
        if (conn != null) {
            conn.postMessage(msg);
            _messagesOut++; // count 'em up!
            return true;
        }

        // don't log dropped messages unless we're dropping a lot of them (meaning something is
        // still queueing messages up for this dead client even though it shouldn't be)
        if (++_messagesDropped % 50 == 0) {
            log.warning("Dropping many messages?", "client", this,
                "count", _messagesDropped, "msg", msg);
        }

        // make darned sure we don't have any remaining subscriptions
        if (_subscrips.size() > 0) {
//             log.warning("Clearing stale subscriptions", "client", this,
//                         "subscrips", _subscrips.size());
            clearSubscrips(_messagesDropped > 10);
        }
        return false;
    }

    /**
     * Notifies this client that its throttle was updated.
     */
    protected void throttleUpdated ()
    {
        int messagesPerSec;
        synchronized(_pendingThrottles) {
            if (_pendingThrottles.size() == 0) {
                log.warning("Received throttleUpdated but have no pending throttles",
                            "client", this);
                return;
            }
            messagesPerSec = _pendingThrottles.remove(0);
        }

        // log.info("Applying updated throttle", "client", this, "msgsPerSec", messagesPerSec);
        // We set our hard throttle over a 10 second period instead of a 1 second period to
        // account for periods of network congestion that might cause otherwise properly
        // throttled messages to bunch up while they're "on the wire"; we also add a one
        // message buffer so that if the client is right up against the limit, we don't end
        // up quibbling over a couple of milliseconds
        _throttle.reinit(10*messagesPerSec+1, 10*1000L);
    }

    @Override
    public String toString ()
    {
        StringBuilder buf = new StringBuilder("[");
        toString(buf);
        return buf.append("]").toString();
    }

    protected String who ()
    {
        return (_authname == null) ? "null" :
            (_authname.getClass().getSimpleName() + "(" + _authname + ")");
    }

    /**
     * Returns the duration (in milliseconds) after which a disconnected session will be terminated
     * and flushed.
     */
    protected long getFlushTime ()
    {
        return DEFAULT_FLUSH_TIME;
    }

    /**
     * Derived classes override this to augment stringification.
     */
    protected void toString (StringBuilder buf)
    {
        buf.append("who=").append(who());
        buf.append(", conn=").append(getConnection());
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
        public void objectAvailable (DObject dobj)
        {
            if (postMessage(new ObjectResponse<DObject>(dobj), _oconn)) {
                _firstEventId = _omgr.getNextEventId(false);
                object = dobj;
                ClientProxy orec;
                synchronized (_subscrips) {
                    // make a note of this new subscription
                    orec = _subscrips.put(dobj.getOid(), this);
                }
                if (orec != null) {
                    log.warning("Replacing existing subscription.", "oid", dobj.getOid(),
                        "client", PresentsSession.this);
                    orec.unsubscribe();
                }
                subscribedToObject(dobj);

            } else {
                // if we failed to send the object response, unsubscribe
                dobj.removeSubscriber(this);
            }
        }

        // from interface ProxySubscriber
        public void requestFailed (int oid, ObjectAccessException cause)
        {
            postMessage(new FailureResponse(oid, cause.getMessage()), _oconn);
        }

        // from interface ProxySubscriber
        public void eventReceived (DEvent event)
        {
            if (event instanceof PresentsDObjectMgr.AccessObjectEvent<?>) {
                log.warning("Ignoring event that shouldn't be forwarded " + event + ".",
                            new Exception());
                return;
            }

            // if this message was posted before we received this object and has already been
            // applied, then this event has already been applied to this object and we should not
            // forward it, it is equivalent to all the events applied to the object before we
            // became a subscriber
            if (event.eventId < _firstEventId) {
                return;
            }

            postMessage(new EventNotification(event), _oconn);

            if (event instanceof ObjectDestroyedEvent) {
                // Make sure it's cleared out.  Otherwise, client-server timing can
                // be such that a client stays subscribed to a no-longer-managed dobj.
                // NOTE: We keep the oid itself on the destroyedSubs list to validate against since
                // we may get a late unsubscribe after the destruction event has gone through. Thus
                // there is still potentially a memory leak until logoff of the oid, but the dobj
                // itself can be collected.
                ClientProxy sub;
                int oid = object.getOid();
                synchronized(_subscrips) {
                    sub = _subscrips.remove(oid);
                    if (sub != null) {
                        _destroyedSubs.add(oid);
                    }
                }

                if (sub == ClientProxy.this) {
                    unsubscribe();
                }
            }
        }

        // from interface ProxySubscriber
        public ClientObject getClientObject ()
        {
            return PresentsSession.this.getClientObject();
        }

        protected long _firstEventId;
        // the connection that was active at the time we were constructed
        protected PresentsConnection _oconn = getConnection();
    }

    /**
     * Message dispatchers are used to dispatch each different type of message. We can look the
     * dispatcher up in a table and invoke it through an overloaded member which is faster (so we
     * think) than doing a bunch of instanceofs.
     */
    protected static interface MessageDispatcher
    {
        /**
         * Dispatch the supplied message for the specified client.
         */
        void dispatch (PresentsSession client, Message mge);
    }

    /**
     * Processes subscribe requests.
     */
    protected static class SubscribeDispatcher implements MessageDispatcher
    {
        public void dispatch (PresentsSession client, Message msg)
        {
            SubscribeRequest req = (SubscribeRequest)msg;
//             log.info("Subscribing", "client", client, "oid", req.getOid());

            // forward the subscribe request to the omgr for processing
            client._omgr.subscribeToObject(req.getOid(), client.createProxySubscriber());
        }
    }

    /**
     * Processes compound messages.
     */
    protected static class CompoundDispatcher implements MessageDispatcher
    {
        public void dispatch (final PresentsSession client, Message msg)
        {
            // Compound downstream messages sent while dispatching the upstream compound message
            client._omgr.postRunnable(new Runnable() {
                public void run () {
                    client.startCompoundMessage();
                }});
            for (UpstreamMessage submsg : ((CompoundUpstreamMessage)msg).msgs) {
                client.dispatchMessage(submsg);
            }
            // Send any messages produced en masse now that we've finished dispatching
            client._omgr.postRunnable(new Runnable() {
                public void run () {
                    client.finishCompoundMessage();
                }});
        }
    }

    /**
     * Processes unsubscribe requests.
     */
    protected static class UnsubscribeDispatcher implements MessageDispatcher
    {
        public void dispatch (PresentsSession client, Message msg)
        {
            UnsubscribeRequest req = (UnsubscribeRequest)msg;
            int oid = req.getOid();
//             log.info("Unsubscribing " + client + "", "oid", oid);

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
        public void dispatch (PresentsSession client, Message msg)
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

//             log.info("Forwarding event", "client", client, "event", fevt);

            // forward the event to the omgr for processing
            client._omgr.postEvent(fevt);
        }
    }

    /**
     * Processes ping requests.
     */
    protected static class PingDispatcher implements MessageDispatcher
    {
        public void dispatch (PresentsSession client, Message msg)
        {
            // send a pong response using the transport with which the message was received
            PingRequest req = (PingRequest)msg;
            client.safePostMessage(new PongResponse(req.getUnpackStamp(), req.getTransport()),
                client.getConnection());
        }
    }

    /**
     * Processes datagram transmission requests.
     */
    protected static class TransmitDatagramsDispatcher implements MessageDispatcher
    {
        public void dispatch (PresentsSession client, Message msg)
        {
            log.debug("Client requested datagram transmission", "client", client);
            client.getConnection().setTransmitDatagrams(true);
        }
    }

    /**
     * Processes throttle updated messages.
     */
    protected static class ThrottleUpdatedDispatcher implements MessageDispatcher
    {
        public void dispatch (final PresentsSession client, Message msg)
        {
            log.debug("Client ACKed throttle update", "client", client);
            client.throttleUpdated();
        }
    }

    /**
     * Processes logoff requests.
     */
    protected static class LogoffDispatcher implements MessageDispatcher
    {
        public void dispatch (final PresentsSession client, Message msg)
        {
            log.debug("Client requested logoff", "client", client);
            client.safeEndSession();
        }
    }

    @Inject protected ClientManager _clmgr;
    @Inject protected ConnectionManager _conmgr;
    @Inject protected PresentsDObjectMgr _omgr;
    @Inject protected InvocationManager _invmgr;

    protected AuthRequest _areq;
    protected Object _authdata;
    protected Name _authname;
    protected PresentsConnection _conn;
    protected ClientObject _clobj;
    protected IntMap<ClientProxy> _subscrips = IntMaps.newHashIntMap();

    /**
     * Message in which we're currently compounding messages to send, or null if we're sending them
     * straight on.
     */
    protected CompoundDownstreamMessage _compound;

    /**
     * The count of startCompoundMessage calls that have occurred without a finishCompoundMessage.
     * _compound won't be set until this reaches 0 again.
     */
    protected int _compoundDepth;

    /** The Oids of objects that have been destroyed while we were subscribed. */
    protected HashSet<Integer> _destroyedSubs = Sets.newHashSet();
    protected ClassLoader _loader;

    /** The time at which this client started their session. */
    protected long _sessionStamp;

    /** The time at which this client most recently connected or disconnected. */
    protected long _networkStamp;

    /** The total number of seconds for which the user was connected to the server in this
     * session. */
    protected int _connectTime;

    /** The configured throttle setting (resent to reconnecting clients). */
    protected int _messagesPerSec = Client.DEFAULT_MSGS_PER_SECOND;

    /** Prevent the client from sending too many messages too frequently. */
    protected Throttle _throttle = createIncomingMessageThrottle();

    /** Used to keep throttles around until we know the client is ready for us to apply them. */
    protected List<Integer> _pendingThrottles = Lists.newArrayList();

    // keep these for kicks and giggles
    protected int _messagesIn;
    protected int _messagesOut;
    protected int _messagesDropped;

    /** A mapping of message dispatchers. */
    protected static Map<Class<?>, MessageDispatcher> _disps = Maps.newHashMap();

    /** Default period a user is allowed after disconn before their session is forcibly ended. */
    protected static final long DEFAULT_FLUSH_TIME = 7 * 60 * 1000L;

    // register our message dispatchers
    static {
        _disps.put(SubscribeRequest.class, new SubscribeDispatcher());
        _disps.put(UnsubscribeRequest.class, new UnsubscribeDispatcher());
        _disps.put(ForwardEventRequest.class, new ForwardEventDispatcher());
        _disps.put(PingRequest.class, new PingDispatcher());
        _disps.put(TransmitDatagramsRequest.class, new TransmitDatagramsDispatcher());
        _disps.put(ThrottleUpdatedMessage.class, new ThrottleUpdatedDispatcher());
        _disps.put(LogoffRequest.class, new LogoffDispatcher());
        _disps.put(CompoundUpstreamMessage.class, new CompoundDispatcher());
    }
}
