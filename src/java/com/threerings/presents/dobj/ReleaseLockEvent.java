//
// $Id: ReleaseLockEvent.java,v 1.1 2001/08/04 00:32:11 mdb Exp $

package com.threerings.cocktail.cher.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import com.threerings.cocktail.cher.dobj.io.ValueMarshaller;

/**
 * A release lock event is dispatched at the end of a chain of attribute
 * change events to release a lock that is intended to prevent some
 * application defined activity from happening on the distributed object
 * until those events have been processed.
 *
 * @see DObjectManager#postEvent
 */
public class ReleaseLockEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 6;

    /**
     * Constructs a new release lock event for the specified target object
     * with the supplied lock name.
     *
     * @param targetOid the object id of the object in question.
     * @param name the name of the lock to release.
     */
    public ReleaseLockEvent (int targetOid, String name)
    {
        super(targetOid);
        _name = name;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public ReleaseLockEvent ()
    {
    }

    /**
     * Returns the name of the lock to release.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Applies this attribute change to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // pass the new value on to the object
        target.setAttribute(_name, _value);
        return true;
    }

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeUTF(_name);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _name = in.readUTF();
    }

    protected void toString (StringBuffer buf)
    {
        buf.append("UNLOCK:");
        super.toString(buf);
        buf.append(", name=").append(_name);
    }

    protected String _name;
    protected Object _value;
}
