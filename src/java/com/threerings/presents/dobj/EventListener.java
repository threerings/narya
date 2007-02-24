//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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
 * Implemented by entites which wish to hear about all events being
 * dispatched on a particular distributed object.
 *
 * @see DObject#addListener
 */
public interface EventListener extends ChangeListener
{
    /**
     * Called when any event has been dispatched on an object. The event
     * will be of the derived class that corresponds to the kind of event
     * that occurred on the object. This will be called <em>after</em> the
     * event has been applied to the object. So fetching an attribute upon
     * receiving an attribute changed event will provide the new value for
     * the attribute.
     *
     * @param event The event that was dispatched on the object.
     */
    public void eventReceived (DEvent event);
}
