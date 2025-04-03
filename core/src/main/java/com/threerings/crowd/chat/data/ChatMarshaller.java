//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.data;

import javax.annotation.Generated;

import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.crowd.chat.client.ChatService;

/**
 * Provides the implementation of the {@link ChatService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from ChatService.java.")
public class ChatMarshaller extends InvocationMarshaller<ClientObject>
    implements ChatService
{
    /**
     * Marshalls results to implementations of {@code ChatService.TellListener}.
     */
    public static class TellMarshaller extends ListenerMarshaller
        implements TellListener
    {
        /** The method id used to dispatch {@link #tellSucceeded}
         * responses. */
        public static final int TELL_SUCCEEDED = 1;

        // from interface TellMarshaller
        public void tellSucceeded (long arg1, String arg2)
        {
            sendResponse(TELL_SUCCEEDED, new Object[] { Long.valueOf(arg1), arg2 });
        }

        @Override // from InvocationMarshaller
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case TELL_SUCCEEDED:
                ((TellListener)listener).tellSucceeded(
                    ((Long)args[0]).longValue(), (String)args[1]);
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    /** The method id used to dispatch {@link #away} requests. */
    public static final int AWAY = 1;

    // from interface ChatService
    public void away (String arg1)
    {
        sendRequest(AWAY, new Object[] {
            arg1
        });
    }

    /** The method id used to dispatch {@link #broadcast} requests. */
    public static final int BROADCAST = 2;

    // from interface ChatService
    public void broadcast (String arg1, InvocationService.InvocationListener arg2)
    {
        ListenerMarshaller listener2 = new ListenerMarshaller();
        listener2.listener = arg2;
        sendRequest(BROADCAST, new Object[] {
            arg1, listener2
        });
    }

    /** The method id used to dispatch {@link #tell} requests. */
    public static final int TELL = 3;

    // from interface ChatService
    public void tell (Name arg1, String arg2, ChatService.TellListener arg3)
    {
        ChatMarshaller.TellMarshaller listener3 = new ChatMarshaller.TellMarshaller();
        listener3.listener = arg3;
        sendRequest(TELL, new Object[] {
            arg1, arg2, listener3
        });
    }
}
