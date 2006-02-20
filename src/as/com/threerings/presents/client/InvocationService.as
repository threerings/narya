//
// $Id: InvocationService.java 3099 2004-08-27 02:21:06Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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
    /**
     * Invocation service methods that require a response should take a
     * listener argument that can be notified of request success or
     * failure. The listener argument should extend this interface so that
     * generic failure can be reported in all cases. For example:
     *
     * <pre>
     * // Used to communicate responses to <code>moveTo</code> requests.
     * public interface MoveListener extends InvocationListener
     * {
     *     // Called in response to a successful <code>moveTo</code>
     *     // request.
     *     public void moveSucceeded (PlaceConfig config);
     * }
     * </pre>
     */
//    public static interface InvocationListener
//    {
        /**
         * Called to report request failure. If the invocation services
         * system detects failure of any kind, it will report it via this
         * callback. Particular services may also make use of this
         * callback to report failures of their own, or they may opt to
         * define more specific failure callbacks.
         */
//        public void requestFailed (String cause);
//    }

    /**
     * Extends the {@link InvocationListener} with a basic success
     * callback.
     */
//    public static interface ConfirmListener extends InvocationListener
//    {
        /**
         * Indicates that the request was successfully processed.
         */
//        public void requestProcessed ();
//    }

    /**
     * Extends the {@link InvocationListener} with a basic success
     * callback that delivers a result object.
     */
//    public static interface ResultListener extends InvocationListener
//    {
        /**
         * Indicates that the request was successfully processed.
         */
//        public void requestProcessed (Object result);
//    }
}
}
