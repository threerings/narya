//
// $Id: Cluster.java,v 1.2 2003/02/13 23:01:35 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.presents.dobj.DSet;

/**
 * Contains information on clusters.
 */
public class Cluster
    implements DSet.Entry
{
    /** A unique identifier for this cluster (also the distributed object
     * id of the cluster chat object). */
    public int clusterOid;

    /** The x-coordinate of the cluster in the scene. */
    public int x;

    /** The y-coordinate of the cluster in the scene. */
    public int y;

    /** The number of occupants in this cluster. */
    public int occupants;

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

    /** Used for {@link #geyKey}. */
    protected transient Integer _key;
}
