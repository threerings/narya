package com.threerings.util {

/**
 * Equivalent to java.lang.Byte.
 */
public class Byte
    implements Equalable
{
    public var value :int;

    public static function valueOf (val :int) :Byte
    {
        return new Byte(val);
    }

    public function Byte (value :int)
    {
        this.value = value;
    }

    // documentation inherited from interface Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Byte) && (value === (other as Byte).value);
    }

    // documentation inherited
    public function toString () :String
    {
        return "Byte(" + value + ")";
    }
}
}
