//
// $Id: RunningConnection.java,v 1.3 2001/06/02 01:30:37 mdb Exp $

package com.threerings.cocktail.cher.server.net;

import java.io.IOException;
import ninja2.core.io_core.nbio.NonblockingSocket;
import com.threerings.cocktail.cher.net.UpstreamMessage;

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
        return "[mode=RUNNING, addr=" + _socket.getInetAddress() + "]";
    }
}
