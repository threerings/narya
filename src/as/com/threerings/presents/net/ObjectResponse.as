//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.net {

import com.threerings.presents.dobj.DObject;

import com.threerings.io.ObjectInputStream;

public class ObjectResponse extends DownstreamMessage
{
    public function getObject () :DObject
    {
        return _dobj;
    }

    public function toString () :String
    {
        return "[type=ORSP, msgid=" + messageId + ", obj=" + _dobj + "]";
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _dobj = DObject(ins.readObject());
    }

    /** The object which is associated with this response. */
    protected var _dobj :DObject;
}
}
