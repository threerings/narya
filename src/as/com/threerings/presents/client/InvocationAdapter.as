package com.threerings.presents.client {

public class InvocationAdapter
    implements InvocationListener
{
    /**
     * Construct an InvocationAdapter that will call the specified
     * function on error.
     */
    public function InvocationAdapter (failedFunc :Function, args :Array = null)
    {
        _failedFunc = failedFunc;
        _args = args;
    }

    // documentation inherited from interface InvocationListener
    public function requestFailed (cause :String) :void
    {
        _failedFunc(cause, _args);
    }

    /** The Function to call when we've recevied our failure response. */
    protected var _failedFunc :Function;

    /** Any other extra information to pass along to the function. */
    protected var _args :Array;
}
}
