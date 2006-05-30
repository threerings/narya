//
// $Id: ObjectDestroyedEvent.java 3099 2004-08-27 02:21:06Z mdb $
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

package com.threerings.presents.dobj {

import com.threerings.util.StringBuilder;

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
     * Constructs a new object destroyed event for the specified
     * distributed object.
     *
     * @param targetOid the object id of the object that will be destroyed.
     */
    public function ObjectDestroyedEvent (targetOid :int = 0)
    {
        super(targetOid);
    }

    // documentation inherited
    public override function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        // nothing to do in preparation for destruction, the omgr will
        // have to recognize this type of event and do the right thing
        return true;
    }

    // documentation inherited
    protected override function notifyListener (listener :Object) :void
    {
        if (listener is ObjectDeathListener) {
            listener.objectDestroyed(this);
        }
    }

    // documentation inherited
    protected override function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("DESTROY:");
        super.toStringBuf(buf);
    }
}
}
