package com.threerings.crowd.client {

import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.client.InvocationListener;

import com.threerings.crowd.data.PlaceConfig;

public interface MoveListener extends InvocationListener
{
    function moveSucceeded (config :PlaceConfig) :void;
}
}
