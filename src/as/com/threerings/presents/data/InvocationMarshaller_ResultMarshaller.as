package com.threerings.presents.data {

import com.threerings.presents.client.InvocationService_ResultListener;

public class InvocationMarshaller_ResultMarshaller
    extends InvocationMarshaller_ListenerMarshaller
    implements InvocationService_ResultListener
{
    public static const REQUEST_PROCESSED :int = 1;

    // documetnation inherited from interfacc
    public function requestProcessed (result :Object) :void
    {
        // TODO: server only?
    }

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
