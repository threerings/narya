package com.threerings.presents.client {

import flash.util.trace;

import com.threerings.presents.dobj.DObjectManager;

import com.threerings.presents.data.ClientObject;

public class InvocationDirector
{
    public function init (omgr :DObjectManager, cloid :int, client :Client) :void
    {
        if (_clobj != null) {
            trace("Zoiks, client object around during invmgr init!");
            cleanup();
        }

        _omgr = omgr;
        _client = client;

        // TODO : lots more
    }

    public function cleanup () :void
    {
        _clobj = null;

        // TODO: lots more
    }

    protected var _omgr :DObjectManager;

    protected var _client :Client;

    protected var _clobj :ClientObject;
}
}
