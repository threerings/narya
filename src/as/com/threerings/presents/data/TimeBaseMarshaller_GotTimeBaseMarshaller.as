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

package com.threerings.presents.data {

import com.threerings.presents.client.TimeBaseService_GotTimeBaseListener;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

/**
 * Marshalls instances of the TimeBaseService_GotTimeBaseMarshaller interface.
 */
public class TimeBaseMarshaller_GotTimeBaseMarshaller
    extends InvocationMarshaller_ListenerMarshaller
{
    /** The method id used to dispatch {@link #gotTimeOid} responses. */
    public static const GOT_TIME_OID :int = 1;

    // from InvocationMarshaller_ListenerMarshaller
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case GOT_TIME_OID:
            (listener as TimeBaseService_GotTimeBaseListener).gotTimeOid(
                (args[0] as int));
            return;

        default:
            super.dispatchResponse(methodId, args);
            return;
        }
    }
}
}
