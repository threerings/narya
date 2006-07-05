//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.peer.data;

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.data.ChatMarshaller;
import com.threerings.crowd.peer.client.CrowdPeerService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.util.Name;

/**
 * Provides the implementation of the {@link CrowdPeerService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class CrowdPeerMarshaller extends InvocationMarshaller
    implements CrowdPeerService
{
    /** The method id used to dispatch {@link #deliverTell} requests. */
    public static final int DELIVER_TELL = 1;

    // documentation inherited from interface
    public void deliverTell (Client arg1, Name arg2, Name arg3, String arg4, ChatService.TellListener arg5)
    {
        ChatMarshaller.TellMarshaller listener5 = new ChatMarshaller.TellMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, DELIVER_TELL, new Object[] {
            arg2, arg3, arg4, listener5
        });
    }

}
