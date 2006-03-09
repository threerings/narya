package com.threerings.io {

import com.threerings.presents.Log;

public dynamic class TypedArray extends Array
{
    public function TypedArray (jtype :String)
    {
        _jtype = jtype;
        if (_jtype == "" || _jtype == null) {
            Log.info("Created a typed array with bogus type {" + _jtype + "}");
        }
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
