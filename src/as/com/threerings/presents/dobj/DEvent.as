package com.threerings.presents.dobj {

import flash.util.StringBuilder;
import flash.util.trace;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.util.Comparable;

public /* abstract */ class DEvent
    implements Streamable
{
    public function DEvent (targetOid :int)
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
    public function applyToObject (target :DObject) :Boolean
    {
        trace("TODO: abstract methods?");
        return false;
    }

    /**
     * Events with associated listener interfaces should implement this
     * function and notify the supplied listener if it implements their
     * event listening interface. For example, the {@link
     * AttributeChangedEvent} will notify listeners that implement {@link
     * AttributeChangeListener}.
     */
    internal function notifyListener (listener :Object) :void
    {
        // the default is to do nothing
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
     * Constructs and returns a string representation of this event.
     */
    public function toString () :String
    {
        var buf :StringBuilder = new StringBuilder();
        buf.append("[");
        toStringBuf(buf);
        buf.append("]");
        return buf.toString();
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific event information to the string buffer.
     */
    protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("targetOid=", _toid);
    }

    /** The oid of the object that is the target of this event. */
    protected var _toid :int;

    protected static const UNSET_OLD_ENTRY :DSetEntry = new DummyEntry();
}
}

class DummyEntry implements com.threerings.presents.dobj.DSetEntry
{
    public function getKey () :com.threerings.util.Comparable
    {
        return null;
    }

    public function writeObject (out :com.threerings.io.ObjectOutputStream) :void
    {
    }

    public function readObject (ins :com.threerings.io.ObjectInputStream) :void
    {
    }
}
