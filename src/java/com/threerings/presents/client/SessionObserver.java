//
// $Id: SessionObserver.java,v 1.2 2002/09/20 00:54:39 mdb Exp $

package com.threerings.presents.client;

/**
 * A session observer is registered with the client instance to be
 * notified when the client establishes and ends their session with the
 * server.
 *
 * <p> These callbacks happen on the client thread and should therefore
 * not be used to perform any complex action or do much more than relay
 * the signal to some other thread (like the AWT thread) to act more fully
 * on the notice.
 *
 * @see ClientObserver
 */
public interface SessionObserver
{
    /**
     * Called after the client successfully connected to and authenticated
     * with the server. The entire object system is up and running by the
     * time this method is called.
     */
    public void clientDidLogon (Client client);

    /**
     * For systems that allow switching screen names after logon, this
     * method is called whenever a screen name change takes place to
     * report that the client object has been replaced to potential
     * client-side subscribers.
     */
    public void clientObjectDidChange (Client client);

    /**
     * Called after the client has been logged off of the server and has
     * disconnected.
     */
    public void clientDidLogoff (Client client);
}
