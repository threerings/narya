//
// $Id: ObjectDestroyedEvent.java,v 1.4 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;
import java.lang.reflect.Method;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * An object destroyed event is dispatched when an object has been removed
 * from the distributed object system. It can also be constructed to
 * request an attribute change on an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectDestroyedEvent extends DEvent
{
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

    // documentation inherited
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // nothing to do in preparation for destruction, the omgr will
        // have to recognize this type of event and do the right thing
        return true;
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof ObjectDeathListener) {
            ((ObjectDeathListener)listener).objectDestroyed(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("DESTROY:");
        super.toString(buf);
    }
}
