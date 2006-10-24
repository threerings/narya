package com.threerings.util {

/**
 * Equivalent to java.lang.Short.
 */
public class Short
    implements Equalable, Wrapped
{
    /** The minimum possible short value. */
    public static const MIN_VALUE :int = -Math.pow(2, 15);

    /** The maximum possible short value. */
    public static const MAX_VALUE :int = (Math.pow(2, 15) - 1);

    /** The value of this short. */
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
