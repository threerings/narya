//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
