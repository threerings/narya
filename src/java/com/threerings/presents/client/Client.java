//
// $Id: Client.java,v 1.11 2001/07/25 00:51:05 mdb Exp $

package com.threerings.cocktail.cher.client;

import java.util.ArrayList;
import java.util.List;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.dobj.DObjectManager;
import com.threerings.cocktail.cher.net.BootstrapData;
import com.threerings.cocktail.cher.net.Credentials;

/**
 * Through the client object, a connection to the system is established
 * and maintained. The client object maintains two separate threads (a
 * reader and a writer) by which all network traffic is managed.
 */
public class Client
{
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
     */
    public void addObserver (ClientObserver observer)
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
    public void removeObserver (ClientObserver observer)
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
     * Returns the invocation manager associated with this session. This
     * reference is only valid for the duration of the session.
     */
    public InvocationManager getInvocationManager ()
    {
        return _invmgr;
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

    boolean notifyObservers (int code, Exception cause)
    {
        boolean rejected = false;
        synchronized (_observers) {
            for (int i = 0; i < _observers.size(); i++) {
                ClientObserver obs = (ClientObserver)_observers.get(i);
                switch (code) {
                case CLIENT_DID_LOGON:
                    obs.clientDidLogon(this);
                    break;
                case CLIENT_FAILED_TO_LOGON:
                    obs.clientFailedToLogon(this, cause);
                    break;
                case CLIENT_CONNECTION_FAILED:
                    obs.clientConnectionFailed(this, cause);
                    break;
                case CLIENT_WILL_LOGOFF:
                    if (!obs.clientWillLogoff(this)) {
                        rejected = true;
                    }
                    break;
                case CLIENT_DID_LOGOFF:
                    obs.clientDidLogoff(this);
                    break;
                default:
                    throw new RuntimeException("Invalid code supplied to " +
                                               "notifyObservers: " + code);
                }
            }
        }
        return rejected;
    }

    synchronized void communicatorDidExit ()
    {
        // clear out our communicator reference
        _comm = null;
    }

    /**
     * Called by the omgr when a bootstrap notification arrives.
     */
    void gotBootstrap (BootstrapData data)
    {
        // extract bootstrap information
        _cloid = data.clientOid;

        // create our invocation manager
        _invmgr = new InvocationManager(this, data.invOid);

        // we can't quite call initialization completed at this point
        // because we need for the invocation manager to fully initialize
        // (which requires a round trip to the server) before turning the
        // client loose to do things like request invocation services
    }

    void invocationManagerReady ()
    {
        // let the client know that logon has now fully succeeded
        notifyObservers(Client.CLIENT_DID_LOGON, null);
    }

    protected Credentials _creds;
    protected Invoker _invoker;

    protected String _hostname;
    protected int _port;

    /** Our list of client observers. */
    protected List _observers = new ArrayList();

    /** The entity that manages our network communications. */
    protected Communicator _comm;

    protected int _cloid;
    protected InvocationManager _invmgr;

    // client observer codes
    static final int CLIENT_DID_LOGON = 0;
    static final int CLIENT_FAILED_TO_LOGON = 1;
    static final int CLIENT_CONNECTION_FAILED = 2;
    static final int CLIENT_WILL_LOGOFF = 3;
    static final int CLIENT_DID_LOGOFF = 4;
}
