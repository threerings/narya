//
// $Id: EntryRemovedEvent.java,v 1.11 2002/12/20 23:29:04 mdb Exp $

package com.threerings.presents.dobj;

/**
 * An entry removed event is dispatched when an entry is removed from a
 * {@link DSet} attribute of a distributed object. It can also be
 * constructed to request the removal of an entry from a set and posted to
 * the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryRemovedEvent extends NamedEvent
{
    /**
     * Constructs a new entry removed event on the specified target object
     * with the supplied set attribute name and entry key to remove.
     *
     * @param targetOid the object id of the object from whose set we will
     * remove an entry.
     * @param name the name of the attribute from which to remove the
     * specified entry.
     * @param key the entry key that identifies the entry to remove.
     */
    public EntryRemovedEvent (int targetOid, String name, Object key)
    {
        super(targetOid, name);
        _key = key;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public EntryRemovedEvent ()
    {
    }

    /**
     * Returns the key that identifies the entry that has been removed.
     */
    public Object getKey ()
    {
        return _key;
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
        DSet set = (DSet)target.getAttribute(_name);
        // fetch the previous value for interested callers
        _oldEntry = set.get(_key);
        // remove it from the set
        set.removeKey(_key);
        return true;
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof SetListener) {
            ((SetListener)listener).entryRemoved(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("ELREM:");
        super.toString(buf);
        buf.append(", key=").append(_key);
    }

    protected Object _key;
    protected transient DSet.Entry _oldEntry;
}
