//
// $Id: EntryAddedEvent.java,v 1.10 2003/03/10 18:29:54 mdb Exp $

package com.threerings.presents.dobj;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;

/**
 * An entry added event is dispatched when an entry is added to a {@link
 * DSet} attribute of a distributed entry. It can also be constructed to
 * request the addition of an entry to a set and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryAddedEvent extends NamedEvent
{
    /**
     * Constructs a new entry added event on the specified target object
     * with the supplied set attribute name and entry to add.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an entry.
     * @param name the name of the attribute to which to add the specified
     * entry.
     * @param entry the entry to add to the set attribute.
     */
    public EntryAddedEvent (int targetOid, String name, DSet.Entry entry)
    {
        this(targetOid, name, entry, false);
    }

    /**
     * Used when the distributed object already added the entry before
     * generating the event.
     */
    public EntryAddedEvent (int targetOid, String name, DSet.Entry entry,
                            boolean alreadyApplied)
    {
        super(targetOid, name);
        _entry = entry;
        _alreadyApplied = alreadyApplied;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public EntryAddedEvent ()
    {
    }

    /**
     * Returns the entry that has been added.
     */
    public DSet.Entry getEntry ()
    {
        return _entry;
    }

    /**
     * Applies this event to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        if (!_alreadyApplied) {
            DSet set = (DSet)target.getAttribute(_name);
            set.add(_entry);
        }
        return true;
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof SetListener) {
            ((SetListener)listener).entryAdded(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("ELADD:");
        super.toString(buf);
        buf.append(", entry=");
        StringUtil.toString(buf, _entry);
    }

    protected DSet.Entry _entry;

    /** Used when this event is generated on the authoritative server
     * where object changes are made immediately. This lets us know not to
     * apply ourselves when we're actually dispatched. */
    protected transient boolean _alreadyApplied;
}
