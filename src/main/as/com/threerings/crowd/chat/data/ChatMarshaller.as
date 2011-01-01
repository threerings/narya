//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.crowd.chat.data {

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.client.ChatService_TellListener;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService_InvocationListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;
import com.threerings.util.Name;

/**
 * Provides the implementation of the <code>ChatService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class ChatMarshaller extends InvocationMarshaller
    implements ChatService
{
    /** The method id used to dispatch <code>away</code> requests. */
    public static const AWAY :int = 1;

    // from interface ChatService
    public function away (arg1 :String) :void
    {
        sendRequest(AWAY, [
            arg1
        ]);
    }

    /** The method id used to dispatch <code>broadcast</code> requests. */
    public static const BROADCAST :int = 2;

    // from interface ChatService
    public function broadcast (arg1 :String, arg2 :InvocationService_InvocationListener) :void
    {
        var listener2 :InvocationMarshaller_ListenerMarshaller = new InvocationMarshaller_ListenerMarshaller();
        listener2.listener = arg2;
        sendRequest(BROADCAST, [
            arg1, listener2
        ]);
    }

    /** The method id used to dispatch <code>tell</code> requests. */
    public static const TELL :int = 3;

    // from interface ChatService
    public function tell (arg1 :Name, arg2 :String, arg3 :ChatService_TellListener) :void
    {
        var listener3 :ChatMarshaller_TellMarshaller = new ChatMarshaller_TellMarshaller();
        listener3.listener = arg3;
        sendRequest(TELL, [
            arg1, arg2, listener3
        ]);
    }
}
}
