//
// $Id: NamedEvent.java,v 1.1 2002/12/20 23:29:04 mdb Exp $

package com.threerings.presents.dobj;

/**
 * A common parent class for all events that are associated with a name
 * (in some cases a field name, in other cases just an identifying name).
 */
public abstract class NamedEvent extends DEvent
{
    /**
     * Constructs a new named event for the specified target object with
     * the supplied lock name.
     *
     * @param targetOid the object id of the object in question.
     * @param name the name of the lock to release.
     */
    public NamedEvent (int targetOid, String name)
    {
        super(targetOid);
        _name = name;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public NamedEvent ()
    {
    }

    /**
     * Returns the name of the lock to release.
     */
    public String getName ()
    {
        return _name;
    }

    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", name=").append(_name);
    }

    protected String _name;
}
