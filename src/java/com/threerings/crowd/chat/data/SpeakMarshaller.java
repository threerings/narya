//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.crowd.chat.client.SpeakService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;

/**
 * Provides the implementation of the {@link SpeakService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class SpeakMarshaller extends InvocationMarshaller
    implements SpeakService
{
    /** The method id used to dispatch {@link #speak} requests. */
    public static final int SPEAK = 1;

    // from interface SpeakService
    public void speak (Client arg1, String arg2, byte arg3)
    {
        sendRequest(arg1, SPEAK, new Object[] {
            arg2, Byte.valueOf(arg3)
        });
    }
}
