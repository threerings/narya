package com.threerings.presents.dobj {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class DummyEntry
    implements DSetEntry
{
    public function DummyEntry ()
    {
    }

    // documentation inherited from interface DSetEntry
    public function getKey () :Object
    {
        return null; // dummy
    }

    // documentation inherited from superinterface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        // dummy
    }

    // documentation inherited from superinterface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        // dummy
    }
}
}
