//
// $Id: PresentsClient.java,v 1.17 2001/08/07 21:25:13 mdb Exp $

package com.threerings.cocktail.cher.server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.data.ClientObject;
import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.cher.net.*;
import com.threerings.cocktail.cher.server.net.*;
import com.threerings.cocktail.cher.util.IntMap;

/**
 * A client object represents a client session in the server. It is
 * associated with a connection instance (while the client is connected)
 * and acts as the intermediary for the remote client in terms of passing
 * along events forwarded by the client, ensuring that subscriptions are
 * maintained on behalf of the client and that events are forwarded to the
 * client.
 *
 * <p><em>A note on synchronization:</em> the client object is structured
 * so that its <code>Subscriber</code> implementation (which is called
 * from the dobjmgr thread) can proceed without synchronization. This does
 * not overlap with its other client duties which are called from the
 * conmgr thread and therefore also need not be synchronized.
 */
public class CherClient implements Subscriber, MessageHandler
{
    /**
     * Returns the username with which this client instance is associated.
     */
    public String getUsername ()
    {
        return _username;
    }

    /**
     * Returns the client object that is associated with this client.
     */
    public ClientObject getClientObject ()
    {
        return _clobj;
    }

    /**
     * Initializes this client instance with the specified username and
     * connection instance and begins a client session.
     */
    protected void startSession (
        ClientManager cmgr, String username, Connection conn)
    {
        _cmgr = cmgr;
        _username = username;
        setConnection(conn);

        // create our client object and bootstrap the client once we've
        // got it
        Subscriber sub = new Subscriber ()
        {
            public void objectAvailable (DObject object)
            {
                setClientObject((ClientObject)object);
                sessionWillStart();
                sendBootstrap();
            }

            public void requestFailed (int oid, ObjectAccessException cause)
            {
                Log.warning("Unable to create client object " +
                            "[client=" + CherClient.this +
                            ", error=" + cause + "].");
            }

            public boolean handleEvent (DEvent event, DObject target)
            {
                return false;
            }
        };
        Class clobjClass = _cmgr.getClientObjectClass();
        CherServer.omgr.createObject(clobjClass, sub, false);
    }

    /**
     * Called when we receive our client object from the dobjmgr. We go
     * through this synchronized method to ensure that the client object
     * is visible to the conmgr.
     */
    protected synchronized void setClientObject (ClientObject clobj)
    {
        _clobj = clobj;
    }

    /**
     * Called by the client manager when a new connection arrives that
     * authenticates as this already established client. This must only be
     * called from the congmr thread.
     */
    protected void resumeSession (Connection conn)
    {
        Connection oldconn = getConnection();

        // check to see if we've already got a connection object, in which
        // case it's probably stale
        if (oldconn != null) {
            Log.info("Closing stale connection [old=" + oldconn +
                     ", new=" + conn + "].");
            // close the old connection (which results in everything being
            // properly unregistered)
            oldconn.close();
        }

        // start using the new connection
        setConnection(conn);

        // we need to get onto the distributed object thread so that we
        // can finalize the resumption of the session. we do so by
        // posting a special event
        DEvent event = new DEvent(0)
        {
            public boolean applyToObject (DObject target)
            {
                // now that we're on the dobjmgr thread we can resume our
                // session resumption
                finishResumeSession();
                return false;
            }
        };
        CherServer.omgr.postEvent(event);
    }

    /**
     * This is called from the dobjmgr thread to complete the session
     * resumption. We call some call backs and send the bootstrap info to
     * the client.
     */
    protected void finishResumeSession ()
    {
        // let derived classes do any session resuming
        sessionWillResume();

        // send off a bootstrap notification immediately because we've
        // already got our client object
        sendBootstrap();

        Log.info("Session resumed [client=" + this + "].");
    }

    /**
     * Requests that the client session be terminated. This is normally
     * invoked as a result of a logoff request by the client. It should
     * only be invoked from the conmgr thread.
     */
    public void endSession ()
    {
        // close our connection (which results in everything being
        // properly unregistered)
        Connection conn = getConnection();
        if (conn != null) {
            conn.close();
        } else {
            Log.info("Unable to close connection for logoff request " +
                     "[client=" + this + "].");
        }
        // then let the client manager know what's up
        _cmgr.clientDidEndSession(this);

        // we need to get onto the distributed object thread so that we
        // can finalize the ending of the session. we do so by posting a
        // special event
        DEvent event = new DEvent(0)
        {
            public boolean applyToObject (DObject target)
            {
                // now that we're on the dobjmgr thread we can call into
                // our derived classes to finish the session termination
                sessionDidTerminate();
                return false;
            }
        };
        CherServer.omgr.postEvent(event);
    }

    /**
     * Makes a note that this client is subscribed to this object so that
     * we can clean up after ourselves if and when the client goes
     * away. This is called by the client internals and needn't be called
     * by code outside the client.
     */
    public synchronized void mapSubscrip (DObject object)
    {
        _subscrips.put(object.getOid(), object);
    }

    /**
     * Makes a note that this client is no longer subscribed to this
     * object. The subscription map is used to clean up after the client
     * when it goes away. This is called by the client internals and
     * needn't be called by code outside the client.
     */
    public synchronized void unmapSubscrip (int oid)
    {
        _subscrips.remove(oid);
    }

    /**
     * Clears out the tracked client subscriptions. Called when the client
     * goes away and shouldn't be called otherwise.
     */
    protected synchronized void clearSubscrips ()
    {
        Enumeration enum = _subscrips.elements();
        while (enum.hasMoreElements()) {
            DObject object = (DObject)enum.nextElement();
            // Log.info("Clearing subscription [client=" + this +
            // ", obj=" + object + "].");
            object.removeSubscriber(this);
        }
    }

    /**
     * Called when the client session is first started. The client object
     * has been created at this point and after this method is executed,
     * the bootstrap information will be sent to the client which will
     * trigger the start of the session. Derived classes that override
     * this method should be sure to call
     * <code>super.sessionWillStart</code>.
     *
     * <p><em>Note:</em> This function will be called on the dobjmgr
     * thread which means that object manipulations are OK, but client
     * instance manipulations must done carefully.
     */
    protected void sessionWillStart ()
    {
    }

    /**
     * Called when the client resumes a session (after having disconnected
     * and reconnected). After this method is executed, the bootstrap
     * information will be sent to the client which will trigger the
     * resumption of the session. Derived classes that override this
     * method should be sure to call <code>super.sessionWillResume</code>.
     *
     * <p><em>Note:</em> This function will be called on the dobjmgr
     * thread which means that object manipulations are OK, but client
     * instance manipulations must done carefully.
     */
    protected void sessionWillResume ()
    {
    }

    /**
     * Called when the client session ends (either because the client
     * logged off or because the server forcibly terminated the session).
     * Derived classes that override this method should be sure to call
     * <code>super.sessionDidTerminate</code>.
     *
     * <p><em>Note:</em> This function will be called on the dobjmgr
     * thread which means that object manipulations are OK, but client
     * instance manipulations must done carefully.
     */
    protected void sessionDidTerminate ()
    {
        // destroy the client object
        CherServer.omgr.destroyObject(_clobj.getOid());
    }

    /**
     * This is called once we have a handle on the client distributed
     * object. It sends a bootstrap notification to the client with all
     * the information it will need to interact with the server.
     */
    protected void sendBootstrap ()
    {
        Log.info("Sending bootstrap [client=" + this + "].");

        // create and populate our bootstrap data
        BootstrapData data = createBootstrapData();
        populateBootstrapData(data);

        // create a send bootstrap notification
        _conn.postMessage(new BootstrapNotification(data));
    }

    /**
     * Derived client classes can override this member to create derived
     * bootstrap data classes that contain extra bootstrap information, if
     * desired.
     */
    protected BootstrapData createBootstrapData ()
    {
        return new BootstrapData();
    }

    /**
     * Derived client classes can override this member to populate the
     * bootstrap data with additional information. They should be sure to
     * call <code>super.populateBootstrapData</code> before doing their
     * own populating, however.
     *
     * <p><em>Note:</em> This function will be called on the dobjmgr
     * thread which means that object manipulations are OK, but client
     * instance manipulations must done carefully.
     */
    protected void populateBootstrapData (BootstrapData data)
    {
        // give them the client object id
        data.clientOid = _clobj.getOid();

        // give them the invocation oid
        data.invOid = CherServer.invmgr.getOid();
    }

    /**
     * Called by the connection manager when this client's connection is
     * unmapped. That may be because of a connection failure (in which
     * case this call will be followed up by a call to
     * <code>connectionFailed</code>) or it may be because of an orderly
     * closing of the connection. In either case, the client can deal with
     * its lack of a connection in this method. This is invoked by the
     * conmgr thread and should behave accordingly.
     */
    protected void wasUnmapped ()
    {
        // clear out our connection reference
        setConnection(null);

        // clear out our subscriptions. we need to do this on the dobjmgr
        // thread. so we do so by posting a special event. it is important
        // that we do this *after* we clear out our connection reference.
        // once the connection ref is null, no more subscriptions will be
        // processed (even those that were queued up before the connection
        // went away)
        DEvent event = new DEvent(0)
        {
            public boolean applyToObject (DObject target)
            {
                clearSubscrips();
                return false;
            }
        };
        CherServer.omgr.postEvent(event);
    }

    /**
     * Called by the connection manager when this client's connection
     * fails. This is invoked on the conmgr thread and should behave
     * accordingly.
     */
    protected void connectionFailed (IOException fault)
    {
        // nothing to do here presently. the client manager already
        // complained about the failed connection
    }

    /**
     * Sets our connection reference in a thread safe way. Also
     * establishes the back reference to us as the connection's message
     * handler.
     */
    protected synchronized void setConnection (Connection conn)
    {
        // keep a handle to the new connection
        _conn = conn;

        // tell the connection to pass messages on to us (if we're setting
        // a connection rather than clearing one out)
        if (_conn != null) {
            _conn.setMessageHandler(this);
        }
    }

    /**
     * The connection instance must be accessed via this member function
     * because it is read from both the dobjmgr and conmgr threads and is
     * modified by the conmgr thread.
     *
     * @return The connection instance associated with this client or null
     * if the client is not currently connected.
     */
    protected synchronized Connection getConnection ()
    {
        return _conn;
    }

    // documentation inherited from interface
    public void handleMessage (UpstreamMessage message)
    {
        // we dispatch to a message dispatcher that is specialized for the
        // particular class of message that we received
        MessageDispatcher disp = (MessageDispatcher)
            _disps.get(message.getClass());
        if (disp == null) {
            Log.warning("No dispacther for message [msg=" + message + "].");
            return;
        }

        // otherwise pass the message on to the dispatcher
        disp.dispatch(this, message);
    }

    // documentation inherited from interface
    public void objectAvailable (DObject object)
    {
        // queue up an object response
        Connection conn = getConnection();
        if (conn != null) {
            // pass the successful subscrip on to the client
            conn.postMessage(new ObjectResponse(object));
            // make a note of this new subscription
            mapSubscrip(object);

        } else {
            Log.info("Dropped object available notification " +
                     "[client=" + this + ", oid=" + object.getOid() + "].");
        }
    }

    // documentation inherited from interface
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        Connection conn = getConnection();
        if (conn != null) {
            conn.postMessage(new FailureResponse(oid));
        } else {
            Log.info("Dropped failure notification " +
                     "[client=" + this + ", oid=" + oid +
                     ", cause=" + cause + "].");
        }
    }

    // documentation inherited from interface
    public boolean handleEvent (DEvent event, DObject target)
    {
        // forward the event to the client
        Connection conn = getConnection();
        if (conn != null) {
            // only typed events will be forwarded to the client, so we
            // need not worry that a non-typed event would make it here
            TypedEvent tevent = (TypedEvent)event;
            conn.postMessage(new EventNotification(tevent));
        } else {
            Log.info("Dropped event forward notification " +
                     "[client=" + this + ", event=" + event + "].");
        }

        return true;
    }

    public String toString ()
    {
        return "[username=" + _username + ", conn=" + _conn +
            ", cloid=" + ((_clobj == null) ? -1 : _clobj.getOid()) + "]";
    }

    /**
     * Message dispatchers are used to dispatch each different type of
     * upstream message. We can look the dispatcher up in a table and
     * invoke it through an overloaded member which is faster (so we
     * think) than doing a bunch of instanceofs.
     */
    protected static interface MessageDispatcher
    {
        /**
         * Dispatch the supplied message for the specified client.
         */
        public void dispatch (CherClient client, UpstreamMessage mge);
    }

    /**
     * Processes subscribe requests.
     */
    protected static class SubscribeDispatcher implements MessageDispatcher
    {
        public void dispatch (CherClient client, UpstreamMessage msg)
        {
            SubscribeRequest req = (SubscribeRequest)msg;
            // Log.info("Subscribing [client=" + client +
            // ", oid=" + req.getOid() + "].");

            // forward the subscribe request to the omgr for processing
            CherServer.omgr.subscribeToObject(req.getOid(), client);
        }
    }

    /**
     * Processes unsubscribe requests.
     */
    protected static class UnsubscribeDispatcher implements MessageDispatcher
    {
        public void dispatch (CherClient client, UpstreamMessage msg)
        {
            UnsubscribeRequest req = (UnsubscribeRequest)msg;
            // Log.info("Unsubscribing [client=" + client +
            // ", oid=" + req.getOid() + "].");

            // forward the unsubscribe request to the omgr for processing
            CherServer.omgr.unsubscribeFromObject(req.getOid(), client);
            // update our subscription tracking table
            client.unmapSubscrip(req.getOid());
        }
    }

    /**
     * Processes forward event requests.
     */
    protected static class ForwardEventDispatcher implements MessageDispatcher
    {
        public void dispatch (CherClient client, UpstreamMessage msg)
        {
            ForwardEventRequest req = (ForwardEventRequest)msg;
            DEvent fevt = req.getEvent();

            // fill in the proper source oid
            fevt.setSourceOid(client.getClientObject().getOid());

            // Log.info("Forwarding event [client=" + client +
            // ", event=" + fevt + "].");

            // forward the event to the omgr for processing
            CherServer.omgr.postEvent(fevt);
        }
    }

    /**
     * Processes ping requests.
     */
    protected static class PingDispatcher implements MessageDispatcher
    {
        public void dispatch (CherClient client, UpstreamMessage msg)
        {
            Log.info("Received client ping [client=" + client + "].");
            // send a pong response
            Connection conn = client.getConnection();
            if (conn != null) {
                conn.postMessage(new PongResponse());
            } else {
                Log.info("Dropped pong response [client=" + client + "].");
            }
        }
    }

    /**
     * Processes logoff requests.
     */
    protected static class LogoffDispatcher implements MessageDispatcher
    {
        public void dispatch (CherClient client, UpstreamMessage msg)
        {
            Log.info("Client requested logoff " +
                     "[client=" + client + "].");
            // request that the client end the session
            client.endSession();
        }
    }

    protected ClientManager _cmgr;
    protected String _username;
    protected Connection _conn;
    protected ClientObject _clobj;
    protected IntMap _subscrips = new IntMap();

    protected static HashMap _disps = new HashMap();

    // register our message dispatchers
    static {
        _disps.put(SubscribeRequest.class, new SubscribeDispatcher());
        _disps.put(UnsubscribeRequest.class, new UnsubscribeDispatcher());
        _disps.put(ForwardEventRequest.class, new ForwardEventDispatcher());
        _disps.put(PingRequest.class, new PingDispatcher());
        _disps.put(LogoffRequest.class, new LogoffDispatcher());
    }
}
