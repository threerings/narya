//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

/**
 * Notifies the server that we would like it to send us datagrams.
 */
public class TransmitDatagramsRequest extends UpstreamMessage
{
    @Override
    public String toString ()
    {
        return "[type=TRANSMIT_DATAGRAMS, msgid=" + messageId + "]";
    }
}
