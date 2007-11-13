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

package com.threerings.presents.dobj {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.util.Log;
import com.threerings.util.StringBuilder;

/**
 * An entry added event is dispatched when an entry is added to a {@link
 * DSet} attribute of a distributed entry. It can also be constructed to
 * request the addition of an entry to a set and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryAddedEvent extends NamedEvent
{
    /**
     * Constructs a new entry added event on the specified target object
     * with the supplied set attribute name and entry to add.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an entry.
     * @param name the name of the attribute to which to add the specified
     * entry.
     * @param entry the entry to add to the set attribute.
     */
    public function EntryAddedEvent (
            targetOid :int = 0, name :String = null, entry :DSet_Entry = null)
    {
        super(targetOid, name);

        // only init these values if they were specified
        if (arguments.length > 0) {
            _entry = entry;
        }
    }

    /**
     * Returns the entry that has been added.
     */
    public function getEntry () :DSet_Entry
    {
        return _entry;
    }

    /**
     * Applies this event to the object.
     */
    override public function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        var added :Boolean = target[_name].add(_entry);
        if (!added) {
            Log.getLog(this).warning(
                "Duplicate entry found [event=" + this + "].");
        }
        return true;
    }

    // documentation inherited
    override protected function notifyListener (listener :Object) :void
    {
        if (listener is SetListener) {
            listener.entryAdded(this);
        }
    }

    // documentation inherited
    override protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("ELADD:");
        super.toStringBuf(buf);
        buf.append(", entry=", _entry);
    }

    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(_entry);
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _entry = (ins.readObject() as DSet_Entry);
    }

    protected var _entry :DSet_Entry;
}
}
