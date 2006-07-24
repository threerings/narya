package com.threerings.util {

/**
 * Equivalent to java.lang.Float.
 */
public class Float
    implements Equalable, Wrapped
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

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Float) && (value === (other as Float).value);
    }

    // from Wrapped
    public function unwrap () :Object
    {
        return value;
    }
}
}
