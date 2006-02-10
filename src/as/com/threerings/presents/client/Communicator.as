package com.threerings.presents.client {

public class Communicator
{
    public function Communicator (client :Client)
    {
        _client = client;
    }

    public function logon () :void
    {
    }

    protected var _client :Client;
    protected var _omgr :ClientDObjectManager;
}
}
