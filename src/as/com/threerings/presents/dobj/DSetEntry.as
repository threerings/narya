package com.threerings.presents.dobj {

import com.threerings.io.Streamable;

public interface DSetEntry extends Streamable
{
    function getKey () :Comparable;
}
}
