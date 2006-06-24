package com.threerings.util {

/**
 * Equivalent to java.lang.Float.
 */
public class Float
    implements Equalable
{
    public var value :Number;

    public static function valueOf (val :Number) :Float
    {
        return new Float(val);
    }

    public function Float (value :Number)
    {
        this.value = value;
    }

    // documentation inherited from interface Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Float) && (value === (other as Float).value);
    }
}
}
