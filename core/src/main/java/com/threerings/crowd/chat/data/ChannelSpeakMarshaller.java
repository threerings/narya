//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.data;

import javax.annotation.Generated;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.crowd.chat.client.ChannelSpeakService;

/**
 * Provides the implementation of the {@link ChannelSpeakService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from ChannelSpeakService.java.")
public class ChannelSpeakMarshaller extends InvocationMarshaller<ClientObject>
    implements ChannelSpeakService
{
    /** The method id used to dispatch {@link #speak} requests. */
    public static final int SPEAK = 1;

    // from interface ChannelSpeakService
    public void speak (ChatChannel arg1, String arg2, byte arg3)
    {
        sendRequest(SPEAK, new Object[] {
            arg1, arg2, Byte.valueOf(arg3)
        });
    }
}
