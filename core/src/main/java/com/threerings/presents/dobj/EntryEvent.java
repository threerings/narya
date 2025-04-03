//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * A common parent class for DSet entry events.
 */
public abstract class EntryEvent<T extends DSet.Entry> extends NamedEvent
{
    /**
     * Constructs a new event for the specified target object with the supplied attribute name.
     *
     * @param targetOid the object id of the object in question.
     * @param name the name associated with this event.
     */
    public EntryEvent (int targetOid, String name)
    {
        super(targetOid, name);
    }

    /**
     * Return the key that identifies the entry related to this event.
     * Never returns <code>null</code>.
     */
    public abstract Comparable<?> getKey ();

    /**
     * Return the <em>new or updated</em> entry, or <code>null</code> if the entry was removed.
     */
    public abstract T getEntry ();

    /**
     * Return the <em>old</em> entry, or <code>null</code> if the entry is newly added.
     */
    public abstract T getOldEntry ();
}
