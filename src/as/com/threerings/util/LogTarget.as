package com.threerings.util {

/**
 * A very simple Logging interface.
 * Used with the top-level class QUOTE in this package ENDQUOTE...
 * (it's actually up above 'com', so it never needs importing...)
 */
public interface LogTarget
{
    /**
     * Log the specified message, which is already fully formatted.
     */
    function log (msg :String) :void;
}
}
