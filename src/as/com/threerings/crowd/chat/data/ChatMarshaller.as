//
// $Id: ChatMarshaller.java 3793 2005-12-21 02:12:57Z ray $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.client.TellListener;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.ListenerMarshaller;
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
    /** The method id used to dispatch {@link #away} requests. */
    public static const AWAY :int = 1;

    // documentation inherited from interface
    public function away (arg1 :Client, arg2 :String) :void
    {
        sendRequest(arg1, AWAY, [ arg2 ]);
    }

    /** The method id used to dispatch {@link #broadcast} requests. */
    public static const BROADCAST :int = 2;

    // documentation inherited from interface
    public function broadcast (arg1 :Client, arg2 :String, arg3 :InvocationListener) :void
    {
        var listener3 :ListenerMarshaller = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, BROADCAST, [ arg2, listener3 ]);
    }

    /** The method id used to dispatch {@link #tell} requests. */
    public static const TELL :int = 3;

    // documentation inherited from interface
    public function tell (arg1 :Client, arg2 :Name, arg3 :String, arg4 :TellListener) :void
    {
        var listener4 :TellMarshaller = new TellMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, TELL, [ arg2, arg3, listener4 ]);
    }

}
}
