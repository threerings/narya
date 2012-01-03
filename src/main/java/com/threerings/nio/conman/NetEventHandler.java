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

package com.threerings.nio.conman;

/**
 * When a network event occurs, the connection manager calls the net event handler associated with
 * that channel to process the event. There are only a few handlers (and probably only ever will
 * be): the one that accepts new connections, the one that deals with a connection while the client
 * is authenticating and the one that processes messages from authenticated connections.
 *
 * <p> Using this interface prevents us from having to do a bunch of inefficient and ugly
 * comparisons; instead we can call through an interface method to the proper code.
 */
public interface NetEventHandler
{
    /**
     * Called when a network event has occurred on this handler's source.
     *
     * @return the number of bytes read from the network as a result of handling this event.
     */
    int handleEvent (long when);

    /**
     * Called to ensure that this channel has not been idle for longer than is possible in happily
     * operating circumstances.
     *
     * @param idleStamp if the handler's last event occurred more recently than this timestamp, it
     * should return false, otherwise true.
     *
     * @return true if the handler is idle (in which case it will be closed shortly), false if it
     * is not.
     */
    boolean checkIdle (long idleStamp);

    /**
     * Called if the handler is deemed to be idle. Should shutdown any associated connection and
     * remove any registrations with the connection manager.
     */
    void becameIdle ();
}
