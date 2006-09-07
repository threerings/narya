package com.threerings.presents.data {

import com.threerings.presents.client.InvocationService_ResultListener;

public class InvocationMarshaller_ResultMarshaller
    extends InvocationMarshaller_ListenerMarshaller
{
    public static const REQUEST_PROCESSED :int = 1;

    // documetnation inherited
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case REQUEST_PROCESSED:
            (listener as InvocationService_ResultListener).requestProcessed(
                args[0]);
            return;

        default:
            super.dispatchResponse(methodId, args);
        }
    }
}
}
