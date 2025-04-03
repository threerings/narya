//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

/**
 * A session observer is registered with the client instance to be notified when the client
 * establishes and ends their session with the server.
 *
 * @see ClientObserver
 */
public interface SessionObserver
{
    /**
     * Called immediately before a logon is attempted.
     */
    void clientWillLogon (Client client);

    /**
     * Called after the client successfully connected to and authenticated with the server. The
     * entire object system is up and running by the time this method is called.
     */
    void clientDidLogon (Client client);

    /**
     * For systems that allow switching screen names after logon, this method is called whenever a
     * screen name change takes place to report that the client object has been replaced to
     * potential client-side subscribers.
     */
    void clientObjectDidChange (Client client);

    /**
     * Called after the client has been logged off of the server and has disconnected.
     */
    void clientDidLogoff (Client client);
}
