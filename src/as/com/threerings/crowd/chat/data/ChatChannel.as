//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.chat.data {

import com.threerings.io.SimpleStreamableObject;
import com.threerings.util.Comparable;
import com.threerings.util.Equalable;
import com.threerings.util.Hashable;

import com.threerings.presents.dobj.DSet_Entry;

/**
 * Represents a chat channel.
 */
public /*abstract*/ class ChatChannel extends SimpleStreamableObject
    implements Comparable, Hashable, Equalable, DSet_Entry
{
    // from interface Comparable
    public function compareTo (other :Object) :int
    {
        throw new Error("abstract");
    }

    // from interface Hashable
    public function hashCode () :int
    {
        throw new Error("abstract");
    }

    // from interface Equalable
    public function equals (other :Object) :Boolean
    {
        return compareTo(other) == 0;
    }

    // from interface DSet_Entry
    public function getKey () :Object
    {
        return this;
    }
}
}
