//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

/**
 * This class encapsulates a message in the distributed object protocol that flows from the server
 * to the client. Downstream messages include object subscription, event forwarding and session
 * management.
 */
public abstract class DownstreamMessage extends Message
{
    /**
     * The message id of the upstream message with which this downstream message is associated (or
     * -1 if it is not associated with any upstream message).
     */
    public short messageId = -1;

    @Override
    public String toString ()
    {
        return "[msgid=" + messageId + "]";
    }
}
