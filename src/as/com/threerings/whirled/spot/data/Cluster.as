//
// $Id: Cluster.java 3310 2005-01-24 23:08:21Z mdb $
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

package com.threerings.whirled.spot.data {

import flash.geom.Rectangle;

import com.threerings.io.Streamable;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.dobj.DSet_Entry;

/**
 * Contains information on clusters.
 */
public class Cluster extends Rectangle
    implements DSet_Entry, Streamable
{
    /** A unique identifier for this cluster (also the distributed object
     * id of the cluster chat object). */
    public var clusterOid :int;

    // documentation inherited from interface DSet_Entry
    public function getKey () :Object
    {
        return clusterOid;
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(width);
        out.writeInt(height);
        out.writeInt(clusterOid);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        x = ins.readInt();
        y = ins.readInt();
        width = ins.readInt();
        height = ins.readInt();
        clusterOid = ins.readInt();
    }

    /**
     * Generates a string representation of this instance.
     */
    public function toString () :String
    {
        return super.toString() + ", clusterOid=" + clusterOid;
    }
}
}
