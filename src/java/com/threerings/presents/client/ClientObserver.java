//
// $Id: ClientObserver.java,v 1.7 2002/09/20 00:54:39 mdb Exp $

package com.threerings.presents.client;

/**
 * A client observer is a more detailed version of the {@link
 * SessionObserver} for entities that are interested in more detail about
 * the logon/logoff process.
 *
 * <p> In the normal course of affairs, <code>clientDidLogon</code> will
 * be called after the client successfully logs on to the server and
 * <code>clientDidLogoff</code> will be called after the client logs off
 * of the server. If logon fails for any reson,
 * <code>clientFailedToLogon</code> will be called to explain the failure.
 *
 * <p> <code>clientWillLogoff</code> will only be called when an abortable
 * logoff is requested (like when the user clicks on a logoff button of
 * some sort). It will not be called during non-abortable logoff requests
 * (like when the browser calls stop on the applet and is about to yank
 * the rug out from under us). If an observer aborts the logoff request,
 * it should notify the user in some way why the request was aborted
 * (<em>but it shouldn't do so on the thread that calls
 * <code>clientWillLogoff</code></em>).
 *
 * <p> If the client connection fails unexpectedly,
 * <code>clientConnectionFailed</code> will be called to let the
 * observers know that we lost our connection to the
 * server. <code>clientDidLogoff</code> will be called immediately
 * afterwards as a normal logoff procedure is effected.
 *
 * <p> These callbacks happen on the client thread and should therefore
 * not be used to perform any complex action or do much more than relay
 * the signal to some other thread (like the AWT thread) to act more fully
 * on the notice.
 */
public interface ClientObserver extends SessionObserver
{
    /**
     * Called if anything fails during the logon attempt. This could be a
     * network failure, authentication failure or otherwise. The exception
     * provided will indicate the cause of the failure.
     */
    public void clientFailedToLogon (Client client, Exception cause);

    /**
     * Called when the connection to the server went away for some
     * unexpected reason. This will be followed by a call to
     * <code>clientDidLogoff</code>.
     */
    public void clientConnectionFailed (Client client, Exception cause);

    /**
     * Called when an abortable logoff request is made. If the observer
     * returns false from this method, the client will abort the logoff
     * request.
     */
    public boolean clientWillLogoff (Client client);
}
