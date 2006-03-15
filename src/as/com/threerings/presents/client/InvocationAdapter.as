package com.threerings.presents.client {

public class InvocationAdapter
    implements InvocationListener
{
    /**
     * Construct an InvocationAdapter that will call the specified
     * function on error.
     */
    public function InvocationAdapter (failedFunc :Function)
    {
        _failedFunc = failedFunc;
    }

    // documentation inherited from interface InvocationListener
    public function requestFailed (cause :String) :void
    {
        _failedFunc(cause);
    }

    /** The Function to call when we've recevied our failure response. */
    protected var _failedFunc :Function;
}
}
