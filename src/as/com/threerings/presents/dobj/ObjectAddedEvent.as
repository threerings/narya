package com.threerings.presents.dobj {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.util.StringBuilder;

/**
 * An object added event is dispatched when an object is added to an
 * <code>OidList</code> attribute of a distributed object. It can also be
 * constructed to request the addition of an oid to an
 * <code>OidList</code> attribute of an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectAddedEvent extends NamedEvent
{
    /**
     * Constructs a new object added event on the specified target object
     * with the supplied oid list attribute name and object id to add.
     *
     * @param targetOid the object id of the object to whose oid list we
     * will add an oid.
     * @param name the name of the attribute (data member) to which to add
     * the specified oid.
     * @param oid the oid to add to the oid list attribute.
     */
    public function ObjectAddedEvent (targetOid :int, name :String, oid :int)
    {
        super(targetOid, name);
        _oid = oid;
    }

    /**
     * Returns the oid that has been added.
     */
    public function getOid () :int
    {
        return _oid;
    }

    /**
     * Applies this event to the object.
     */
    public override function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        var list :OidList = target[_name];
        list.add(_oid);
        return true;
    }

    // documentation inherited
    protected override function notifyListener (listener :Object) :void
    {
        if (listener is OidListListener) {
            listener.objectAdded(this);
        }
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(_oid);
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _oid = ins.readInt();
    }

    // documentation inherited
    protected override function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("OBJADD:");
        super.toStringBuf(buf);
        buf.append(", oid=", _oid);
    }

    protected var _oid :int;
}
}
