package com.threerings.presents.dobj {

import com.threerings.io.Streamable;

import com.threerings.util.Comparable;

public interface DSetEntry extends Streamable
{
    function getKey () :Comparable;
}
}
