package com.threerings.presents.dobj {

import flash.events.EventDispatcher;

public class DObject extends EventDispatcher
{
    public function getOid ():int
    {
        return _oid;
    }

    public function postEvent (DEvent event) :void
    {

    }

    protected var _oid :int;
}
}
