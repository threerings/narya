//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
