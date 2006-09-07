//
// $Id: ForwardEventRequest.java 3099 2004-08-27 02:21:06Z mdb $
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

import com.threerings.presents.dobj.DEvent;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class ForwardEventRequest extends UpstreamMessage
{
    /**
     * Constructs a forward event request for the supplied event.
     */
    public function ForwardEventRequest (event :DEvent)
    {
        _event = event;
    }

    /**
     * Returns the event that we wish to have forwarded.
     */
    public function getEvent () :DEvent
    {
        return _event;
    }

    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(_event);
    }

    /** readObject omitted */

    public function toString () :String
    {
        return "[type=FWD, evt=" + _event + "]";
    }

    /** The event which we are forwarding. */
    protected var _event :DEvent;
}
}
