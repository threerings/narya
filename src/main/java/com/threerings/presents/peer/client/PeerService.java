//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.presents.peer.client;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.peer.data.NodeObject.Lock;

/**
 * Defines requests made from one peer to another.
 */
public interface PeerService extends InvocationService<ClientObject>
{
    /**
     * Informs the node that the sending peer ratifies its acquisition or release of the specified
     * lock.
     */
    void ratifyLockAction (Lock lock, boolean acquire);

    /**
     * Requests that the specified action be invoked on this server.
     */
    void invokeAction (byte[] serializedAction);

    /**
     * Requests that the specified request be invoked on this server and wants a confirmation
     *  when it's complete.
     */
    void invokeRequest (byte[] serializedAction, ResultListener listener);

    /**
     * Generates a server status report for this peer and returns it to the supplied listener. The
     * result must be a string.
     *
     * @param type the type of report to generate. See ReportManager for more information.
     */
    void generateReport (String type, ResultListener listener);
}
