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

package com.threerings.presents.peer.data;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;
import com.threerings.util.Name;

import com.threerings.presents.dobj.DSet;

/**
 * Contains information on a particular client.
 */
public class ClientInfo extends SimpleStreamableObject
    implements DSet.Entry
{
    /** The username used by this client to authenticate. */
    public Name username;

    // documentation inherited from interface DSet.Entry
    public Comparable getKey ()
    {
        return username;
    }

    // AUTO-GENERATED: METHODS START
    // from interface Streamable
    public void readObject (ObjectInputStream ins)
        throws IOException, ClassNotFoundException
    {
        username = (Name)ins.readObject();
    }

    // from interface Streamable
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.writeObject(username);
    }
    // AUTO-GENERATED: METHODS END
}
