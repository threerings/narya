//
// $Id: ReleaseLockEvent.java,v 1.9 2004/08/27 02:20:20 mdb Exp $
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
 * A release lock event is dispatched at the end of a chain of events to
 * release a lock that is intended to prevent some application defined
 * activity from happening until those events have been processed. This is
 * an entirely cooperative locking system, meaning that the application
 * will have to explicitly attempt acquisition of the lock to find out if
 * the lock has yet been released. These locks don't actually prevent any
 * of the distributed object machinery from functioning.
 *
 * @see DObjectManager#postEvent
 */
public class ReleaseLockEvent extends NamedEvent
{
    /**
     * Constructs a new release lock event for the specified target object
     * with the supplied lock name.
     *
     * @param targetOid the object id of the object in question.
     * @param name the name of the lock to release.
     */
    public ReleaseLockEvent (int targetOid, String name)
    {
        super(targetOid, name);
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public ReleaseLockEvent ()
    {
    }

    // documentation inherited
    public boolean isPrivate ()
    {
        // we need only run on the server; no need to propagate to proxies
        return true;
    }

    /**
     * Applies this lock release to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // clear this lock from the target object
        target.clearLock(_name);
        // no need to notify subscribers about these sorts of events
        return false;
    }

    protected void toString (StringBuffer buf)
    {
        buf.append("UNLOCK:");
        super.toString(buf);
    }
}
