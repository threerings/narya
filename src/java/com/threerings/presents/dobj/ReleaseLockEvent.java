//
// $Id: ReleaseLockEvent.java,v 1.7 2002/12/20 23:29:04 mdb Exp $

package com.threerings.presents.dobj;

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
public class ReleaseLockEvent extends NamedEvent
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
        super(targetOid, name);
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public ReleaseLockEvent ()
    {
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

    protected void toString (StringBuffer buf)
    {
        buf.append("UNLOCK:");
        super.toString(buf);
    }
}
