package com.threerings.crowd.client {

import com.threerings.presents.client.InvocationAdapter;

import com.threerings.crowd.data.PlaceConfig;

public class MoveAdapter extends InvocationAdapter
    implements MoveListener
{
    public function MoveAdapter (success :Function, failure :Function)
    {
        super(failure);
        _success = success;
    }

    // documentation inherited from interface MoveListener
    public function moveSucceeded (config :PlaceConfig) :void
    {
        _success(config);
    }

    protected var _success :Function;
}
}
