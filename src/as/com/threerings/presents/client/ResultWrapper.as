package com.threerings.presents.client {

public class ResultWrapper extends InvocationAdapter
    implements InvocationService_ResultListener
{
    public function ResultWrapper (
        failed :Function, processed :Function = null)
    {
        super(failed);
        _processed = processed;
    }

    // documentation inherited from interface ResultListener
    public function requestProcessed (result :Object) :void
    {
        _processed(result);
    }

    protected var _processed :Function;
}
}
