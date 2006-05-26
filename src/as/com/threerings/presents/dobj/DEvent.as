package com.threerings.presents.dobj {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.util.Comparable;
import com.threerings.util.StringBuilder;

public /* abstract */ class DEvent
    implements Streamable
{
    public function DEvent (targetOid :int = 0)
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
        // TODO
        Log.getLog(this).warning(
            "DEvent.applyToTarget is really an abstract method.");
        return false;
    }

    /**
     * We want to make the notifyListener method visible to DObject.
     */
    internal function friendNotifyListener (listener :Object) :void
    {
        notifyListener(listener);
    }

    /**
     * Events with associated listener interfaces should implement this
     * function and notify the supplied listener if it implements their
     * event listening interface. For example, the {@link
     * AttributeChangedEvent} will notify listeners that implement {@link
     * AttributeChangeListener}.
     */
    protected function notifyListener (listener :Object) :void
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

    protected static const UNSET_OLD_ENTRY :DSet_Entry = new DummyEntry();
}
}

/*
import com.threerings.presents.dobj.DSet_Entry;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

class DummyEntry implements DSet_Entry
{
    public function DummyEntry ()
    {
    }

    public function getKey () :Object
    {
        return null; // dummy
    }

    public function writeObject (out :ObjectOutputStream) :void
    {
        // dummy
    }

    public function readObject (ins :ObjectInputStream) :void
    {
        // dummy
    }
}
*/
