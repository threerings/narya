package com.threerings.util {

/**
 * An interface implemented by our wrapper classes.
 */
public interface Wrapped
{
    /**
     * Return the wrapped value.
     */
    function unwrap () :Object;
}
}
