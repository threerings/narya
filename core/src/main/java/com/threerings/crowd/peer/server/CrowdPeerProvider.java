//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.peer.server;

import javax.annotation.Generated;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.peer.client.CrowdPeerService;

/**
 * Defines the server-side of the {@link CrowdPeerService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from CrowdPeerService.java.")
public interface CrowdPeerProvider extends InvocationProvider
{
    /**
     * Handles a {@link CrowdPeerService#deliverBroadcast} request.
     */
    void deliverBroadcast (ClientObject caller, Name arg1, byte arg2, String arg3, String arg4);

    /**
     * Handles a {@link CrowdPeerService#deliverTell} request.
     */
    void deliverTell (ClientObject caller, UserMessage arg1, Name arg2, ChatService.TellListener arg3)
        throws InvocationException;
}
