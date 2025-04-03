//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

/**
 * A client observer is a more detailed version of the {@link SessionObserver} for entities that
 * are interested in more detail about the logon/logoff process.
 *
 * <p> In the normal course of affairs, {@link SessionObserver#clientDidLogon} will be called after
 * the client successfully logs on to the server and {@link SessionObserver#clientDidLogoff} will
 * be called after the client logs off of the server. If logon fails for any reason,
 * {@link #clientFailedToLogon} will be called to explain the failure.
 *
 * <p> {@link #clientWillLogoff} will only be called when an abortable logoff is requested (like
 * when the user clicks on a logoff button of some sort). It will not be called during
 * non-abortable logoff requests (like when the browser calls stop on the applet and is about to
 * yank the rug out from under us). If an observer aborts the logoff request, it should notify the
 * user in some way why the request was aborted (<em>but it shouldn't do so on the thread that
 * calls {@link #clientWillLogoff}</em>).
 *
 * <p> If the client connection fails unexpectedly, {@link #clientConnectionFailed} will be called
 * to let the observers know that we lost our connection to the server.
 * {@link SessionObserver#clientDidLogoff} will be called immediately afterwards as a normal logoff
 * procedure is effected.
 */
public interface ClientObserver extends SessionObserver
{
    /**
     * Called if anything fails during the logon attempt. This could be a network failure,
     * authentication failure or otherwise. The exception provided will indicate the cause of the
     * failure.
     *
     * @param cause an exception indicating the cause of the logon failure.  <em>Note:</em> this
     * may be a {@link LogonException} and if so, the caller <em>must</em> check {@link
     * LogonException#isStillInProgress} to find out if the logon process has totally failed or if
     * we are simply reporting intermediate status (we might be falling back to an alternative port
     * or delaying our auto-retry attempt due to server overload).
     */
    void clientFailedToLogon (Client client, Exception cause);

    /**
     * Called when the connection to the server went away for some unexpected reason. This will be
     * followed by a call to {@link SessionObserver#clientDidLogoff}.
     */
    void clientConnectionFailed (Client client, Exception cause);

    /**
     * Called when an abortable logoff request is made. If the observer returns false from this
     * method, the client will abort the logoff request.
     */
    boolean clientWillLogoff (Client client);

    /**
     * Called after the client is completely logged off from a successful session and is ready to
     * reconnect to a new server if desired. This will only be called after an active session was
     * terminated, not after a logon attempt failed as that failure will be reported by {@link
     * #clientFailedToLogon}.
     */
    void clientDidClear (Client client);
}
