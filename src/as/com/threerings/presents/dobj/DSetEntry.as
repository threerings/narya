package com.threerings.presents.dobj {

import com.threerings.util.Equalable;

import com.threerings.io.Streamable;

public interface DSetEntry extends Streamable
{
    /**
     * Get the key used to identify this object. On the java side of things,
     * this key should be a Comparable object, but here in ActionScript land
     * it needs to be either a simple type or implement Equalable.
     */
    function getKey () :Object;
}
}
