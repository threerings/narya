//
// $Id: Client.java,v 1.32 2002/10/21 18:04:56 mdb Exp $

package com.threerings.presents.client;

import java.util.ArrayList;
import java.util.List;

import com.samskivert.util.ObserverList;
import com.samskivert.util.ResultListener;

import com.threerings.presents.Log;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.PingRequest;
import com.threerings.presents.net.PongResponse;

/**
 * Through the client object, a connection to the system is established
 * and maintained. The client object maintains two separate threads (a
 * reader and a writer) by which all network traffic is managed.
 */
public class Client
{
    /** The default port on which the server listens for client
     * connections. */
    public static final int DEFAULT_SERVER_PORT = 4007;

    /**
     * This is used by the client to allow dobj event dispatching to take
     * place along side the activities of the rest of the application
     * (usually this means running dobj events on the AWT thread).
     */
    public static interface Invoker
    {
        /**
         * Requests that the supplied runnable be queued up for invocation
         * on the main event dispatching thread of the application.
         */
        public void invokeLater (Runnable run);
    }

    /**
     * Constructs a client object with the supplied credentials and
     * invoker. The creds will be used to authenticate with any server to
     * which this client attempts to connect. The invoker is used to
     * operate the distributed object event dispatch mechanism. To allow
     * the dobj event dispatch to coexist with threads like the AWT
     * thread, the client will request that the invoker queue up a
     * runnable whenever there are distributed object events that need to
     * be processed. The invoker can then queue that runnable up on the
     * AWT thread if it is so inclined to make life simpler for the rest
     * of the application.
     *
     * @param creds the credentials to use when logging on to the server.
     * These can be null, but <code>setCredentials</code> must then be
     * called before any call to <code>logon</code>.
     * @param invoker an invoker that can be used to process incoming
     * events.
     */
    public Client (Credentials creds, Invoker invoker)
    {
        _creds = creds;
        _invoker = invoker;
    }

    /**
     * Registers the supplied observer with this client. While registered
     * the observer will receive notifications of state changes within the
     * client. The function will refuse to register an already registered
     * observer.
     *
     * @see ClientObserver
     * @see SessionObserver
     */
    public void addClientObserver (SessionObserver observer)
    {
        synchronized (_observers) {
            // disallow multiple instances of the same observer
            if (!_observers.contains(observer)) {
                _observers.add(observer);
            }
        }
    }

    /**
     * Unregisters the supplied observer. Upon return of this function,
     * the observer will no longer receive notifications of state changes
     * within the client.
     */
    public void removeClientObserver (SessionObserver observer)
    {
        synchronized (_observers) {
            _observers.remove(observer);
        }
    }

    /**
     * Configures the client to communicate with the server on the
     * supplied hostname/port combination.
     *
     * @see #logon
     */
    public void setServer (String hostname, int port)
    {
        _hostname = hostname;
        _port = port;
    }

    /**
     * Returns the invoker in use by this client. This can be used to
     * queue up event dispatching stints.
     */
    public Invoker getInvoker ()
    {
        return _invoker;
    }

    /**
     * Returns the hostname of the server to which this client is
     * currently configured to connect.
     */
    public String getHostname ()
    {
        return _hostname;
    }

    /**
     * Returns the port on which this client is currently configured to
     * connect to the server.
     */
    public int getPort ()
    {
        return _port;
    }

    /**
     * Returns the credentials with which this client is currently
     * configured to connect to the server.
     */
    public Credentials getCredentials ()
    {
        return _creds;
    }

    /**
     * Sets the credentials that will be used by this client to
     * authenticate with the server. This should be done before any call
     * to <code>logon</code>.
     */
    public void setCredentials (Credentials creds)
    {
        _creds = creds;
    }

    /**
     * Returns the distributed object manager associated with this
     * session. This reference is only valid for the duration of the
     * session and a new reference must be obtained if the client
     * disconnects and reconnects to the server.
     *
     * @return the dobjmgr in effect or null if we have no established
     * connection to the server.
     */
    public DObjectManager getDObjectManager ()
    {
        return (_comm != null) ? _comm.getDObjectManager() : null;
    }

    /**
     * Returns the oid of the client object associated with this session.
     * It is only valid for the duration of the session.
     */
    public int getClientOid ()
    {
        return _cloid;
    }

    /**
     * Returns a reference to the client object associated with this
     * session. It is only valid for the duration of the session.
     */
    public ClientObject getClientObject ()
    {
        return _clobj;
    }

    /**
     * Returns the invocation director associated with this session. This
     * reference is only valid for the duration of the session.
     */
    public InvocationDirector getInvocationDirector ()
    {
        return _invdir;
    }

    /**
     * Returns the first bootstrap service that could be located that
     * implements the supplied {@link InvocationService} derivation.
     * <code>null</code> is returned if no such service could be found.
     */
    public InvocationService getService (Class sclass)
    {
        int scount = _bstrap.services.size();
        for (int ii = 0; ii < scount; ii++) {
            InvocationService service = (InvocationService)
                _bstrap.services.get(ii);
            if (sclass.isInstance(service)) {
                return service;
            }
        }
        return null;
    }

    /**
     * Like {@link #getService} except that a {@link RuntimeException} is
     * thrown if the service is not available. Useful to avoid redundant
     * error checking when you know that the shit will hit the fan if a
     * particular invocation service is not available.
     */
    public InvocationService requireService (Class sclass)
    {
        InvocationService isvc = getService(sclass);
        if (isvc == null) {
            throw new RuntimeException(sclass.getName() + " isn't available. " +
                                       "I can't bear to go on.");
        }
        return isvc;
    }

    /**
     * Returns a reference to the bootstrap data provided to this client
     * at logon time.
     */
    public BootstrapData getBootstrapData ()
    {
        return _bstrap;
    }

    /**
     * Converts a server time stamp to a value comparable to client clock
     * readings.
     */
    public long fromServerTime (long stamp)
    {
        // when we calcuated our time delta, we did it such that: C - S =
        // dT, thus to convert server to client time we do: C = S + dT
        return stamp + _serverDelta;
    }

    /**
     * Returns true if we are logged on, false if we're not.
     */
    public synchronized boolean isLoggedOn ()
    {
        // we're not "logged on" until we're fully done with the
        // procedure, meaning we have a client object reference
        return (_clobj != null);
    }

    /**
     * Requests that this client connect and logon to the server with
     * which it was previously configured.
     */
    public synchronized void logon ()
    {
        // if we have a communicator reference, we're already logged on
        if (_comm != null) {
            return;
        }

        // otherwise create a new communicator instance and start it up.
        // this will initiate the logon process
        _comm = new Communicator(this);
        _comm.logon();
    }

    /**
     * Requests that the client log off of the server to which it is
     * connected.
     *
     * @param abortable If true, the client will call
     * <code>clientWillDisconnect</code> on all of the client observers
     * and abort the logoff process if any of them return false. If false,
     * <code>clientWillDisconnect</code> will not be called at all.
     *
     * @return true if the logoff succeeded, false if it failed due to a
     * disagreeable observer.
     */
    public boolean logoff (boolean abortable)
    {
        // if we have no communicator, we're not logged on anyway
        if (_comm == null) {
            Log.warning("Ignoring request to logoff because we're not " +
                        "logged on.");
            return true;
        }

        // if the request is abortable, let's run it past the observers
        // before we act upon it
        if (abortable && notifyObservers(CLIENT_WILL_LOGOFF, null)) {
            return false;
        }

        // ask the communicator to send a logoff message and disconnect
        // from the server
        _comm.logoff();

        return true;
    }

    /**
     * Called during initialization to initiate a sequence of ping/pong
     * messages which will be used to determine (with "good enough"
     * accuracy) the difference between the client clock and the server
     * clock so that we can later interpret server timestamps.
     */
    protected void establishClockDelta ()
    {
        // create a new delta calculator and start the process
        _dcalc = new DeltaCalculator();
        PingRequest req = new PingRequest();
        _comm.postMessage(req);
        _dcalc.sentPing(req);
    }

    /**
     * This is called when we've completed the process of pinging the
     * server a few times to establish our clock delta.
     */
    protected void clockDeltaEstablished ()
    {
        // initialize our invocation director
        _invdir.init(_comm.getDObjectManager(), _cloid, this);

        // we can't quite call initialization completed at this point
        // because we need for the invocation director to fully initialize
        // (which requires a round trip to the server) before turning the
        // client loose to do things like request invocation services
    }

    /**
     * Called by the invocation director when it successfully subscribes
     * to the client object immediately following logon.
     */
    protected void gotClientObject (ClientObject clobj)
    {
        // keep this around
        _clobj = clobj;

        // let the client know that logon has now fully succeeded
        notifyObservers(Client.CLIENT_DID_LOGON, null);
    }

    /**
     * Called by the invocation director if it fails to subscribe to the
     * client object after logon.
     */
    protected void getClientObjectFailed (Exception cause)
    {
        // pass the buck onto the listeners
        notifyObservers(Client.CLIENT_FAILED_TO_LOGON, cause);
    }

    /**
     * Called by the invocation director when it discovers that the client
     * object has changed.
     */
    protected void clientObjectDidChange (ClientObject clobj)
    {
        _clobj = clobj;
        _cloid = _clobj.getOid();

        // report to our observers
        notifyObservers(Client.CLIENT_OBJECT_CHANGED, null);
    }

    boolean notifyObservers (int code, Exception cause)
    {
        final Notifier noty = new Notifier(code, cause);
        Runnable unit = new Runnable() {
            public void run () {
                synchronized (_observers) {
                    _observers.apply(noty);
                }
            }
        };

        // we need to run immediately if this is WILL_LOGOFF or if we have
        // no invoker (which currently only happens in some really obscure
        // circumstances where we're using a Client instance on the server
        // so that we can sort of pretend to be a real client)
        if (code == CLIENT_WILL_LOGOFF || _invoker == null) {
            unit.run();
            return noty.getRejected();

        } else {
            // otherwise we can queue this notification up with our
            // invoker and ensure that it's run on the proper thread
            _invoker.invokeLater(unit);
            return false;
        }
    }

    synchronized void communicatorDidExit ()
    {
        // we know that prior to the call to this method, the observers
        // were notified with CLIENT_DID_LOGOFF; that may not have been
        // invoked yet, so we don't want to clear out our communicator
        // reference immediately; instead we queue up an invoker unit to
        // do so to ensure that it won't happen until CLIENT_DID_LOGOFF
        // was dispatched
        _invoker.invokeLater(new Runnable() {
            public void run () {
                // clear out our references
                _comm = null;
                _clobj = null;
                _cloid = -1;
                // and let our invocation director know we're logged off
                _invdir.cleanup();
            }
        });
    }

    /**
     * Called by the omgr when a bootstrap notification arrives.
     */
    void gotBootstrap (BootstrapData data)
    {
        // keep this around for interested parties
        _bstrap = data;

        // extract bootstrap information
        _cloid = data.clientOid;

        // send a few pings to the server to establish the clock offset
        // between this client and server standard time
        establishClockDelta();
    }

    /**
     * Called when we receive a pong packet. We may be in the process of
     * calculating the client/server time differential, or we may have
     * already done that at which point we ignore pongs.
     */
    void gotPong (PongResponse pong)
    {
        // if we're not calculating our client/server time delta, then we
        // don't need to do anything with the pong
        if (_dcalc == null) {
            return;
        }

        if (_dcalc.gotPong(pong)) {
            // we're either done with our calculations, so we can grab the
            // time delta and finish our business...
            _serverDelta = _dcalc.getTimeDelta();
            // free up our delta calculator
            _dcalc = null;
            // let the client continue with its initialization
            clockDeltaEstablished();

        } else {
            // ...or we'll either be sending another ping
            PingRequest req = new PingRequest();
            _comm.postMessage(req);
            _dcalc.sentPing(req);
        }
    }

    /**
     * Used to notify client observers of events.
     */
    protected class Notifier
        implements ObserverList.ObserverOp
    {
        public Notifier (int code, Exception cause)
        {
            _code = code;
            _cause = cause;
            _rejected = false;
        }

        public boolean getRejected ()
        {
            return _rejected;
        }

        public boolean apply (Object observer)
        {
            SessionObserver obs = (SessionObserver)observer;
            switch (_code) {
            case CLIENT_DID_LOGON:
                obs.clientDidLogon(Client.this);
                break;

            case CLIENT_FAILED_TO_LOGON:
                if (obs instanceof ClientObserver) {
                    ((ClientObserver)obs).clientFailedToLogon(
                        Client.this, _cause);
                }
                break;

            case CLIENT_OBJECT_CHANGED:
                if (obs instanceof SessionObserver) {
                    ((SessionObserver)obs).clientObjectDidChange(Client.this);
                }
                break;

            case CLIENT_CONNECTION_FAILED:
                if (obs instanceof ClientObserver) {
                    ((ClientObserver)obs).clientConnectionFailed(
                        Client.this, _cause);
                }
                break;

            case CLIENT_WILL_LOGOFF:
                if (obs instanceof ClientObserver) {
                    if (!((ClientObserver)obs).clientWillLogoff(Client.this)) {
                        _rejected = true;
                    }
                }
                break;

            case CLIENT_DID_LOGOFF:
                obs.clientDidLogoff(Client.this);
                break;

            default:
                throw new RuntimeException("Invalid code supplied to " +
                                           "notifyObservers: " + _code);
            }

            return true;
        }

        protected int _code;
        protected Exception _cause;
        protected boolean _rejected;
    }

    /** The credentials we used to authenticate with the server. */
    protected Credentials _creds;

    /** An entity that gives us the ability to process events on the main
     * client thread (which is also the AWT thread). */
    protected Invoker _invoker;

    /** Our client distribted object id. */
    protected int _cloid = -1;

    /** Our client distributed object. */
    protected ClientObject _clobj;

    /** The game server host. */
    protected String _hostname;

    /** The port on which we connect to the game server. */
    protected int _port;

    /** Our list of client observers. */
    protected ObserverList _observers =
        new ObserverList(ObserverList.SAFE_IN_ORDER_NOTIFY);

    /** The entity that manages our network communications. */
    protected Communicator _comm;

    /** General startup information provided by the server. */
    protected BootstrapData _bstrap;

    /** Manages invocation services. */
    protected InvocationDirector _invdir = new InvocationDirector();

    /** The difference between the server clock and the client clock
     * (estimated immediately after logging on). */
    protected long _serverDelta;

    /** Used when establishing our clock delta between the client and
     * server. */
    protected DeltaCalculator _dcalc;

    // client observer codes
    static final int CLIENT_DID_LOGON = 0;
    static final int CLIENT_FAILED_TO_LOGON = 1;
    static final int CLIENT_OBJECT_CHANGED = 2;
    static final int CLIENT_CONNECTION_FAILED = 3;
    static final int CLIENT_WILL_LOGOFF = 4;
    static final int CLIENT_DID_LOGOFF = 5;
}
