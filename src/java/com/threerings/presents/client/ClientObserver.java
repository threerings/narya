//
// $Id: ClientObserver.java,v 1.9 2004/08/27 02:20:18 mdb Exp $
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
