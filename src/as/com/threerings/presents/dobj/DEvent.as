package com.threerings.presents.dobj {

import flash.util.StringBuilder;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class DEvent
    implements Streamable
{
    public function DEvent ()
    {
    }

    public function DEvent (int targetOid)
    {
        _toid = targetOid;
    }

    /**
     * Returns the oid of the object that is the target of this event.
     */
    public function getTargetOid () :int
    {
        return _toid;
    }


    /**
     * Some events are used only internally on the server and need not be
     * broadcast to subscribers, proxy or otherwise. Such events can
     * return true here and short-circuit the normal proxy event dispatch
     * mechanism.
     */
    public function applyToObject (target :DObject) :void
    {
        trace("TODO: abstract methods?");
    }

    /**
     * Events with associated listener interfaces should implement this
     * function and notify the supplied listener if it implements their
     * event listening interface. For example, the {@link
     * AttributeChangedEvent} will notify listeners that implement {@link
     * AttributeChangeListener}.
     */
    protected function notifyListener (listener :*) :void
    {
        // the default is to do nothing
    }

    /**
     * Constructs and returns a string representation of this event.
     */
    public override function toString () :String
    {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        toString(buf);
        buf.append("]");
        return buf.toString();
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(_toid);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        _toid = ins.readInt();
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific event information to the string buffer.
     */
    protected function toString (buf :StringBuilder) :void
    {
        buf.append("targetOid=", _toid);
    }

    /** The oid of the object that is the target of this event. */
    protected var _toid :int;

    protected static const UNSET_OLD_ENTRY :DSetEntry = new DummyEntry();
}
}

class DummyEntry implements DSetEntry
{
    public function getKey () :Comparable
    {
        return null;
    }

    public function writeObject (out :ObjectOutputStream) :void
    {
    }

    public function readObject (ins :ObjectInputStream) :void
    {
    }
}
