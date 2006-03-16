package com.threerings.crowd.client {

import com.threerings.presents.client.InvocationListener;

public interface MoveListener extends InvocationListener
{
    function moveSucceeded (config :PlaceConfig) :void;
}
}
