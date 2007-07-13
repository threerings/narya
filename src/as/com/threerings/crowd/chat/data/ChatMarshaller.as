//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.chat.data {

import flash.utils.ByteArray;
import com.threerings.util.*; // for Float, Integer, etc.
import com.threerings.io.TypedArray;

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.client.ChatService_TellListener;
import com.threerings.crowd.chat.data.ChatMarshaller_TellMarshaller;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService_InvocationListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;
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
    /** The method id used to dispatch {@link #away} requests. */
    public static const AWAY :int = 1;

    // from interface ChatService
    public function away (arg1 :Client, arg2 :String) :void
    {
        sendRequest(arg1, AWAY, [
            arg2
        ]);
    }

    /** The method id used to dispatch {@link #broadcast} requests. */
    public static const BROADCAST :int = 2;

    // from interface ChatService
    public function broadcast (arg1 :Client, arg2 :String, arg3 :InvocationService_InvocationListener) :void
    {
        var listener3 :InvocationMarshaller_ListenerMarshaller = new InvocationMarshaller_ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, BROADCAST, [
            arg2, listener3
        ]);
    }

    /** The method id used to dispatch {@link #tell} requests. */
    public static const TELL :int = 3;

    // from interface ChatService
    public function tell (arg1 :Client, arg2 :Name, arg3 :String, arg4 :ChatService_TellListener) :void
    {
        var listener4 :ChatMarshaller_TellMarshaller = new ChatMarshaller_TellMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, TELL, [
            arg2, arg3, listener4
        ]);
    }
}
}
