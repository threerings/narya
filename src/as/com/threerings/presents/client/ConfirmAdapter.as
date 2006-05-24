package com.threerings.presents.client {

public class ConfirmAdapter
    implements ConfirmListener
{
    public function ConfirmAdapter (processed :Function, failed :Function)
    {
        _processed = processed;
        _failed = failed;
    }

    // documentation inherited from interface ConfirmListener
    public function requestProcessed () :void
    {
        _processed();
    }

    // documentation inherited from interface ConfirmListener
    public function requestFailed (cause :String) :void
    {
        _failed(cause);
    }

    protected var _processed :Function;
    protected var _failed :Function;
}
}
