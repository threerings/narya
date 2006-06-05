package com.threerings.crowd.data {

import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

import com.threerings.crowd.client.MoveListener

public class LocationMarshaller_MoveMarshaller extends InvocationMarshaller_ListenerMarshaller
{
    /** The method id used to dispatch {@link #moveSucceeded}
     * responses. */
    public static const MOVE_SUCCEEDED :int = 1;

    // documentation inherited
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case MOVE_SUCCEEDED:
            (listener as MoveListener).moveSucceeded(
                (args[0] as PlaceConfig));
            return;

        default:
            super.dispatchResponse(methodId, args);
        }
    }
}
}
