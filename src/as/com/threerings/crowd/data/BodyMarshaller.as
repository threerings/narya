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

package com.threerings.crowd.data {

import com.threerings.util.*; // for Float, Integer, etc.

import com.threerings.crowd.client.BodyService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

/**
 * Provides the implementation of the {@link BodyService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class BodyMarshaller extends InvocationMarshaller
    implements BodyService
{
    /** The method id used to dispatch {@link #setIdle} requests. */
    public static const SET_IDLE :int = 1;

    // from interface BodyService
    public function setIdle (arg1 :Client, arg2 :Boolean) :void
    {
        sendRequest(arg1, SET_IDLE, [
            langBoolean.valueOf(arg2)
        ]);
    }
}
}
