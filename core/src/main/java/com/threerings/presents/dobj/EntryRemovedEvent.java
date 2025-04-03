//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import static com.threerings.presents.Log.log;

/**
 * An entry removed event is dispatched when an entry is removed from a {@link DSet} attribute of a
 * distributed object. It can also be constructed to request the removal of an entry from a set and
 * posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 *
 * @param <T> the type of entry being handled by this event. This must match the type on the set
 * that generated this event.
 */
public class EntryRemovedEvent<T extends DSet.Entry> extends EntryEvent<T>
{
    /**
     * Constructs a new entry removed event on the specified target object with the supplied set
     * attribute name and entry key to remove.
     *
     * @param targetOid the object id of the object from whose set we will remove an entry.
     * @param name the name of the attribute from which to remove the specified entry.
     * @param key the entry key that identifies the entry to remove.
     */
    public EntryRemovedEvent (int targetOid, String name, Comparable<?> key)
    {
        super(targetOid, name);
        _key = key;
    }

    @Override
    public Comparable<?> getKey ()
    {
        return _key;
    }

    /**
     * {@inheritDoc}
     * This implementation always returns <code>null</code>.
     */
    @Override
    public T getEntry ()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     * This implementation never returns <code>null</code>.
     */
    @Override
    public T getOldEntry ()
    {
        return _oldEntry;
    }

    @Override
    public boolean alreadyApplied ()
    {
        return (_oldEntry != UNSET_OLD_ENTRY);
    }

    @Override
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        if (!alreadyApplied()) {
            DSet<T> set = target.getSet(_name);
            // remove, fetch the previous value for interested callers
            _oldEntry = set.removeKey(_key);
            if (_oldEntry == null) {
                // complain if there was actually nothing there
                log.warning("No matching entry to remove", "key", _key, "set", set);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        if (listener instanceof SetListener<?>) {
            @SuppressWarnings("unchecked") SetListener<T> setlist = (SetListener<T>)listener;
            setlist.entryRemoved(this);
        }
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        buf.append("ELREM:");
        super.toString(buf);
        buf.append(", key=").append(_key);
    }

    protected EntryRemovedEvent<T> setOldEntry (T oldEntry)
    {
        _oldEntry = oldEntry;
        return this;
    }

    protected Comparable<?> _key;

    @SuppressWarnings("unchecked")
    protected transient T _oldEntry = (T)UNSET_OLD_ENTRY;
}
