package com.threerings.util {

/**
 * Equivalent to java.lang.Short.
 */
public class Short
    implements Equalable
{
    public var value :int;

    public function Short (value :int)
    {
        this.value = value;
    }

    // documentation inherited from interface Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Short) && (value === (other as Short).value);
    }
}
}
