//
// $Id: PresentsClient.java,v 1.1 2001/06/02 01:30:37 mdb Exp $

package com.threerings.cocktail.cher.server;

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
    public Client (String username, Connection conn)
    {
        _username = username;
        setConnection(conn);
    }

    /**
     * Called by the client manager when a new connection arrives that
     * authenticates as this already established client. This must only be
     * called from the congmr thread.
     */
    public void resumeSession (Connection conn)
    {
        // check to see if we've already got a connection object, in which
        // case it's probably stale
        if (_conn != null) {
            Log.info("Closing stale connection [old=" + _conn +
                     ", new=" + conn + "].");
            // close the old connection (which results in everything being
            // properly unregistered)
            _conn.close();
        }

        // start using the new connection
        setConnection(conn);

        Log.info("Session resumed [client=" + this + "].");
    }

    protected void setConnection (Connection conn)
    {
        // keep a handle to the new connection
        _conn = conn;

        // tell the connection to pass messages on to us
        _conn.setMessageHandler(this);
    }

    // documentation inherited from interface
    public void handleMessage (UpstreamMessage message)
    {
    }

    // documentation inherited from interface
    public void objectAvailable (DObject object)
    {
        // queue up an object response
        _conn.postMessage(new ObjectResponse(object));
    }

    // documentation inherited from interface
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        _conn.postMessage(new FailureResponse(oid));
    }

    // documentation inherited from interface
    public boolean handleEvent (DEvent event, DObject target)
    {
        // forward the event to the client
        _conn.postMessage(new EventNotification(event));
        return true;
    }

    protected String _username;
    protected transient Connection _conn;
}
