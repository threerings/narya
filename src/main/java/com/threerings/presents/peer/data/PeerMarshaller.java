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

package com.threerings.presents.peer.data;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.peer.client.PeerService;

/**
 * Provides the implementation of the {@link PeerService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from PeerService.java.")
public class PeerMarshaller extends InvocationMarshaller<ClientObject>
    implements PeerService
{
    /** The method id used to dispatch {@link #generateReport} requests. */
    public static final int GENERATE_REPORT = 1;

    // from interface PeerService
    public void generateReport (String arg1, InvocationService.ResultListener arg2)
    {
        InvocationMarshaller.ResultMarshaller listener2 = new InvocationMarshaller.ResultMarshaller();
        listener2.listener = arg2;
        sendRequest(GENERATE_REPORT, new Object[] {
            arg1, listener2
        });
    }

    /** The method id used to dispatch {@link #invokeAction} requests. */
    public static final int INVOKE_ACTION = 2;

    // from interface PeerService
    public void invokeAction (byte[] arg1)
    {
        sendRequest(INVOKE_ACTION, new Object[] {
            arg1
        });
    }

    /** The method id used to dispatch {@link #invokeRequest} requests. */
    public static final int INVOKE_REQUEST = 3;

    // from interface PeerService
    public void invokeRequest (byte[] arg1, InvocationService.ResultListener arg2)
    {
        InvocationMarshaller.ResultMarshaller listener2 = new InvocationMarshaller.ResultMarshaller();
        listener2.listener = arg2;
        sendRequest(INVOKE_REQUEST, new Object[] {
            arg1, listener2
        });
    }

    /** The method id used to dispatch {@link #ratifyLockAction} requests. */
    public static final int RATIFY_LOCK_ACTION = 4;

    // from interface PeerService
    public void ratifyLockAction (NodeObject.Lock arg1, boolean arg2)
    {
        sendRequest(RATIFY_LOCK_ACTION, new Object[] {
            arg1, Boolean.valueOf(arg2)
        });
    }
}
