//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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
 * The root distributed object manager extends the basic distributed
 * object manager interface with methods that can only be guaranteed to
 * work in the virtual machine that is hosting the distributed objects in
 * question. VMs that operate proxies of objects can only implement the
 * basic distributed object manager interface.
 */
public interface RootDObjectManager extends DObjectManager
{
    /**
     * Looks up and returns the requested distributed object in the dobj
     * table, returning null if no object exists with that oid.
     */
    public DObject getObject (int oid);

    /**
     * Registers a distributed object instance of the supplied class with the
     * system and assigns it an oid. When the call returns the object will be
     * registered with the system and its oid will have been assigned.
     *
     * @return the registered object for the caller's convenience.
     */
    public <T extends DObject> T registerObject (T object);

    /**
     * Requests that the specified object be destroyed. Once destroyed an
     * object is removed from the runtime system and may no longer have
     * events dispatched on it.
     *
     * @param oid The object id of the distributed object to be destroyed.
     */
    public void destroyObject (int oid);
}
