//
// $Id: EntryAddedEvent.java,v 1.9 2002/12/20 23:29:04 mdb Exp $

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
        super(targetOid, name);
        _entry = entry;
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
        DSet set = (DSet)target.getAttribute(_name);
        set.add(_entry);
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
}
