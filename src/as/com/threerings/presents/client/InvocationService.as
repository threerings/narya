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

package com.threerings.presents.client {

/**
 * Serves as the base interface for invocation services. An invocation
 * service can be defined by extending this interface and defining service
 * methods, as well as response listeners (which must extend {@link
 * InvocationListener}). For example:
 *
 * <pre>
 * public interface LocationService extends InvocationService
 * {
 *     
 *     // Used to communicate responses to moveTo() requests.
 *     public interface MoveListener extends InvocationListener
 *     {
 *         // Called in response to a successful moveTo() request.
 *         public void moveSucceeded (PlaceConfig config);
 *     }
 *
 *     // Requests that this client's body be moved to the specified
 *     // location.
 *     //
 *     // @param placeId the object id of the place object to which the
 *     // body should be moved.
 *     // @param listener the listener that will be informed of success or
 *     // failure.
 *     public void moveTo (int placeId, MoveListener listener);
 * }
 * </pre>
 *
 * From this interface, a <code>LocationProvider</code> interface will be
 * generated which should be implemented by whatever server entity that
 * will actually provide the server side of this invocation service. That
 * provider interface would look like the following:
 *
 * <pre>
 * public interface LocationProvider extends InvocationProvider
 * {
 *      // Requests that this client's body be moved to the specified
 *      // location.
 *      //
 *      // @param caller the client object of the client that invoked this
 *      // remotely callable method.
 *      // @param placeId the object id of the place object to which the
 *      // body should be moved.
 *      // @param listener the listener that should be informed of success
 *      // or failure.
 *     public void moveTo (ClientObject caller, int placeId,
 *                         MoveListener listener)
 *         throws InvocationException;
 * }
 * </pre>
 */
public interface InvocationService
{
    // nada
}
}
