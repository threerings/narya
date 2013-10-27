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

package com.threerings.presents.client;

import java.security.PublicKey;
import java.util.HashSet;

import com.google.common.collect.Sets;

import com.samskivert.util.Interval;
import com.samskivert.util.ObserverList;
import com.samskivert.util.RunAnywhere;
import com.samskivert.util.RunQueue;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Throttle;

import com.threerings.presents.data.AuthCodes;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.PingRequest;
import com.threerings.presents.net.PongResponse;
import com.threerings.presents.net.ThrottleUpdatedMessage;
import com.threerings.presents.util.SecureUtil;

import static com.threerings.presents.Log.log;

/**
 * Through the client object, a connection to the system is established and maintained. The client
 * object maintains two separate threads (a reader and a writer) by which all network traffic is
 * managed.
 */
public class Client
{
    /** The default ports on which the server listens for client connections. */
    public static final int[] DEFAULT_SERVER_PORTS = { 47624 };

    /** The default ports on which the server listens for datagrams. */
    public static final int[] DEFAULT_DATAGRAM_PORTS = { };

    /** The maximum size of a datagram. */
    public static final int MAX_DATAGRAM_SIZE = 1450;

    /** Our default maximum outgoing message rate in messages per second. */
    public static final int DEFAULT_MSGS_PER_SECOND = 10;

    /**
     * Constructs a client object with the supplied credentials and RunQueue. The creds will be
     * used to authenticate with any server to which this client attempts to connect. The RunQueue
     * is used to operate the distributed object event dispatch mechanism. To allow the dobj event
     * dispatch to coexist with threads like the AWT thread, the client will request that the
     * RunQueue queue up a runnable whenever there are distributed object events that need to be
     * processed. The RunQueue can then queue that runnable up on the AWT thread if it is so
     * inclined to make life simpler for the rest of the application.
     *
     * @param creds the credentials to use when logging on to the server.  These can be null, but
     * <code>setCredentials</code> must then be called before any call to <code>logon</code>.
     * @param runQueue a RunQueue that can be used to process incoming events.
     */
    public Client (Credentials creds, RunQueue runQueue)
    {
        _creds = creds;
        _runQueue = runQueue;
    }

    /**
     * Registers the supplied observer with this client. While registered the observer will receive
     * notifications of state changes within the client. The function will refuse to register an
     * already registered observer.
     *
     * @see ClientObserver
     * @see SessionObserver
     */
    public void addClientObserver (SessionObserver observer)
    {
        synchronized (_observers) {
            _observers.add(observer);
        }
    }

    /**
     * Unregisters the supplied observer. Upon return of this function, the observer will no longer
     * receive notifications of state changes within the client.
     */
    public void removeClientObserver (SessionObserver observer)
    {
        synchronized (_observers) {
            _observers.remove(observer);
        }
    }

    /**
     * Checks whether or not this client is operating in a standalone mode.
     */
    public boolean isStandalone ()
    {
        return _standalone;
    }

    /**
     * Configures the client to communicate with the server on the supplied hostname and set of
     * ports (which will be tried in succession).
     *
     * @see #logon
     * @see #moveToServer
     */
    public void setServer (String hostname, int[] ports)
    {
        setServer(hostname, ports, new int[0]);
    }

    /**
     * Configures the client to communicate with the server on the supplied hostname, set of
     * ports (which will be tried in succession), and datagram ports.
     *
     * @see #logon
     * @see #moveToServer
     */
    public void setServer (String hostname, int[] ports, int[] datagramPorts)
    {
        _hostname = hostname;
        _ports = ports;
        _datagramPorts = datagramPorts;
    }

    /**
     * Returns the RunQueue in use by this client. This can be used to queue up event dispatching
     * stints.
     */
    public RunQueue getRunQueue ()
    {
        return _runQueue;
    }

    /**
     * Returns the hostname of the server to which this client is currently configured to connect.
     */
    public String getHostname ()
    {
        return _hostname;
    }

    /**
     * Returns the port on which this client is currently configured to connect to the server.
     */
    public int[] getPorts ()
    {
        return _ports;
    }

    /**
     * Returns the ports on the server to which the client can send datagrams.  Returns an empty
     * array if datagrams are not supported.
     */
    public int[] getDatagramPorts ()
    {
        return _datagramPorts;
    }

    /**
     * Returns the credentials with which this client is currently configured to connect to the
     * server.
     */
    public Credentials getCredentials ()
    {
        return _creds;
    }

    /**
     * Sets the credentials that will be used by this client to authenticate with the server. This
     * should be done before any call to <code>logon</code>.
     */
    public void setCredentials (Credentials creds)
    {
        _creds = creds;
    }

    /**
     * Returns the public key with which this client is currently configured to create a secure
     * authentication channel to the server.
     */
    public PublicKey getPublicKey ()
    {
        return _publicKey;
    }

    /**
     * Sets the public key that will be used by this client to create a secure authentication
     * channel with the server if the ciphers are supported.  This should be done before any call
     * to <code>logon</code>.
     *
     * @return true if the key is set
     */
    public boolean setPublicKey (PublicKey key)
    {
        if (SecureUtil.ciphersSupported(key)) {
            _publicKey = key;
            return true;
        }
        return false;
    }

    /**
     * Sets the public key that will be used by this client to create a secure authentication
     * channel with the server if the ciphers are supported.  This should be done before any call
     * to <code>logon</code>.
     *
     * @return true if the key is set
     */
    public boolean setPublicKey (String key)
    {
        return key == null ? false : setPublicKey(SecureUtil.stringToRSAPublicKey(key));
    }

    /**
     * Sets if we require a secure authentication.
     */
    public void setRequireSecureAuth (boolean requireSecureAuth)
    {
        _requireSecureAuth = requireSecureAuth;
    }

    /**
     * Returns true if we require secure authentication.
     */
    public boolean requireSecureAuth ()
    {
        return _requireSecureAuth;
    }

    /**
     * Sets the secret key to use with a session.
     */
    public void setSecret (byte[] secret)
    {
        _secret = secret;
    }

    /**
     * Gets the secret key to use with a session.
     */
    public byte[] getSecret ()
    {
        return _secret;
    }

    /**
     * Returns the version string configured for this client.
     */
    public String getVersion ()
    {
        return _version;
    }

    /**
     * Sets the version string reported to the server during authentication. Some server
     * implementations may wish to refuse connections by old or invalid client versions.
     */
    public void setVersion (String version)
    {
        _version = version;
    }

    /**
     * Configures the client with a custom class loader which will be used when reading objects off
     * of the network.
     */
    public void setClassLoader (ClassLoader loader)
    {
        _loader = loader;
        if (_comm != null) {
            _comm.setClassLoader(loader);
        }
    }

    /**
     * Installs (or clears) a message tracker that will be notified on message transmission and
     * receipt for the purpose of statistics tracking.
     *
     * @param tracker the new tracker to install, or <code>null</code> to clear the tracker.
     */
    public void setMessageTracker (MessageTracker tracker)
    {
        _messageTracker = (tracker == null) ? MessageTracker.NOOP : tracker;
    }

    /**
     * Returns the data associated with our authentication response. Users of the Presents system
     * may wish to communicate authentication related information to their client by extending and
     * augmenting {@link AuthResponseData}.
     */
    public AuthResponseData getAuthResponseData ()
    {
        return _authData;
    }

    /**
     * Returns the distributed object manager associated with this session. This reference is only
     * valid for the duration of the session and a new reference must be obtained if the client
     * disconnects and reconnects to the server.
     *
     * @return the dobjmgr in effect or null if we have no established connection to the server.
     */
    public DObjectManager getDObjectManager ()
    {
        return _omgr;
    }

    /**
     * Instructs the distributed object manager associated with this client to allow objects of the
     * specified class to linger around the specified number of milliseconds after their last
     * subscriber has been removed before the client finally removes its object proxy and flushes
     * the object. Normally, objects are flushed immediately following the removal of their last
     * subscriber.
     *
     * <p><em>Note:</em> the delay will be applied to derived classes as well as exact
     * matches. <em>Note also:</em> this method cannot be called until after the client has
     * established a connection with the server and the distributed object manager is available.
     */
    public void registerFlushDelay (Class<?> objclass, long delay)
    {
        ClientDObjectMgr omgr = (ClientDObjectMgr)getDObjectManager();
        omgr.registerFlushDelay(objclass, delay);
    }

    /**
     * Returns the unique id of the client's connection to the server.  It is only valid for the
     * duration of the session.
     */
    public int getConnectionId ()
    {
        return _connectionId;
    }

    /**
     * Returns the oid of the client object associated with this session.  It is only valid for the
     * duration of the session.
     */
    public int getClientOid ()
    {
        return _cloid;
    }

    /**
     * Returns a reference to the client object associated with this session. It is only valid for
     * the duration of the session.
     */
    public ClientObject getClientObject ()
    {
        return _clobj;
    }

    /**
     * Returns the invocation director associated with this session. This reference is only valid
     * for the duration of the session.
     */
    public InvocationDirector getInvocationDirector ()
    {
        return _invdir;
    }

    /**
     * Marks this client as interested in the specified bootstrap services group. Any services
     * registered as bootstrap services with the supplied group name will be included in this
     * clients bootstrap services set. This must be called before {@link #logon}.
     */
    public void addServiceGroup (String group)
    {
        if (isLoggedOn()) {
            throw new IllegalStateException("Service groups must be registered prior to logon.");
        }
        _bootGroups.add(group);
    }

    /**
     * Returns the set of bootstrap service groups needed by this client.
     */
    public String[] getBootGroups ()
    {
        return _bootGroups.toArray(new String[_bootGroups.size()]);
    }

    /**
     * Returns the first bootstrap service that could be located that implements the supplied
     * {@link InvocationService} derivation.  <code>null</code> is returned if no such service
     * could be found.
     */
    public <T> T getService (Class<T> sclass)
    {
        if (_bstrap == null) {
            return null;
        }
        int scount = _bstrap.services.size();
        for (int ii = 0; ii < scount; ii++) {
            InvocationService<?> service = _bstrap.services.get(ii);
            if (sclass.isInstance(service)) {
                return sclass.cast(service);
            }
        }
        return null;
    }

    /**
     * Like {@link #getService} except that a {@link RuntimeException} is thrown if the service is
     * not available. Useful to avoid redundant error checking when you know that the shit will hit
     * the fan if a particular invocation service is not available.
     */
    public <T> T requireService (Class<T> sclass)
    {
        T isvc = getService(sclass);
        if (isvc == null) {
            throw new RuntimeException(
                sclass.getName() + " isn't available. I can't bear to go on.");
        }
        return isvc;
    }

    /**
     * Returns a reference to the bootstrap data provided to this client at logon time.
     */
    public BootstrapData getBootstrapData ()
    {
        return _bstrap;
    }

    /**
     * Converts a server time stamp to a value comparable to client clock readings.
     */
    public long fromServerTime (long stamp)
    {
        // when we calculated our time delta, we did it such that: C - S = dT, thus to convert
        // server to client time we do: C = S + dT
        return stamp + _serverDelta;
    }

    /**
     * Converts a client clock reading to a value comparable to a server time stamp.
     */
    public long toServerTime (long stamp)
    {
        // when we calculated our time delta, we did it such that: C - S = dT, thus to convert
        // server to client time we do: S = C - dT
        return stamp - _serverDelta;
    }

    /**
     * Returns true if we are in active communication (we may not yet be logged on, but we could be
     * trying to log on).
     */
    public synchronized boolean isActive ()
    {
        // if we have a communicator, we're doing something
        return (_comm != null);
    }

    /**
     * Checks whether we should transmit datagrams.
     */
    public synchronized boolean getTransmitDatagrams ()
    {
        return (_comm != null && _comm.getTransmitDatagrams());
    }

    /**
     * Returns true if we are logged on, false if we're not.
     */
    public synchronized boolean isLoggedOn ()
    {
        // we're not "logged on" until we're fully done with the procedure, meaning we have a
        // client object reference
        return (_clobj != null);
    }

    /**
     * Requests that this client connect and logon to the server with which it was previously
     * configured.
     *
     * @return false if we're already logged on, true if a logon attempt was initiated.
     */
    public synchronized boolean logon ()
    {
        // if we have a communicator reference, we're already logged on
        if (_comm != null) {
            return false;
        }

        // notify our observers immediately
        _observers.apply(new ObserverOps.Session(this) {
            @Override protected void notify (SessionObserver obs) {
                obs.clientWillLogon(_client);
            }
        });

        // create a new communicator and start it up; this will initiate the logon process
        _comm = createCommunicator();
        _comm.setClassLoader(_loader);
        _comm.logon();

        // register an interval to keep the clock synced and to send pings when appropriate
        if (_tickInterval == null) {
            _tickInterval = new Interval(_runQueue) {
                @Override public void expired () {
                    tick();
                }
                @Override public String toString () {
                    return "Client.tickInterval";
                }
            };
            _tickInterval.schedule(5000L, true);
        }

        return true;
    }

    /**
     * Transitions a logged on client from its current server to the specified new server.
     * Currently this simply logs the client off of its current server (if it is logged on) and
     * logs it onto the new server, but in the future we may aim to do something fancier.
     *
     * <p> If we fail to connect to the new server, the client <em>will not</em> be automatically
     * reconnected to the old server. It will be in a logged off state. However, it will be
     * reconfigured with the hostname and ports of the old server so that the caller can notify the
     * user of the failure and then simply call {@link #logon} to attempt to reconnect to the old
     * server.
     *
     * @param obs an observer that will be notified when we have successfully logged onto the
     * other server, or if the move failed.
     */
    public void moveToServer (String hostname, int[] ports, InvocationService.ConfirmListener obs)
    {
        moveToServer(hostname, ports, new int[0], obs);
    }

    /**
     * Transitions a logged on client from its current server to the specified new server.
     * Currently this simply logs the client off of its current server (if it is logged on) and
     * logs it onto the new server, but in the future we may aim to do something fancier.
     *
     * <p> If we fail to connect to the new server, the client <em>will not</em> be automatically
     * reconnected to the old server. It will be in a logged off state. However, it will be
     * reconfigured with the hostname and ports of the old server so that the caller can notify the
     * user of the failure and then simply call {@link #logon} to attempt to reconnect to the old
     * server.
     *
     * @param obs an observer that will be notified when we have successfully logged onto the
     * other server, or if the move failed.
     */
    public void moveToServer (
        String hostname, int[] ports, int[] datagramPorts, InvocationService.ConfirmListener obs)
    {
        // the server switcher will take care of everything for us
        new ServerSwitcher(hostname, ports, datagramPorts, obs).switchServers();
    }

    /**
     * Requests that the client log off of the server to which it is connected.
     *
     * @param abortable If true, the client will call <code>clientWillDisconnect</code> on all of
     * the client observers and abort the logoff process if any of them return false. If false,
     * <code>clientWillDisconnect</code> will not be called at all.
     *
     * @return true if the logoff succeeded, false if it failed due to a disagreeable observer.
     */
    public boolean logoff (boolean abortable)
    {
        // if we have no communicator, we're not logged on anyway
        if (_comm == null) {
            log.warning("Ignoring request to logoff because we're not logged on.");
            return true;
        }

        // if the request is abortable, let's run it past the observers before we act upon it
        final boolean[] rejected = new boolean[] { false };
        _observers.apply(new ObserverOps.Client(this) {
            @Override protected void notify (ClientObserver obs) {
                if (!obs.clientWillLogoff(_client)) {
                    rejected[0] = true;
                }
            }
        });
        if (abortable && rejected[0]) {
            return false;
        }

        // ask the communicator to send a logoff message and disconnect from the server
        _comm.logoff();

        return true;
    }

    /**
     * Prepares the client for a standalone mode logon. Returns the set of bootstrap service groups
     * that should be supplied to the invocation manager to create our fake bootstrap record.
     */
    public String[] prepareStandaloneLogon ()
    {
        _standalone = true;
        // notify our observers immediately
        _observers.apply(new ObserverOps.Session(this) {
            @Override protected void notify (SessionObserver obs) {
                obs.clientWillLogon(_client);
            }
        });
        return getBootGroups();
    }

    /**
     * Logs this client on in standalone mode with the faked bootstrap data and shared local
     * distributed object manager.
     */
    public void standaloneLogon (BootstrapData data, DObjectManager omgr)
    {
        if (!_standalone) {
            throw new IllegalStateException("Must call prepareStandaloneLogon() first.");
        }
        gotBootstrap(data, omgr);
    }

    /**
     * For standalone mode, this notifies observers that the client has logged off and cleans up.
     */
    public void standaloneLogoff ()
    {
        notifyObservers(new ObserverOps.Session(this) {
            @Override protected void notify (SessionObserver obs) {
                obs.clientDidLogoff(_client);
            }
        });
        cleanup(null); // this will set _standalone to false
    }

    @Override
    public String toString ()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName()).append(" [");
        fieldsToString(builder);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Adds text representation of fields to the builder. The results will be placed between
     * brackets by <code>toString</code>.
     */
    public void fieldsToString (StringBuilder builder)
    {
        builder.append("hostname=").append(_hostname);
        if (_ports != null && _ports.length > 0) {
            builder.append(", ports=");
            StringUtil.toString(builder, _ports);
        }
        if (_datagramPorts != null && _datagramPorts.length > 0) {
            builder.append(", datagramPorts=");
            StringUtil.toString(builder, _datagramPorts);
        }
        builder.append(", clOid=").append(_cloid);
        builder.append(", connId=").append(_connectionId);
        builder.append(", creds=");
        StringUtil.toString(builder, _creds);
    }

    /**
     * Called by the {@link ClientDObjectMgr} when our bootstrap notification arrives. If the
     * client and server are being run in "merged" mode in a single JVM, this is how the client is
     * configured with the server's distributed object manager and provided with bootstrap data.
     */
    protected void gotBootstrap (BootstrapData data, DObjectManager omgr)
    {
        if (debugLogMessages()) {
            log.info("Got bootstrap " + data + ".");
        }

        // keep these around for interested parties
        _bstrap = data;
        _omgr = omgr;

        // extract bootstrap information
        _connectionId = data.connectionId;
        _cloid = data.clientOid;

        // notify the communicator that we got our bootstrap data (if we have one)
        if (_comm != null) {
            _comm.gotBootstrap();
        }

        // initialize our invocation director
        _invdir.init(omgr, _cloid, this);

        // send a few pings to the server to establish the clock offset between this client and
        // server standard time
        establishClockDelta(System.currentTimeMillis());

        // we can't quite call initialization completed at this point because we need for the
        // invocation director to fully initialize (which requires a round trip to the server)
        // before turning the client loose to do things like request invocation services
    }

    /**
     * If this client is being used to proxy events from another server, this method can be
     * overridden to adjust the event in any way needed prior to dispatching the event.
     */
    protected void convertFromRemote (DObject target, DEvent event)
    {
        // nothing by default
    }

    /**
     * Creates the communicator that this client will use to send and receive messages.
     */
    protected Communicator createCommunicator ()
    {
        return new ClientCommunicator(this);
    }

    /**
     * Configures the outgoing message throttle. This is done when the server informs us that a new
     * rate is in effect.
     */
    protected synchronized void setOutgoingMessageThrottle (int msgsPerSec)
    {
        log.info("Updating outgoing message throttle", "msgsPerSec", msgsPerSec);
        _outThrottle.reinit(msgsPerSec, 1000L);
        _comm.postMessage(new ThrottleUpdatedMessage());
    }

    /**
     * Returns our outgoing message throttle. Used by the communicator's writer.
     */
    protected synchronized Throttle getOutgoingMessageThrottle ()
    {
        return _outThrottle;
    }

    /**
     * Returns a reference to the message tracker to notify on message transmission and receipt.
     */
    protected MessageTracker getMessageTracker ()
    {
        return _messageTracker;
    }

    /**
     * Called every five seconds; ensures that we ping the server if we haven't communicated in a
     * long while and periodically resyncs the client and server clock deltas.
     */
    protected void tick ()
    {
        // if we're not connected, skip it
        if (_comm == null) {
            return;
        }

        long now = RunAnywhere.currentTimeMillis();
        if (_dcalc != null) {
            // if our current calculator is done, clear it out
            if (_dcalc.isDone()) {
                if (debugLogMessages()) {
                    log.info("Time offset from server: " + _serverDelta + "ms.");
                }
                _dcalc = null;
            } else if (_dcalc.shouldSendPing()) {
                // otherwise, send another ping
                PingRequest req = new PingRequest();
                _comm.postMessage(req);
                _dcalc.sentPing(req);
            }

        } else if (now - _comm.getLastWrite() > PingRequest.PING_INTERVAL) {
            // if we haven't sent anything over the network in a while, we ping the server to let
            // it know that we're still alive
            _comm.postMessage(new PingRequest());

        } else if (now - _lastSync > CLOCK_SYNC_INTERVAL) {
            // resync our clock with the server
            establishClockDelta(now);
        }
    }

    /**
     * Called during initialization to initiate a sequence of ping/pong messages which will be used
     * to determine (with "good enough" accuracy) the difference between the client clock and the
     * server clock so that we can later interpret server timestamps.
     */
    protected void establishClockDelta (long now)
    {
        if (_comm != null) {
            // create a new delta calculator and start the process
            _dcalc = new DeltaCalculator();
            PingRequest req = new PingRequest();
            _comm.postMessage(req);
            _dcalc.sentPing(req);
            _lastSync = now;
        }
    }

    /**
     * Called by the {@link Communicator} if it is experiencing trouble logging on but is still
     * trying fallback strategies.
     */
    protected void reportLogonTribulations (final LogonException cause)
    {
        notifyObservers(new ObserverOps.Client(this) {
            @Override protected void notify (ClientObserver obs) {
                obs.clientFailedToLogon(_client, cause);
            }
        });
    }

    /**
     * Called by the invocation director when it successfully subscribes to the client object
     * immediately following logon.
     */
    protected void gotClientObject (ClientObject clobj)
    {
        // keep this around
        _clobj = clobj;

        // let the client know that logon has now fully succeeded
        notifyObservers(new ObserverOps.Session(this) {
            @Override protected void notify (SessionObserver obs) {
                obs.clientDidLogon(_client);
            }
        });
    }

    /**
     * Called by the invocation director if it fails to subscribe to the client object after logon.
     */
    protected void getClientObjectFailed (final Exception cause)
    {
        // pass the buck onto the listeners
        notifyObservers(new ObserverOps.Client(this) {
            @Override protected void notify (ClientObserver obs) {
                obs.clientFailedToLogon(_client, cause);
            }
        });
    }

    /**
     * Called by the invocation director when it discovers that the client object has changed.
     */
    protected void clientObjectDidChange (ClientObject clobj)
    {
        _clobj = clobj;
        _cloid = _clobj.getOid();

        // report to our observers
        notifyObservers(new ObserverOps.Session(this) {
            @Override protected void notify (SessionObserver obs) {
                obs.clientObjectDidChange(_client);
            }
        });
    }

    protected void notifyObservers (final ObserverOps.Session op)
    {
        // if we have no run queue, or we're already on the run queue's thread; dispatch this
        // operation immediately on the current thread
        if (_runQueue == null || _runQueue.isDispatchThread()) {
            _observers.apply(op);
        }
        // otherwise queue this notification up to run on the run queue thread
        else {
            _runQueue.postRunnable(new Runnable() {
                public void run () {
                    _observers.apply(op);
                }
            });
        }
    }

    protected synchronized void cleanup (final Exception logonError)
    {
        // kill our tick interval here, otherwise we may get a tick call after the runnable below
        // has finished
        if (_tickInterval != null) {
            _tickInterval.cancel();
            _tickInterval = null;
        }

        // restore the default message throttle
        _outThrottle = new Throttle(DEFAULT_MSGS_PER_SECOND, 1000L);

        // we know that prior to the call to this method, the observers were notified with
        // CLIENT_DID_LOGOFF; that may not have been invoked yet, so we don't want to clear out our
        // communicator reference immediately; instead we queue up a runnable unit to do so to
        // ensure that it won't happen until CLIENT_DID_LOGOFF was dispatched
        _runQueue.postRunnable(new Runnable() {
            public void run () {
                // tell the object manager that we're no longer connected to the server
                if (_omgr instanceof ClientDObjectMgr) {
                    ((ClientDObjectMgr)_omgr).cleanup();
                }

                // clear out our references
                _comm = null;
                _bstrap = null;
                _omgr = null;
                _clobj = null;
                _connectionId = _cloid = -1;
                _standalone = false;

                // and let our invocation director know we're logged off
                _invdir.cleanup();

                // if we were cleaned up due to a failure to logon, we can report the logon error
                // now that the communicator is cleaned up; this allows a logon failure listener to
                // immediately try another logon (hopefully with something changed like the server
                // or port)
                notifyObservers(new ObserverOps.Client(Client.this) {
                    @Override protected void notify (ClientObserver obs) {
                        if (logonError != null) {
                            obs.clientFailedToLogon(_client, logonError);
                        } else {
                            obs.clientDidClear(_client);
                        }
                    }
                });
            }
        });
    }

    /**
     * Called when we receive a pong packet. We may be in the process of calculating the client/
     * server time differential, or we may have already done that at which point we ignore pongs.
     */
    protected void gotPong (PongResponse pong)
    {
        // if we're not currently calculating our delta, then we can throw away the pong
        if (_dcalc != null) {
            // we update the delta after every receipt so as to immediately obtain an estimate of
            // the clock delta and then refine it as more packets come in
            _dcalc.gotPong(pong);
            _serverDelta = _dcalc.getTimeDelta();
        }
    }

    /**
     * Whether or not to log low-level debug messages. This is used by the communicator as well
     * which may be running on the server as a peer client, so we want to avoid constructing log
     * messages when we're not logging and thus need to use this pattern rather than just
     * <code>log.fine</code>.
     */
    protected boolean debugLogMessages ()
    {
        return false;
    }

    /**
     * Should we be logging the specified failure?
     */
    protected boolean isFailureLoggable (FailureType type)
    {
        return true;
    }

    /**
     * The types of failures that may occur in our associated classes, which may call
     * isFailureLoggable() for use to arbitrate whether we want to log the error.
     */
    protected enum FailureType
    {
        /** An attempt was made to unsubscribe from an object that is not proxied. */
        UNSUBSCRIBE_NOT_PROXIED,

        // TODO: fill out more
        ;
    }

    /** Handles the process of switching between servers. See {@link Client#moveToServer}. */
    protected class ServerSwitcher extends ClientAdapter
    {
        public ServerSwitcher (
            String hostname, int[] ports, InvocationService.ConfirmListener obs) {
            this(hostname, ports, new int[0], obs);
        }

        public ServerSwitcher (
            String hostname, int[] ports, int[] datagramPorts,
            InvocationService.ConfirmListener obs) {
            _hostname = hostname;
            _ports = ports;
            _datagramPorts = datagramPorts;
            _observer = obs;
        }

        public void switchServers () {
            addClientObserver(this);
            if (!isLoggedOn()) {
                // if we're not logged on right now, just do the switch immediately
                clientDidClear(Client.this);

            } else {
                // note our current connection information so that we can restore it if our logon
                // attempt fails
                _oldHostname = Client.this._hostname;
                _oldPorts = Client.this._ports;
                _oldDatagramPorts = Client.this._datagramPorts;

                // otherwise logoff and wait for all of our callbacks to clear
                logoff(true);
            }
        }

        @Override
        public void clientDidClear (Client client) {
            // configure the client to point to the new server and logon
            setServer(_hostname, _ports, _datagramPorts);

            if (!logon()) {
                log.warning("logon() failed during server switch?",
                    "hostname", _hostname, "ports", _ports, "datagramPorts", _datagramPorts);
                clientFailedToLogon(Client.this, new Exception("logon() failed?"));
            }
        }

        @Override
        public void clientDidLogon (Client client) {
            removeClientObserver(this);
            if (_observer != null) {
                _observer.requestProcessed();
            }
        }

        @Override
        public void clientFailedToLogon (Client client, Exception cause) {
            removeClientObserver(this);
            if (_oldHostname != null) { // restore our previous server and ports
                setServer(_oldHostname, _oldPorts, _oldDatagramPorts);
            }
            if (_observer != null) {
                _observer.requestFailed((cause instanceof LogonException) ?
                                        cause.getMessage() : AuthCodes.SERVER_ERROR);
            }
        }

        protected String _hostname, _oldHostname;
        protected int[] _ports, _oldPorts;
        protected int[] _datagramPorts, _oldDatagramPorts;
        protected InvocationService.ConfirmListener _observer;
    }

    /** The credentials we used to authenticate with the server. */
    protected Credentials _creds;

    /** The version string reported to the server at auth time. */
    protected String _version = "";

    /** An entity that gives us the ability to process events on the main client thread. */
    protected RunQueue _runQueue;

    /** The distributed object manager we're using during this session. */
    protected DObjectManager _omgr;

    /** The data associated with our authentication response. */
    protected AuthResponseData _authData;

    /** Our public key. */
    protected PublicKey _publicKey;

    /** Our session secret key. */
    protected byte[] _secret;

    /** If we require a secure connection to send our credentials. */
    protected boolean _requireSecureAuth = false;

    /** The unique id of our connection. */
    protected int _connectionId = -1;

    /** Our client distributed object id. */
    protected int _cloid = -1;

    /** Our client distributed object. */
    protected ClientObject _clobj;

    /** Whether or not this client is operating in a standalone mode. */
    protected boolean _standalone;

    /** The game server host. */
    protected String _hostname;

    /** The ports on which we connect to the game server. */
    protected int[] _ports;

    /** The server ports to which we can send datagrams. */
    protected int[] _datagramPorts;

    /** Our list of client observers. */
    protected ObserverList<SessionObserver> _observers = ObserverList.newSafeInOrder();

    /** The entity that manages our network communications. */
    protected Communicator _comm;

    /** A custom class loader used to load objects that come in over the network. */
    protected ClassLoader _loader = getClass().getClassLoader();

    /** General startup information provided by the server. */
    protected BootstrapData _bstrap;

    /** The set of bootstrap service groups this client cares about. */
    protected HashSet<String> _bootGroups = Sets.newHashSet(); {
        _bootGroups.add(InvocationCodes.GLOBAL_GROUP);
    }

    /** Manages invocation services. */
    protected InvocationDirector _invdir = new InvocationDirector();

    /** The difference between the server clock and the client clock (estimated immediately after
     * logging on). */
    protected long _serverDelta;

    /** Used when establishing our clock delta between the client and server. */
    protected DeltaCalculator _dcalc;

    /** The last time at which we synced our clock with the server. */
    protected long _lastSync;

    /** Our tick interval id. */
    protected Interval _tickInterval;

    /** Our outgoing message throttle. */
    protected Throttle _outThrottle = new Throttle(DEFAULT_MSGS_PER_SECOND, 1000L);

    /** The tracker to notify on message transmission or receipt. */
    protected volatile MessageTracker _messageTracker = MessageTracker.NOOP;

    /** How often we recompute our time offset from the server. */
    protected static final long CLOCK_SYNC_INTERVAL = 600 * 1000L;
}
