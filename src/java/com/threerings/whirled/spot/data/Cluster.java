//
// $Id: Cluster.java,v 1.7 2004/02/25 14:50:28 mdb Exp $

package com.threerings.whirled.spot.data;

import java.awt.Rectangle;

import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;

import com.threerings.presents.dobj.DSet;

/**
 * Contains information on clusters.
 */
public class Cluster extends Rectangle
    implements DSet.Entry, Streamable
{
    /** A unique identifier for this cluster (also the distributed object
     * id of the cluster chat object). */
    public int clusterOid;

    // documentation inherited
    public Comparable getKey ()
    {
        if (_key == null) {
            _key = new Integer(clusterOid);
        }
        return _key;
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        if (other instanceof Cluster) {
            return ((Cluster)other).clusterOid == clusterOid;
        } else {
            return false;
        }
    }

    // documentation inherited
    public int hashCode ()
    {
        return clusterOid;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    /** Used for {@link #geyKey}. */
    protected transient Integer _key;
}
