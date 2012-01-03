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

package com.threerings.crowd.client;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;

import com.threerings.crowd.data.PlaceConfig;

/**
 * The location services provide a mechanism by which the client can request to move from place to
 * place in the server. These services should not be used directly, but instead should be accessed
 * via the {@link LocationDirector}.
 */
public interface LocationService extends InvocationService<ClientObject>
{
    /**
     * Used to communicate responses to {@link LocationService#moveTo} requests.
     */
    public static interface MoveListener extends InvocationListener
    {
        /**
         * Called in response to a successful {@link LocationService#moveTo} request.
         */
        void moveSucceeded (PlaceConfig config);
    }

    /**
     * Requests that this client's body be moved to the specified location.
     *
     * @param placeId the object id of the place object to which the body should be moved.
     * @param listener the listener that will be informed of success or failure.
     */
    void moveTo (int placeId, MoveListener listener);

    /**
     * Requests that we leave our current place and move to nowhere land.
     */
    void leavePlace ();
}
