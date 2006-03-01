package com.threerings.presents.client {

import com.threerings.util.Comparable;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.DSetEntry;

/**
 * Used to maintain a registry of invocation receivers that can be
 * used to convert (large) hash codes into (small) registration
 * numbers.
 */
public class InvocationRegistration
    implements DSetEntry
{
    /** The unique hash code associated with this invocation receiver class. */
    public var receiverCode :String;

    /** The unique id assigned to this invocation receiver class at
     * registration time. */
    public var receiverId :int;

    /** Creates and initializes a registration instance. */
    public function Registration (receiverCode :String, receiverId :int)
    {
        this.receiverCode = receiverCode;
        this.receiverId = receiverId;
    }

    // documentation inherited from interface DSetEntry
    public function getKey () :Object
    {
        return receiverCode;
    }

    // documentation inherited
    public function toString () :String
    {
        return "[" + receiverCode + " => " + receiverId + "]";
    }

    // documentation inherited from superinterface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeField(receiverCode);
        out.writeShort(receiverId);
    }

    // documentation inherited from superinterface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        receiverCode = (ins.readField(String) as String);
        receiverId = ins.readShort();
    }
}
}
