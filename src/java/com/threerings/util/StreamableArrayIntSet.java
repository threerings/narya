//
// $Id: StreamableArrayIntSet.java,v 1.3 2004/08/27 02:20:36 mdb Exp $
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

package com.threerings.util;

import java.io.IOException;

import com.samskivert.util.ArrayIntSet;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * A {@link ArrayIntSet} extension that can be streamed.
 */
public class StreamableArrayIntSet extends ArrayIntSet
    implements Streamable
{
    // documentation inherited
    public StreamableArrayIntSet (int[] values)
    {
        super(values);
    }

    // documentation inherited
    public StreamableArrayIntSet (int initialCapacity)
    {
        super(initialCapacity);
    }

    // documentation inherited
    public StreamableArrayIntSet ()
    {
        super();
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.writeInt(_size);
        out.writeObject(_values);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        _size = in.readInt();
        _values = (int[]) in.readObject();
    }
}
