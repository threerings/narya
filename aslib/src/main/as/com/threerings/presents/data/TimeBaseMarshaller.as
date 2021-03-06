//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.data {

import com.threerings.presents.client.TimeBaseService;
import com.threerings.presents.client.TimeBaseService_GotTimeBaseListener;

/**
 * Provides the implementation of the <code>TimeBaseService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class TimeBaseMarshaller extends InvocationMarshaller
    implements TimeBaseService
{
    /** The method id used to dispatch <code>getTimeOid</code> requests. */
    public static const GET_TIME_OID :int = 1;

    // from interface TimeBaseService
    public function getTimeOid (arg1 :String, arg2 :TimeBaseService_GotTimeBaseListener) :void
    {
        var listener2 :TimeBaseMarshaller_GotTimeBaseMarshaller = new TimeBaseMarshaller_GotTimeBaseMarshaller();
        listener2.listener = arg2;
        sendRequest(GET_TIME_OID, [
            arg1, listener2
        ]);
    }
}
}
