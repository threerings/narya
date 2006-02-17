package com.threerings.presents.dobj {

import flash.util.StringBuilder;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * A common parent class for all events that are associated with a name
 * (in some cases a field name, in other cases just an identifying name).
 */
public class NamedEvent extends DEvent
{
    /**
     * Constructs a new named event for the specified target object with
     * the supplied attribute name.
     *
     * @param targetOid the object id of the object in question.
     * @param name the name associated with this event.
     */
    public NamedEvent (targetOid :int, name :String)
    {
        super(targetOid);
        _name = name;
    }

    /**
     * Returns the name of the attribute to which this event pertains.
     */
    public function getName () :String
    {
        return _name;
    }

    protected override function toString (buf :StringBuilder) :void
    {
        super.toString(buf);
        buf.append(", name=", _name);
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeField(_name);
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _name = ins.readField(String);
    }

    /** The name of the event. */
    protected var _name :String;
}
}
