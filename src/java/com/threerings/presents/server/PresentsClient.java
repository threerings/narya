//
// $Id: PresentsClient.java,v 1.2 2001/06/05 22:44:31 mdb Exp $

package com.threerings.cocktail.cher.server;

import java.io.IOException;
import java.util.HashMap;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.cher.net.*;
import com.threerings.cocktail.cher.server.net.*;

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
public class Client implements Subscriber, MessageHandler
{
    /**
     * Constructs a new client instance bound to the specified username
     * and initially associated with the specified connection instance.
     */
    public Client (ClientManager cmgr, String username, Connection conn)
    {
        _cmgr = cmgr;
        _username = username;
        setConnection(conn);
    }

    /**
     * Returns the username with which this client instance is associated.
     */
    public String getUsername ()
    {
        return _username;
    }

    /**
     * Called by the client manager when a new connection arrives that
     * authenticates as this already established client. This must only be
     * called from the congmr thread.
     */
    public void resumeSession (Connection conn)
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

        Log.info("Session resumed [client=" + this + "].");
    }

    /**
     * Called by the connection manager when this client's connection
     * fails. This is invoked on the conmgr thread, and should behave
     * accordingly.
     */
    public void connectionFailed (IOException fault)
    {
        // clear out our connection reference
        setConnection(null);
    }

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
            conn.postMessage(new ObjectResponse(object));
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
            conn.postMessage(new EventNotification(event));
        } else {
            Log.info("Dropped event forward notification " +
                     "[client=" + this + ", event=" + event + "].");
        }
        return true;
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
        public void dispatch (Client client, UpstreamMessage mge);
    }

    /**
     * Processes subscribe requests.
     */
    protected static class SubscribeDispatcher implements MessageDispatcher
    {
        public void dispatch (Client client, UpstreamMessage msg)
        {
            SubscribeRequest req = (SubscribeRequest)msg;
            Log.info("Subscribing [client=" + client +
                     ", oid=" + req.getOid() + "].");
            // forward the subscribe request to the omgr for processing
            CherServer.omgr.subscribeToObject(req.getOid(), client);
        }
    }

    /**
     * Processes fetch requests.
     */
    protected static class FetchDispatcher implements MessageDispatcher
    {
        public void dispatch (Client client, UpstreamMessage msg)
        {
            FetchRequest req = (FetchRequest)msg;
            Log.info("Fetching [client=" + client +
                     ", oid=" + req.getOid() + "].");
            // forward the fetch request to the omgr for processing
            CherServer.omgr.fetchObject(req.getOid(), client);
        }
    }

    /**
     * Processes unsubscribe requests.
     */
    protected static class UnsubscribeDispatcher implements MessageDispatcher
    {
        public void dispatch (Client client, UpstreamMessage msg)
        {
            UnsubscribeRequest req = (UnsubscribeRequest)msg;
            Log.info("Unsubscribing [client=" + client +
                     ", oid=" + req.getOid() + "].");
            // forward the unsubscribe request to the omgr for processing
            CherServer.omgr.unsubscribeFromObject(req.getOid(), client);
        }
    }

    /**
     * Processes forward event requests.
     */
    protected static class ForwardEventDispatcher implements MessageDispatcher
    {
        public void dispatch (Client client, UpstreamMessage msg)
        {
            ForwardEventRequest req = (ForwardEventRequest)msg;
            Log.info("Forwarding event [client=" + client +
                     ", event=" + req.getEvent() + "].");
            // forward the event to the omgr for processing
            CherServer.omgr.postEvent(req.getEvent());
        }
    }

    /**
     * Processes ping requests.
     */
    protected static class PingDispatcher implements MessageDispatcher
    {
        public void dispatch (Client client, UpstreamMessage msg)
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
        public void dispatch (Client client, UpstreamMessage msg)
        {
            Log.info("Client requested logoff " +
                     "[client=" + client + "].");
            // close our connection (which results in everything being
            // properly unregistered)
            Connection conn = client.getConnection();
            if (conn != null) {
                conn.close();
            } else {
                Log.info("Unable to close connection for logoff request " +
                         "[client=" + client + "].");
            }
            // then let the client manager know what's up
            client._cmgr.clientDidEndSession(client);
        }
    }

    protected ClientManager _cmgr;
    protected String _username;
    protected Connection _conn;

    protected static HashMap _disps = new HashMap();

    // register our message dispatchers
    static {
        _disps.put(SubscribeRequest.class, new SubscribeDispatcher());
        _disps.put(FetchRequest.class, new FetchDispatcher());
        _disps.put(UnsubscribeRequest.class, new UnsubscribeDispatcher());
        _disps.put(ForwardEventRequest.class, new ForwardEventDispatcher());
        _disps.put(PingRequest.class, new PingDispatcher());
        _disps.put(LogoffRequest.class, new LogoffDispatcher());
    }
}
