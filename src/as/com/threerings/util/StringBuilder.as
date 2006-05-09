package com.threerings.util {

public class StringBuilder
{
    public function StringBuilder (... args)
    {
        _array = args;
    }

    /**
     * Append all arguments to the end of the string being built
     * and return this StringBuilder.
     */
    public function append (... args) :StringBuilder
    {
        while (args.length > 0) {
            _array.push(args.shift());
        }
        return this;
    }

    /**
     * Return the String built so far.
     */
    public function toString () :String
    {
        return _array.join("");
    }

    /** Our array in which we place all arguments. */
    protected var _array :Array;
}
}
