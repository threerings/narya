package com.threerings.util {

import flash.net.SharedObject;

public class Config
{
    /**
     * Constructs a new config object which will obtain configuration
     * information from the specified path.
     */
    public function Config (path :String)
    {
        _so = SharedObject.getLocal("config_" + path, "/");
    }

    /**
     * Fetches and returns the value for the specified configuration property.
     */
    public function getValue (name :String, defValue :Object) :Object
    {
        var val :* = _so.data[name];
        return (val === undefined) ? defValue : val;
    }

    /**
     * Returns the value specified.
     */
    public function setValue (name :String, value :Object) :void
    {
        _so.data[name] = value;
        _so.flush(); // flushing is not strictly necessary
    }

    /**
     * Remove any set value for the specified preference.
     */
    public function remove (name :String) :void
    {
        delete _so.data[name];
        _so.flush(); // flushing is not strictly necessary
    }

    /** The shared object that contains our preferences. */
    protected var _so :SharedObject;
}
}
