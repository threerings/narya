package com.threerings.presents.data {

import com.threerings.presents.client.ConfirmListener;

public class ConfirmMarshaller extends ListenerMarshaller
    implements ConfirmListener
{
    public static const REQUEST_PROCESSED :int = 1;

    // documetnation inherited from interfacc
    public function requestProcessed ()
    {
        // TODO: server only?
    }

    // documetnation inherited
    public function dispatchResponse (methodId :int, args :Array) :void
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
