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

package com.threerings.presents.client;

import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.UpstreamMessage;

/**
 * Used to listen to low-level message handling for the purpose of statistics tracking.  Methods
 * must be thread-safe.
 */
public interface MessageTracker
{
    /**
     * An implementation of the interface that does nothing.
     */
    public static final MessageTracker NOOP = new MessageTracker() {
        public void messageSent (boolean datagram, int size, UpstreamMessage msg) {
        }
        public void messageReceived (
            boolean datagram, int size, DownstreamMessage msg, int missed) {
        }
    };

    /**
     * Notes that a message has been sent.
     */
    public void messageSent (boolean datagram, int size, UpstreamMessage msg);

    /**
     * Notes that a message has been received.
     *
     * @param msg the received message, or <code>null</code> if received out of order.
     * @param missed the number of messages missed between this message and the one before it.
     */
    public void messageReceived (boolean datagram, int size, DownstreamMessage msg, int missed);
}
