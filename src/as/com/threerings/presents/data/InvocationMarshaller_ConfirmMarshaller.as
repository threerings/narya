package com.threerings.presents.data {

import com.threerings.presents.client.ConfirmListener;

public class InvocationMarshaller_ConfirmMarshaller
    extends InvocationMarshaller_ListenerMarshaller
    implements ConfirmListener
{
    public static const REQUEST_PROCESSED :int = 1;

    // documetnation inherited from interfacc
    public function requestProcessed () :void
    {
        // TODO: server only?
    }

    // documetnation inherited
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case REQUEST_PROCESSED:
            (listener as ConfirmListener).requestProcessed();
            return;

        default:
            super.dispatchResponse(methodId, args);
        }
    }
}
}
