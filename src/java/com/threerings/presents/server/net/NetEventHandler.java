//
// $Id: NetEventHandler.java,v 1.6 2002/11/18 18:53:10 mdb Exp $

package com.threerings.presents.server.net;

/**
 * When a network event occurs, the connection manager calls the net event
 * handler associated with that channel to process the event. There are
 * only a few handlers (and probably only ever will be): the one that
 * accepts new connections, the one that deals with a connection while the
 * client is authenticating and the one that processes messages from
 * authenticated connections.
 *
 * <p> Utilising this interface prevents us from having to do a bunch of
 * inefficient and ugly comparisons; instead we can simply call through an
 * interface method to the proper code.
 */
public interface NetEventHandler
{
    /**
     * Called when a network event has occurred on this handler's source.
     *
     * @return the number of bytes read from the network as a result of
     * handling this event.
     */
    public int handleEvent (long when);

    /**
     * Called to ensure that this channel has not been idle for longer
     * than is possible in happily operating circumstances.
     *
     * @return true if the handler is idle (in which case it will be
     * closed shortly), false if it is not.
     */
    public boolean checkIdle (long now);
}
