//
// $Id: RunningConnection.java,v 1.7 2002/09/24 00:50:27 mdb Exp $

package com.threerings.presents.server.net;

import java.io.IOException;
import ninja2.core.io_core.nbio.NonblockingSocket;
import com.samskivert.util.StringUtil;

import com.threerings.presents.net.UpstreamMessage;

/**
 * A running connection object represents a fully operational client
 * connection to the server.
 */
public class RunningConnection extends Connection
{
    /**
     * Constructs a new running connection object to manage the supplied
     * client socket.
     */
    public RunningConnection (ConnectionManager cmgr,
                              NonblockingSocket socket)
        throws IOException
    {
        super(cmgr, socket);
    }

    /**
     * Called when a new message has arrived from the client.
     */
    public void handleMessage (UpstreamMessage msg)
    {
    }

    public String toString ()
    {
        if (_socket != null) {
            return "[mode=RUNNING, id=" + (hashCode() % 1000) +
                ", addr=" + StringUtil.toString(_socket.getInetAddress()) + "]";
        } else {
            return "[mode=RUNNING, id=" + (hashCode() % 1000) +
                ", addr=<disconnected>]";
        }
    }
}
