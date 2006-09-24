package com.threerings.presents.data {

import com.threerings.util.Wrapped;

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
            var result :Object = args[0];
            if (result is Wrapped) {
                result = (result as Wrapped).unwrap();
            }
            (listener as InvocationService_ResultListener).requestProcessed(
                result);
            return;

        default:
            super.dispatchResponse(methodId, args);
        }
    }
}
}
