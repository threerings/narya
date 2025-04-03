//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * An object added event is dispatched when an object is added to an <code>OidList</code> attribute
 * of a distributed object. It can also be constructed to request the addition of an oid to an
 * <code>OidList</code> attribute of an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectAddedEvent extends NamedEvent
{
    /**
     * Constructs a new object added event on the specified target object with the supplied oid
     * list attribute name and object id to add.
     *
     * @param targetOid the object id of the object to whose oid list we will add an oid.
     * @param name the name of the attribute (data member) to which to add the specified oid.
     * @param oid the oid to add to the oid list attribute.
     */
    public ObjectAddedEvent (int targetOid, String name, int oid)
    {
        super(targetOid, name);
        _oid = oid;
    }

    /**
     * Returns the oid that has been added.
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
            list.add(_oid);
        }
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        if (listener instanceof OidListListener) {
            ((OidListListener)listener).objectAdded(this);
        }
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        buf.append("OBJADD:");
        super.toString(buf);
        buf.append(", oid=").append(_oid);
    }

    /** Used by {@link DObject} to note if this event has already been applied locally. */
    protected ObjectAddedEvent setAlreadyApplied (boolean alreadyApplied)
    {
        _alreadyApplied = alreadyApplied;
        return this;
    }

    protected int _oid;
    protected transient boolean _alreadyApplied;
}
