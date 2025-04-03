//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.peer.data;

import javax.annotation.Generated;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.data.ChatMarshaller;
import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.peer.client.CrowdPeerService;

/**
 * Provides the implementation of the {@link CrowdPeerService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from CrowdPeerService.java.")
public class CrowdPeerMarshaller extends InvocationMarshaller<ClientObject>
    implements CrowdPeerService
{
    /** The method id used to dispatch {@link #deliverBroadcast} requests. */
    public static final int DELIVER_BROADCAST = 1;

    // from interface CrowdPeerService
    public void deliverBroadcast (Name arg1, byte arg2, String arg3, String arg4)
    {
        sendRequest(DELIVER_BROADCAST, new Object[] {
            arg1, Byte.valueOf(arg2), arg3, arg4
        });
    }

    /** The method id used to dispatch {@link #deliverTell} requests. */
    public static final int DELIVER_TELL = 2;

    // from interface CrowdPeerService
    public void deliverTell (UserMessage arg1, Name arg2, ChatService.TellListener arg3)
    {
        ChatMarshaller.TellMarshaller listener3 = new ChatMarshaller.TellMarshaller();
        listener3.listener = arg3;
        sendRequest(DELIVER_TELL, new Object[] {
            arg1, arg2, listener3
        });
    }
}
