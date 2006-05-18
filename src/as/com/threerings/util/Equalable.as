package com.threerings.util {

/**
 * An interface we can use to implement equals(), which is standard and
 * very useful in Java.
 */
public interface Equalable
{
    /**
     * Returns true to indicate that the specified object is equal to
     * this instance.
     */
    function equals (other :Object) :Boolean;
}
}
