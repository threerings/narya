//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import com.threerings.presents.data.ClientObject;

/**
 * Serves as the base interface for invocation services. An invocation service can be defined by
 * extending this interface and defining service methods, as well as response listeners (which must
 * extend {@link InvocationListener}). For example:
 *
 * <pre>{@code
 * public interface LocationService extends InvocationService<ClientObject>
 * {
 *
 *     // Used to communicate responses to moveTo() requests.
 *     public interface MoveListener extends InvocationListener
 *     {
 *         // Called in response to a successful moveTo() request.
 *         void moveSucceeded (PlaceConfig config);
 *     }
 *
 *     // Requests that this client's body be moved to the specified location.
 *     //
 *     // @param placeId the object id of the place object to which the body should be moved.
 *     // @param listener the listener that will be informed of success or failure.
 *     void moveTo (int placeId, MoveListener listener);
 * }
 * }</pre>
 *
 * From this interface, a <code>LocationProvider</code> interface will be generated which should be
 * implemented by whatever server entity that will actually provide the server side of this
 * invocation service. That provider interface would look like the following:
 *
 * <pre>{@code
 * public interface LocationProvider extends InvocationProvider
 * {
 *      // Requests that this client's body be moved to the specified location.
 *      //
 *      // @param caller the client object of the client that invoked this remotely callable method.
 *      // @param placeId the object id of the place object to which the body should be moved.
 *      // @param listener the listener that should be informed of success or failure.
 *     void moveTo (ClientObject caller, int placeId, MoveListener listener)
 *         throws InvocationException;
 * }
 * }</pre>
 */
public interface InvocationService<T extends ClientObject>
{
    /**
     * Invocation service methods that require a response should take a listener argument that can
     * be notified of request success or failure. The listener argument should extend this
     * interface so that generic failure can be reported in all cases. For example:
     *
     * <pre>
     * // Used to communicate responses to <code>moveTo</code> requests.
     * public interface MoveListener extends InvocationListener
     * {
     *     // Called in response to a successful <code>moveTo</code> request.
     *     void moveSucceeded (PlaceConfig config);
     * }
     * </pre>
     */
    public static interface InvocationListener
    {
        /**
         * Called to report request failure. If the invocation services system detects failure of
         * any kind, it will report it via this callback. Particular services may also make use of
         * this callback to report failures of their own, or they may opt to define more specific
         * failure callbacks.
         */
        void requestFailed (String cause);
    }

    /**
     * Extends the {@link InvocationListener} with a basic success callback.
     */
    public static interface ConfirmListener extends InvocationListener
    {
        /**
         * Indicates that the request was successfully processed.
         */
        void requestProcessed ();
    }

    /**
     * Extends the {@link InvocationListener} with a basic success callback that delivers a result
     * object.
     */
    public static interface ResultListener extends InvocationListener
    {
        /**
         * Indicates that the request was successfully processed.
         */
        void requestProcessed (Object result);
    }
}
