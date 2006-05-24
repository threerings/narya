package com.threerings.presents.client {

/**
 * Extends the {@link InvocationListener} with a basic success
 * callback.
 */
public interface ConfirmListener extends InvocationListener
{
    /**
     * Indicates that the request was successfully processed.
     */
    function requestProcessed () :void;
}
}
