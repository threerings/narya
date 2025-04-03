//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * A common parent class for all events that are associated with a name
 * (in some cases a field name, in other cases just an identifying name).
 */
public abstract class NamedEvent extends DEvent
{
    /**
     * Constructs a new named event for the specified target object with
     * the supplied attribute name.
     *
     * @param targetOid the object id of the object in question.
     * @param name the name associated with this event.
     */
    public NamedEvent (int targetOid, String name)
    {
        super(targetOid);
        _name = name;
    }

    /**
     * Returns the name of the attribute to which this event pertains.
     */
    public String getName ()
    {
        return _name;
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        super.toString(buf);
        buf.append(", name=").append(_name);
    }

    protected String _name;
}
