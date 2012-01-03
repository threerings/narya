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

package com.threerings.presents.dobj {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.util.Joiner;

public /* abstract */ class DEvent
    implements Streamable
{
    public function DEvent (targetOid :int = 0)
    {
        // TODO: Remove when https://bugzilla.mozilla.org/show_bug.cgi?id=433103 is fixed
        if (UNSET_OLD_ENTRY == null) {
            UNSET_OLD_ENTRY = new DummyEntry();
        }
        _toid = targetOid;
    }

    /**
     * Returns the oid of the object that is the target of this event.
     */
    public function getTargetOid () :int
    {
        return _toid;
    }


    /**
     * Some events are used only internally on the server and need not be
     * broadcast to subscribers, proxy or otherwise. Such events can
     * return true here and short-circuit the normal proxy event dispatch
     * mechanism.
     */
    public function applyToObject (target :DObject) :Boolean
    {
        throw new Error("abstract");
    }

    /**
     * We want to make the notifyListener method visible to DObject.
     */
    internal function friendNotifyListener (listener :Object) :void
    {
        notifyListener(listener);
    }

    /**
     * Events with associated listener interfaces should implement this
     * function and notify the supplied listener if it implements their
     * event listening interface. For example, the {@link
     * AttributeChangedEvent} will notify listeners that implement {@link
     * AttributeChangeListener}.
     */
    protected function notifyListener (listener :Object) :void
    {
        // the default is to do nothing
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(_toid);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        _toid = ins.readInt();
    }

    /**
     * Constructs and returns a string representation of this event.
     */
    public function toString () :String
    {
        var j :Joiner = Joiner.createFor(this);
        toStringJoiner(j);
        return j.toString();
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toStringJoiner()</code> first) to append the derived class
     * specific event information onto the String representation.
     */
    protected function toStringJoiner (j :Joiner) :void
    {
        j.add("targetOid", _toid);
    }

    /** The oid of the object that is the target of this event. */
    protected var _toid :int;

    protected static var UNSET_OLD_ENTRY :DSet_Entry;
}
}
