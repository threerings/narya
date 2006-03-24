package com.threerings.crowd.data {

import com.threerings.presents.data.ListenerMarshaller;

import com.threerings.crowd.client.MoveListener

public class MoveMarshaller extends ListenerMarshaller
{
    /** The method id used to dispatch {@link #moveSucceeded}
     * responses. */
    public static const MOVE_SUCCEEDED :int = 1;

    // documentation inherited
    public override function dispatchResponse (methodId :int, args :Array) :void
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
