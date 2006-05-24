package com.threerings.util {

public class ResultAdapter
    implements ResultListener
{
    public function ResultAdapter (completed :Function, failed :Function)
    {
        _completed = completed;
        _failed = failed;
    }

    // documentation inherited from interface ResultListener
    public function requestCompleted (obj :Object) :void
    {
        if (_completed != null) {
            _completed(obj);
        }
    }

    // documentation inherited from interface ResultListener
    public function requestFailed (cause :Error) :void
    {
        if (_failed != null) {
            _failed(cause);
        }
    }

    protected var _completed :Function;
    protected var _failed :Function;
}
}
