//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
