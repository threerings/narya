//
// $Id: ReleaseLockEvent.java,v 1.6 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;
import java.lang.reflect.Method;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * A release lock event is dispatched at the end of a chain of events to
 * release a lock that is intended to prevent some application defined
 * activity from happening until those events have been processed. This is
 * an entirely cooperative locking system, meaning that the application
 * will have to explicitly attempt acquisition of the lock to find out if
 * the lock has yet been released. These locks don't actually prevent any
 * of the distributed object machinery from functioning.
 *
 * @see DObjectManager#postEvent
 */
public class ReleaseLockEvent extends DEvent
{
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
     * Applies this lock release to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // clear this lock from the target object
        target.clearLock(_name);
        // no need to notify subscribers about these sorts of events
        return false;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeUTF(_name);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _name = in.readUTF();
    }

    protected void toString (StringBuffer buf)
    {
        buf.append("UNLOCK:");
        super.toString(buf);
        buf.append(", name=").append(_name);
    }

    protected String _name;
}
