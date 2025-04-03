//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * An object destroyed event is dispatched when an object has been removed
 * from the distributed object system. It can also be constructed to
 * request an attribute change on an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectDestroyedEvent extends DEvent
{
    /**
     * Constructs a new object destroyed event for the specified distributed object.
     *
     * @param targetOid the object id of the object that will be destroyed.
     */
    public ObjectDestroyedEvent (int targetOid)
    {
        super(targetOid);
    }

    @Override
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // nothing to do in preparation for destruction, the omgr will
        // have to recognize this type of event and do the right thing
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        if (listener instanceof ObjectDeathListener) {
            ((ObjectDeathListener)listener).objectDestroyed(this);
        }
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        buf.append("DESTROY:");
        super.toString(buf);
    }
}
