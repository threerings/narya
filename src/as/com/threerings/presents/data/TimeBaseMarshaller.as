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

package com.threerings.presents.data {

import flash.utils.ByteArray;
import com.threerings.util.*; // for Float, Integer, etc.
import com.threerings.io.TypedArray;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.TimeBaseService;
import com.threerings.presents.client.TimeBaseService_GotTimeBaseListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;
import com.threerings.presents.data.TimeBaseMarshaller_GotTimeBaseMarshaller;

/**
 * Provides the implementation of the {@link TimeBaseService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class TimeBaseMarshaller extends InvocationMarshaller
    implements TimeBaseService
{
    /** The method id used to dispatch {@link #getTimeOid} requests. */
    public static const GET_TIME_OID :int = 1;

    // from interface TimeBaseService
    public function getTimeOid (arg1 :Client, arg2 :String, arg3 :TimeBaseService_GotTimeBaseListener) :void
    {
        var listener3 :TimeBaseMarshaller_GotTimeBaseMarshaller = new TimeBaseMarshaller_GotTimeBaseMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, GET_TIME_OID, [
            arg2, listener3
        ]);
    }
}
}
