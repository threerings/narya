//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.client;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;

import com.threerings.crowd.chat.data.ChatChannel;

/**
 * Provides a way for clients to speak on chat channels.
 */
public interface ChannelSpeakService extends InvocationService<ClientObject>
{
    /**
     * Requests to speak the supplied message on the specified channel.
     */
    public void speak (ChatChannel channel, String message, byte mode);
}
