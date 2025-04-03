//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import com.samskivert.util.StringUtil;

import static com.threerings.presents.Log.log;

/**
 * An entry updated event is dispatched when an entry of a {@link DSet} is updated. It can also be
 * constructed to request the update of an entry and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 *
 * @param <T> the type of entry being handled by this event. This must match the type on the set
 * that generated this event.
 */
public class EntryUpdatedEvent<T extends DSet.Entry> extends EntryEvent<T>
{
    /**
     * Constructs a new entry updated event on the specified target object for the specified set
     * name and with the supplied updated entry.
     *
     * @param targetOid the object id of the object to whose set we will add an entry.
     * @param name the name of the attribute in which to update the specified entry.
     * @param entry the entry to update.
     */
    public EntryUpdatedEvent (int targetOid, String name, T entry)
    {
        super(targetOid, name);
        _entry = entry;
    }

    @Override
    public Comparable<?> getKey ()
    {
        return _entry.getKey();
    }

    /**
     * {@inheritDoc}
     * This implementation never returns <code>null</code>.
     */
    @Override
    public T getEntry ()
    {
        return _entry;
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
        // only apply the change if we haven't already
        if (!alreadyApplied()) {
            DSet<T> set = target.getSet(_name);
            // fetch the previous value for interested callers
            _oldEntry = set.update(_entry);
            if (_oldEntry == null) {
                // complain if we didn't update anything
                log.warning("No matching entry to update", "entry", this, "set", set);
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
            setlist.entryUpdated(this);
        }
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        buf.append("ELUPD:");
        super.toString(buf);
        buf.append(", entry=");
        StringUtil.toString(buf, _entry);
    }

    protected EntryUpdatedEvent<T> setOldEntry (T oldEntry)
    {
        _oldEntry = oldEntry;
        return this;
    }

    protected T _entry;

    @SuppressWarnings("unchecked")
    protected transient T _oldEntry = (T)UNSET_OLD_ENTRY;
}
