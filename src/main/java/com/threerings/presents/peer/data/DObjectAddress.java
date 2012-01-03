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

package com.threerings.presents.peer.data;

import com.google.common.base.Objects;
import com.threerings.io.SimpleStreamableObject;

/**
 * Identifies a DObject on a peer.
 */
public class DObjectAddress extends SimpleStreamableObject
{
    /** The node on which this object lives.*/
    public final String nodeName;

    /** This object's oid in its node's oid space. */
    public final int oid;

    public DObjectAddress (String nodeName, int oid)
    {
        this.nodeName = nodeName;
        this.oid = oid;
    }

    @Override
    public int hashCode ()
    {
        return 31 * Objects.hashCode(nodeName) + oid;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (!(obj instanceof DObjectAddress)) {
            return false;
        }
        DObjectAddress o = (DObjectAddress)obj;
        return oid == o.oid
            && (nodeName == o.nodeName || (nodeName != null && nodeName.equals(o.nodeName)));
    }
}
