//
// $Id: MessageHandler.java,v 1.1 2001/06/02 01:30:37 mdb Exp $

package com.threerings.cocktail.cher.server.net;

import com.threerings.cocktail.cher.net.UpstreamMessage;

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
