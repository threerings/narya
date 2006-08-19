package com.threerings.presents.client {

public class ConfirmAdapter extends InvocationAdapter
    implements InvocationService_ConfirmListener
{
    public function ConfirmAdapter (
        failed :Function, processed :Function = null)
    {
        super(failed);
        _processed = processed;
    }

    // documentation inherited from interface ConfirmListener
    public function requestProcessed () :void
    {
        if (_processed != null) {
            _processed();
        }
    }

    protected var _processed :Function;
}
}
