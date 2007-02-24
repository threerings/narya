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

import com.threerings.util.StringBuilder;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * A common parent class for all events that are associated with a name
 * (in some cases a field name, in other cases just an identifying name).
 */
public /* abstract */ class NamedEvent extends DEvent
{
    /**
     * Constructs a new named event for the specified target object with
     * the supplied attribute name.
     *
     * @param targetOid the object id of the object in question.
     * @param name the name associated with this event.
     */
    public function NamedEvent (targetOid :int, name :String)
    {
        super(targetOid);
        _name = name;
    }

    /**
     * Returns the name of the attribute to which this event pertains.
     */
    public function getName () :String
    {
        return _name;
    }

    override protected function toStringBuf (buf :StringBuilder) :void
    {
        super.toStringBuf(buf);
        buf.append(", name=", _name);
    }

    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeField(_name);
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _name = (ins.readField(String) as String);
    }

    /** The name of the event. */
    protected var _name :String;
}
}
