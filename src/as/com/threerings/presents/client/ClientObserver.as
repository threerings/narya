//
// $Id: ClientObserver.java 3099 2004-08-27 02:21:06Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.client {

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
 */
public interface ClientObserver extends SessionObserver
{
    /**
     * Called if anything fails during the logon attempt. This could be a
     * network failure, authentication failure or otherwise. The exception
     * provided will indicate the cause of the failure.
     *
     * @param cause an exception indicating the cause of the logon failure.
     * <em>Note:</em> this may be a {@link LogonError} and if so, the
     * caller <em>must</em> check {@link LogonException#isStillInProgress} to
     * find out if the logon process has totally failed or if we are simply
     * reporting intermediate status (we might be falling back to an
     * alternative port or delaying our auto-retry attempt due to server
     * overload).
     */
    function clientFailedToLogon (event :ClientEvent) :void;

    /**
     * Called when the connection to the server went away for some
     * unexpected reason. This will be followed by a call to
     * <code>clientDidLogoff</code>.
     */
    function clientConnectionFailed (event :ClientEvent) :void;

    /**
     * Called when an abortable logoff request is made. If the observer
     * calls event.preventDefault() then it will abort the logoff request.
     */
    function clientWillLogoff (event :ClientEvent) :void;

    /**
     * Called after the client is completely logged off from a successful
     * session and is ready to reconnect to a new server if desired. This will
     * only be called after an active session was terminated, not after a logon
     * attempt failed as that failure will be reported by {@link
     * #clientFailedToLogon}.
     */
    function clientDidClear (event :ClientEvent) :void;
}
}
