//
// $Id: RunningConnection.java,v 1.9 2002/11/18 18:53:10 mdb Exp $

package com.threerings.presents.server.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

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
    public RunningConnection (ConnectionManager cmgr, SelectionKey selkey,
                              SocketChannel channel, long createStamp)
        throws IOException
    {
        super(cmgr, selkey, channel, createStamp);
    }

    /**
     * Called when a new message has arrived from the client.
     */
    public void handleMessage (UpstreamMessage msg)
    {
    }

    public String toString ()
    {
        String addr = (_channel == null) ? "<disconnected>" :
            StringUtil.toString(_channel.socket().getInetAddress());
        return "[mode=RUNNING, id=" + (hashCode() % 1000) +
            ", addr=" + addr + "]";
    }
}
