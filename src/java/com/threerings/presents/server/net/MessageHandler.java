//
// $Id: MessageHandler.java,v 1.2 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.server.net;

import com.threerings.presents.net.UpstreamMessage;

/**
 * After the connection object has parsed an entire upstream message, it
 * passes it on to its message handler.
 */
public interface MessageHandler
{
    /**
     * Called when a complete message has been parsed from incoming
     * network data.
     */
    public void handleMessage (UpstreamMessage message);
}
