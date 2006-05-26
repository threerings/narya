package com.threerings.crowd.chat.data {

import com.threerings.util.Long;

import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

import com.threerings.crowd.chat.client.TellListener;

public class ChatMarshaller_TellMarshaller extends InvocationMarshaller_ListenerMarshaller
{
    /** The method id used to dispatch {@link #tellSucceeded}
     * responses. */
    public static const TELL_SUCCEEDED :int = 1;

    // documentation inherited
    public override function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case TELL_SUCCEEDED:
            (listener as TellListener).tellSucceeded(
                (args[0] as Long), (args[1] as String));
            return;

        default:
            super.dispatchResponse(methodId, args);
        }
    }
}
}
