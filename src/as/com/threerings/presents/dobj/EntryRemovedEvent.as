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
import com.threerings.util.Wrapped;

/**
 * An entry removed event is dispatched when an entry is removed from a
 * {@link DSet} attribute of a distributed object. It can also be
 * constructed to request the removal of an entry from a set and posted to
 * the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryRemovedEvent extends NamedEvent
{
    /**
     * Constructs a new entry removed event on the specified target object
     * with the supplied set attribute name and entry key to remove.
     *
     * @param targetOid the object id of the object from whose set we will
     * remove an entry.
     * @param name the name of the attribute from which to remove the
     * specified entry.
     * @param key the entry key that identifies the entry to remove.
     * @param oldEntry the previous value of the entry.
     */
    public function EntryRemovedEvent (
            targetOid :int = 0, name :String = null, key :Object = null,
            oldEntry :DSet_Entry = null)
    {
        super(targetOid, name);

        // only init these values if they were specified
        if (arguments.length > 0) {
            _key = key;
            _oldEntry = oldEntry;
        }
    }

    /**
     * Returns the key that identifies the entry that has been removed.
     */
    public function getKey () :Object
    {
        return _key;
    }

    /**
     * Returns the entry that was in the set prior to being updated.
     */
    public function getOldEntry () :DSet_Entry
    {
        return _oldEntry;
    }

    /**
     * Applies this event to the object.
     */
    override public function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        if (_oldEntry == UNSET_OLD_ENTRY) {
            var dset :DSet = target[_name];
            // remove, fetch the previous value for interested callers
            _oldEntry = dset.removeKey(_key);
            if (_oldEntry == null) {
                // complain if there was actually nothing there
                Log.getLog(this).warning("No matching entry to remove " +
                    "[key=" + _key + ", set=" + dset + "].");
                return false;
            }
        }
        return true;
    }

    // documentation inherited
    override protected function notifyListener (listener :Object) :void
    {
        if (listener is SetListener) {
            listener.entryRemoved(this);
        }
    }

    // documentation inherited
    override protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("ELREM:");
        super.toStringBuf(buf);
        buf.append(", key=", _key);
    }

    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(_key);
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _key = ins.readObject();

        if (_key is Wrapped) {
            _key = (_key as Wrapped).unwrap();
        }
    }

    protected var _key :Object;
    protected var _oldEntry :DSet_Entry = UNSET_OLD_ENTRY;
}
}
