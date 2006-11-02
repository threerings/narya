package com.threerings.util {

public class StringBuilder
{
    public function StringBuilder (... args)
    {
        append.apply(this, args);
    }

    /**
     * Append all arguments to the end of the string being built
     * and return this StringBuilder.
     */
    public function append (... args) :StringBuilder
    {
        for each (var o :Object in args) {
            _buf += String(o);
        }
        return this;
    }

    /**
     * Return the String built so far.
     */
    public function toString () :String
    {
        // it's ok, Strings are immutable
        return _buf;
    }

    /** The string upon which we build. Internally in AVM2, Strings have
     * been designed with a prefix pointer so that concatination is
     * really cheap. */
    protected var _buf :String = "";
}
}
