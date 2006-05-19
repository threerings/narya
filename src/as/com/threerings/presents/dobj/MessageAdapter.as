package com.threerings.presents.dobj {

public class MessageAdapter
    implements MessageListener
{
    public function MessageAdapter (received :Function)
    {
        _received = received;
    }

    // documentation inherited from interface MessageListener
    public function messageReceived (event :MessageEvent) :void
    {
        _received(event);
    }

    protected var _received :Function;
}
}
