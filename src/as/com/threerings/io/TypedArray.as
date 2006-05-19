package com.threerings.io {

public dynamic class TypedArray extends Array
{
    public function TypedArray (jtype :String)
    {
        _jtype = jtype;
    }

    public function getJavaType () :String
    {
        return _jtype;
    }

    /** The 'type' of this array, which doesn't really mean anything
     * except gives it a clue as to how to stream to our server. */
    protected var _jtype :String;
}
}
