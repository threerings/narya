package com.threerings.presents.dobj {

import com.threerings.presents.Log;

public class SubscriberAdapter
    implements Subscriber
{
    public function SubscriberAdapter (success :Function, failure :Function)
    {
        _success = success;
        _failure = failure;
    }

    // documentation inherited from interface Subscriber
    public function objectAvailable (obj :DObject) :void
    {
        Log.debug("calling success function: " + _success);
        _success(obj);
    }

    // documentation inherited from interface Subscriber
    public function requestFailed (oid :int, cause :ObjectAccessError) :void
    {
        _failure(oid, cause);
    }

    protected var _success :Function;
    protected var _failure :Function;
}
}
