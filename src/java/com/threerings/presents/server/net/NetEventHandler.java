//
// $Id: NetEventHandler.java,v 1.4 2002/10/29 23:51:26 mdb Exp $

package com.threerings.presents.server.net;

import ninja2.core.io_core.nbio.Selectable;

/**
 * When a network event arrives on a particular <code>Selectable</code>,
 * the connection manager calls the net event handler associated with that
 * selectable to process the event. There are only a few handlers (and
 * probably only ever will be): the one that accepts new connections, the
 * one that deals with a connection while the client is authenticating and
 * the one that processes messages from authenticated connections.
 * Providing this interface prevents us from having to do a bunch of
 * inefficient and ugly comparisons; instead we can simply call through an
 * interface method to the proper code.
 */
public interface NetEventHandler
{
    /**
     * Called when a network event has occurred on the supplied source.
     * The <code>events</code> parameter indicates which event or events
     * have occurred.
     */
    public void handleEvent (long when, Selectable source, short events);

    /**
     * Called to ensure that this selectable has not been idle for longer
     * than is possible in happily operating circumstances.
     */
    public void checkIdle (long now);
}
