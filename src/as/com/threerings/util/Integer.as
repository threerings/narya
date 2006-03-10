package com.threerings.util {

/**
 * Equivalent to java.lang.Integer.
 */
public class Integer
    implements Equalable
{
    public var value :int;

    public function Integer (value :int)
    {
        this.value = value;
    }

    // documentation inherited from interface Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Integer) && (value === (other as Integer).value);
    }
}
}
