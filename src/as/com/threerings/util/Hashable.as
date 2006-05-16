package com.threerings.util {

public interface Hashable extends Equalable
{
    /**
     * Get a hashcode for this Equalable object so that it may be placed
     * in a HashMap.
     */
    function hashCode () :int;
}
}
