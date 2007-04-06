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

/**
 * A message event that only goes to the server. If generated on the server then it never leaves
 * the server.
 */
public class ServerMessageEvent extends MessageEvent
{
    /**
     * Constructs a new message event on the specified target object with
     * the supplied name and arguments.
     *
     * @param targetOid the object id of the object whose attribute has
     * changed.
     * @param name the name of the message event.
     * @param args the arguments for this message. This array should
     * contain only values of valid distributed object types.
     */
    public function ServerMessageEvent (
            targetOid :int = 0, name :String = null, args :Array = null)
    {
        super(targetOid, name, args);
    }

// Note: isPrivate() is currently not implemented in actionscript, since
// it's a server-only attribute. Rest assured that this event will still
// only be server-only, as it will unserialize into a Java ServerMessageEvent
// on the server and then be properly private.
//    override public function isPrivate () :Boolean
//    {
//        // this is what makes us server-only
//        return true;
//    }
}
}
