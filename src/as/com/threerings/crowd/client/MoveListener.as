package com.threerings.crowd.client {

import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.client.InvocationService_InvocationListener;

import com.threerings.crowd.data.PlaceConfig;

public interface MoveListener extends InvocationService_InvocationListener
{
    function moveSucceeded (config :PlaceConfig) :void;
}
}
