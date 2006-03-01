package com.threerings.util {

/**
 * An interface we can use to implement equals(), which is standard and
 * very useful in Java.
 */
public interface Equalable {
    /**
     * Return true to see if this instance is equal to the specified object.
     */
    function equals (other :Object) :Boolean;
}
}
