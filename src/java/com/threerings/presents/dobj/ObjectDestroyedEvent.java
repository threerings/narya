//
// $Id: ObjectDestroyedEvent.java,v 1.1 2001/08/07 20:38:58 mdb Exp $

package com.threerings.cocktail.cher.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * An object destroyed event is dispatched when an object has been removed
 * from the distributed object system. It can also be constructed to
 * request an attribute change on an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectDestroyedEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 7;

    /**
     * Constructs a new object destroyed event for the specified
     * distributed object.
     *
     * @param targetOid the object id of the object that will be destroyed.
     */
    public ObjectDestroyedEvent (int targetOid)
    {
        super(targetOid);
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public ObjectDestroyedEvent ()
    {
    }

    /**
     * Applies this attribute change to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // nothing to do in preparation for destruction, the omgr will
        // have to recognize this type of event and do the right thing
        return true;
    }

    public short getType ()
    {
        return TYPE;
    }

    protected void toString (StringBuffer buf)
    {
        buf.append("DESTROY:");
        super.toString(buf);
    }
}
