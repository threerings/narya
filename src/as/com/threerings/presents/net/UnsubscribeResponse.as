//
// $Id: UnsubscribeResponse.java 3099 2004-08-27 02:21:06Z mdb $
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

package com.threerings.presents.net {

import com.threerings.io.ObjectInputStream;

/**
 * Used to communicate to the client that we received their unsubscribe
 * request and that it is now OK to remove an object mapping from their
 * local object table.
 */
public class UnsubscribeResponse extends DownstreamMessage
{
    public function getOid () :int
    {
        return _oid;
    }

    public function toString () :String
    {
        return "[type=UNACK, msgid=" + messageId + ", oid=" + _oid + "]";
    }

    public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _oid = ins.readInt();
    }

    protected var _oid :int;
}
}
