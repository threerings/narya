//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.peer.client;

import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.data.UserMessage;

/**
 * Bridges certain Crowd services between peers in a cluster configuration.
 */
public interface CrowdPeerService extends InvocationService<ClientObject>
{
    /**
     * Used to forward a tell request to the server on which the destination user actually
     * occupies.
     */
    void deliverTell (UserMessage message, Name target,
                      ChatService.TellListener listener);

    /**
     * Dispatches a broadcast message on this peer.
     */
    void deliverBroadcast (Name from, byte levelOrMode, String bundle, String msg);
}
