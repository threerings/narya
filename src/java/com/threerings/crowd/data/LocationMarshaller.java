//
// $Id$
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

package com.threerings.crowd.data;

import com.threerings.crowd.client.LocationService;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link LocationService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class LocationMarshaller extends InvocationMarshaller
    implements LocationService
{
    // documentation inherited
    public static class MoveMarshaller extends ListenerMarshaller
        implements MoveListener
    {
        /** The method id used to dispatch {@link #moveSucceeded}
         * responses. */
        public static final int MOVE_SUCCEEDED = 1;

        // documentation inherited from interface
        public void moveSucceeded (PlaceConfig arg1)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, MOVE_SUCCEEDED,
                               new Object[] { arg1 }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case MOVE_SUCCEEDED:
                ((MoveListener)listener).moveSucceeded(
                    (PlaceConfig)args[0]);
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /** The method id used to dispatch {@link #leavePlace} requests. */
    public static final int LEAVE_PLACE = 1;

    // documentation inherited from interface
    public void leavePlace (Client arg1)
    {
        sendRequest(arg1, LEAVE_PLACE, new Object[] {
            
        });
    }

    /** The method id used to dispatch {@link #moveTo} requests. */
    public static final int MOVE_TO = 2;

    // documentation inherited from interface
    public void moveTo (Client arg1, int arg2, LocationService.MoveListener arg3)
    {
        LocationMarshaller.MoveMarshaller listener3 = new LocationMarshaller.MoveMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, MOVE_TO, new Object[] {
            new Integer(arg2), listener3
        });
    }

}
