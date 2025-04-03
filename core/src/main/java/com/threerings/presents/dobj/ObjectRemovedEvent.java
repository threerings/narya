//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * An object removed event is dispatched when an object is removed from an <code>OidList</code>
 * attribute of a distributed object. It can also be constructed to request the removal of an oid
 * from an <code>OidList</code> attribute of an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectRemovedEvent extends NamedEvent
{
    /**
     * Constructs a new object removed event on the specified target object with the supplied oid
     * list attribute name and object id to remove.
     *
     * @param targetOid the object id of the object from whose oid list we will remove an oid.
     * @param name the name of the attribute (data member) from which to remove the specified oid.
     * @param oid the oid to remove from the oid list attribute.
     */
    public ObjectRemovedEvent (int targetOid, String name, int oid)
    {
        super(targetOid, name);
        _oid = oid;
    }

    /**
     * Returns the oid that has been removed.
     */
    public int getOid ()
    {
        return _oid;
    }

    @Override
    public boolean alreadyApplied ()
    {
        return _alreadyApplied;
    }

    @Override
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        if (!_alreadyApplied) {
            OidList list = (OidList)target.getAttribute(_name);
            list.remove(_oid);
        }
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        if (listener instanceof OidListListener) {
            ((OidListListener)listener).objectRemoved(this);
        }
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        buf.append("OBJREM:");
        super.toString(buf);
        buf.append(", oid=").append(_oid);
    }

    /** Used by {@link DObject} to note if this event has already been applied locally. */
    protected ObjectRemovedEvent setAlreadyApplied (boolean alreadyApplied)
    {
        _alreadyApplied = alreadyApplied;
        return this;
    }

    protected int _oid;
    protected transient boolean _alreadyApplied;
}
