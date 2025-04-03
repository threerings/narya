//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.server;

import javax.annotation.Generated;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.chat.client.ChannelSpeakService;
import com.threerings.crowd.chat.data.ChatChannel;

/**
 * Defines the server-side of the {@link ChannelSpeakService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from ChannelSpeakService.java.")
public interface ChannelSpeakProvider extends InvocationProvider
{
    /**
     * Handles a {@link ChannelSpeakService#speak} request.
     */
    void speak (ClientObject caller, ChatChannel arg1, String arg2, byte arg3);
}
