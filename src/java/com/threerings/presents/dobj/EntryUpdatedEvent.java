//
// $Id: EntryUpdatedEvent.java,v 1.10 2003/03/10 18:29:54 mdb Exp $

package com.threerings.presents.dobj;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;

/**
 * An entry updated event is dispatched when an entry of a {@link DSet} is
 * updated. It can also be constructed to request the update of an entry
 * and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryUpdatedEvent extends NamedEvent
{
    /**
     * Constructs a new entry updated event on the specified target object
     * for the specified set name and with the supplied updated entry.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an entry.
     * @param name the name of the attribute in which to update the
     * specified entry.
     * @param entry the entry to update.
     */
    public EntryUpdatedEvent (int targetOid, String name, DSet.Entry entry)
    {
        this(targetOid, name, entry, null);
    }

    /**
     * Used when the distributed object already updated the entry before
     * generating the event.
     */
    public EntryUpdatedEvent (int targetOid, String name, DSet.Entry entry,
                              DSet.Entry oldEntry)
    {
        super(targetOid, name);
        _entry = entry;
        _oldEntry = oldEntry;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public EntryUpdatedEvent ()
    {
    }

    /**
     * Returns the entry that has been updated.
     */
    public DSet.Entry getEntry ()
    {
        return _entry;
    }

    /**
     * Returns the entry that was in the set prior to being updated.
     */
    public DSet.Entry getOldEntry ()
    {
        return _oldEntry;
    }

    /**
     * Applies this event to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        if (_oldEntry == null) {
            DSet set = (DSet)target.getAttribute(_name);

            // fetch the previous value for interested callers
            _oldEntry = set.get(_entry.getKey());

            // update the entry
            if (!set.update(_entry)) {
                // complain if we didn't update anything
                Log.warning("No matching entry to update [entry=" + this +
                            ", set=" + set + "].");
                return false;
            }
        }
        return true;
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof SetListener) {
            ((SetListener)listener).entryUpdated(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("ELUPD:");
        super.toString(buf);
        buf.append(", entry=");
        StringUtil.toString(buf, _entry);
    }

    protected DSet.Entry _entry;
    protected transient DSet.Entry _oldEntry;
}
