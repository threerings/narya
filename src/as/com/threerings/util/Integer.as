package com.threerings.util {

/**
 * Equivalent to java.lang.Integer.
 */
// Unfortunately, I think this is necessary.
// I was going to remove this class and just make the streaming stuff
// autotranslate between int <--> java.lang.Integer and
// Number <--> java.lang.Double. However, a Number object that refers
// to an integer value is actually an int. Yes, it's totally fucked.
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
