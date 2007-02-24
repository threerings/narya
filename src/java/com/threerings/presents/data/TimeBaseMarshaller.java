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

package com.threerings.presents.data;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.TimeBaseService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

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
    /**
     * Marshalls results to implementations of {@link GotTimeBaseListener}.
     */
    public static class GotTimeBaseMarshaller extends ListenerMarshaller
        implements GotTimeBaseListener
    {
        /** The method id used to dispatch {@link #gotTimeOid}
         * responses. */
        public static final int GOT_TIME_OID = 1;

        // from interface GotTimeBaseMarshaller
        public void gotTimeOid (int arg1)
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, GOT_TIME_OID,
                               new Object[] { Integer.valueOf(arg1) }));
        }

        @Override // from InvocationMarshaller
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case GOT_TIME_OID:
                ((GotTimeBaseListener)listener).gotTimeOid(
                    ((Integer)args[0]).intValue());
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    /** The method id used to dispatch {@link #getTimeOid} requests. */
    public static final int GET_TIME_OID = 1;

    // from interface TimeBaseService
    public void getTimeOid (Client arg1, String arg2, TimeBaseService.GotTimeBaseListener arg3)
    {
        TimeBaseMarshaller.GotTimeBaseMarshaller listener3 = new TimeBaseMarshaller.GotTimeBaseMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, GET_TIME_OID, new Object[] {
            arg2, listener3
        });
    }
}
