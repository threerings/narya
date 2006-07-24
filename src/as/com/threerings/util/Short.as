package com.threerings.util {

/**
 * Equivalent to java.lang.Short.
 */
public class Short
    implements Equalable, Wrapped
{
    public var value :int;

    public static function valueOf (val :int) :Short
    {
        return new Short(val);
    }

    public function Short (value :int)
    {
        this.value = value;
    }

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Short) && (value === (other as Short).value);
    }

    // from Wrapped
    public function unwrap () :Object
    {
        return value;
    }
}
}
