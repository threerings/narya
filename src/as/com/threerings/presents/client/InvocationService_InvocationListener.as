package com.threerings.presents.client {

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
public interface InvocationService_InvocationListener
{
    /**
     * Called to report request failure. If the invocation services
     * system detects failure of any kind, it will report it via this
     * callback. Particular services may also make use of this
     * callback to report failures of their own, or they may opt to
     * define more specific failure callbacks.
     */
    function requestFailed (cause :String) :void;
}
}
