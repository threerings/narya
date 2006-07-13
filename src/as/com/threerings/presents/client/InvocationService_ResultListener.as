package com.threerings.presents.client {

/**
 * Extends the {@link InvocationListener} with a basic success
 * callback that delivers a result object.
 */
public interface InvocationService_ResultListener
    extends InvocationService_InvocationListener
{
    /**
     * Indicates that the request was successfully processed.
     */
    function requestProcessed (result :Object) :void;
}
}
