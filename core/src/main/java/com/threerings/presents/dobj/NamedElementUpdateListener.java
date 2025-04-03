//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * An ElementUpdateListener that listens for changes with a given name and calls
 * namedElementUpdated when they occur.
 */
public abstract class NamedElementUpdateListener
    implements ElementUpdateListener
{
    /**
     * Listen for element updates with the given name.
     */
    public NamedElementUpdateListener (String name)
    {
        _name = name;
    }

    final public void elementUpdated (ElementUpdatedEvent event)
    {
        if (event.getName().equals(_name)) {
            namedElementUpdated(event);
        }
    }

    abstract protected void namedElementUpdated (ElementUpdatedEvent event);

    protected final String _name;
}
