//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Groups messages to be dispatched without triggering the message throttle. Any messages sent to
 * a client while processing an upstream message will be sent back en masse in a
 * CompoundDownstreamMessage.
 */
public class CompoundUpstreamMessage extends UpstreamMessage
{
    public List<UpstreamMessage> msgs = Lists.newArrayList();

    @Override
    public String toString ()
    {
        return "[type=COMPOUND, msgid=" + messageId + "]";
    }
}
