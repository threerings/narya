//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

/**
 * A AttributeChangeListener that listens for changes with a given name and calls
 * <code>namedAttributeChanged</code> when they occur.
 */
public abstract class NamedAttributeListener
    implements AttributeChangeListener
{
    /**
     * Listen for AttributeChangedEvent events with the given name.
     */
    public NamedAttributeListener (String name)
    {
        _name = name;
    }

    final public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(_name)) {
            namedAttributeChanged(event);
        }
    }

    /**
     * The attribute this listener is watching has changed.
     */
    public abstract void namedAttributeChanged (AttributeChangedEvent event);

    protected final String _name;
}
