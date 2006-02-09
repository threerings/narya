package com.threerings.presents.dobj {

public class DEvent extends Object
{
    public function DEvent ()
    {
    }

    public function DEvent (int targetOid)
    {
        _toid = targetOid;
    }

    public function getTargetOid () :int
    {
        return _toid;
    }

    /** The oid of the object that is the target of this event. */
    protected var _toid :int;
}
}
