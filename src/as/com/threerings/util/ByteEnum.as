//
// $Id$

package com.threerings.util {

/**
 * An enum value that can be persisted as a byte.
 */
public interface ByteEnum
{
    /**
     * Return the byte form of this enum.
     */
    function toByte () :int;
}
}
