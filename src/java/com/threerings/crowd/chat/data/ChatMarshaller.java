//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.chat.data;

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.util.Name;

/**
 * Provides the implementation of the {@link ChatService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class ChatMarshaller extends InvocationMarshaller
    implements ChatService
{
    // documentation inherited
    public static class TellMarshaller extends ListenerMarshaller
        implements TellListener
    {
        /** The method id used to dispatch {@link #tellSucceeded}
         * responses. */
        public static final int TELL_SUCCEEDED = 1;

        // documentation inherited from interface
        public void tellSucceeded (long arg1, String arg2)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, TELL_SUCCEEDED,
                               new Object[] { new Long(arg1), arg2 }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case TELL_SUCCEEDED:
                ((TellListener)listener).tellSucceeded(
                    ((Long)args[0]).longValue(), (String)args[1]);
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /** The method id used to dispatch {@link #tell} requests. */
    public static final int TELL = 1;

    // documentation inherited from interface
    public void tell (Client arg1, Name arg2, String arg3, ChatService.TellListener arg4)
    {
        ChatMarshaller.TellMarshaller listener4 = new ChatMarshaller.TellMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, TELL, new Object[] {
            arg2, arg3, listener4
        });
    }

    /** The method id used to dispatch {@link #broadcast} requests. */
    public static final int BROADCAST = 2;

    // documentation inherited from interface
    public void broadcast (Client arg1, String arg2, InvocationService.InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, BROADCAST, new Object[] {
            arg2, listener3
        });
    }

    /** The method id used to dispatch {@link #away} requests. */
    public static final int AWAY = 3;

    // documentation inherited from interface
    public void away (Client arg1, String arg2)
    {
        sendRequest(arg1, AWAY, new Object[] {
            arg2
        });
    }

}
