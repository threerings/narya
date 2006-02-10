package com.threerings.io {

public dynamic class StreamableArray extends Array
    implements Streamable
{
    public function StreamableArray (ctype :Class = undefined, length :int = 0)
    {
        super(length);
        _ctype = ctype;
    }

    public function StreamableArray (ctype :Class, ... values)
    {
        super(values);
        _ctype = ctype;
    }

    /** The type of all the elements in the array. */
    protected var _ctype :Class;
}
