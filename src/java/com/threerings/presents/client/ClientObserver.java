//
// $Id: ClientObserver.java,v 1.1 2001/05/22 06:07:59 mdb Exp $

package com.samskivert.cocktail.cher.client;

/**
 * A client observer is registered with the client instance to be notified
 * when state changes happen within the client. Specifically, logon
 * sucess/failure and logoff.
 *
 * <p> These callbacks happen on the client thread and should therefore
 * not be used to perform any complex action or do much more than relay
 * the signal to some other thread (like the AWT thread) to act more fully
 * on the notice.
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
 */
public interface ClientObserver
{
    /**
     * Called after the client successfully connected to and authenticated
     * with the server. The entire object system is up and running by the
     * time this method is called.
     */
    public void clientDidLogon (Client client);

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
     * Called when an abortable logoff requrest is made. If the observer
     * returns false from this method, the client will abort the logoff
     * request.
     */
    public boolean clientWillLogoff (Client client);

    /**
     * Called after the client has been logged off of the server and has
     * disconnected.
     */
    public void clientDidLogoff (Client client);
}
