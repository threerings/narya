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

import com.threerings.util.StreamableArrayList;
import com.threerings.util.StringBuilder;

/**
 * Used to manage and submit groups of events on a collection of distributed objects in a single
 * transaction.
 *
 * @see DObject#startTransaction
 */
public class CompoundEvent extends DEvent
{
    /**
     * Constructs a compound event and prepares it for operation.
     */
    public function CompoundEvent (targetOid :int = 0)
    {
        super(targetOid);

        if (targetOid != 0) {
            _events = new StreamableArrayList();
        }
    }

    /**
     * Posts an event to this transaction. The event will be delivered as part of the entire
     * transaction if it is committed or discarded if the transaction is cancelled.
     */
    public function postEvent (event :DEvent) :void
    {
        _events.add(event);
    }

    /**
     * Returns the list of events contained within this compound event.  Don't mess with it.
     */
    public function getEvents () :Array
    {
        return _events.asArray();
    }

    /**
     * Commits this transaction by posting this event to the distributed object event queue.
     */
    public function commit (omgr :DObjectManager) :void
    {
        // post this event onto the queue (but only if we actually accumulated some events)
        switch (_events.size()) {
        case 0: // nothing doing
            break;
        case 1: // no point in being compound
            omgr.postEvent(_events.get(0) as DEvent);
            break;
        default: // now we're talking
            omgr.postEvent(this);
            break;
        }
    }

    // from DObject
    override public function applyToObject (target :DObject) :Boolean
        // throws ObjectAccessException
    {
        return false;
    }

    // from DObject
    override protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("COMPOUND:");
        super.toStringBuf(buf);

        var nn :int = _events.size();
        for (var ii :int = 0; ii < nn; ii++) {
            buf.append(", ", _events.get(ii));
        }
    }

    // from DObject
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(_events);
    }

    // from DObject
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _events = (ins.readObject() as StreamableArrayList);
    }

    /** A list of the events associated with this compound event. */
    protected var _events :StreamableArrayList;
}
}
