//
// $Id: Cluster.java,v 1.4 2003/03/26 02:37:08 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.presents.dobj.DSet;

/**
 * Contains information on clusters.
 */
public class Cluster extends SimpleStreamableObject
    implements DSet.Entry
{
    /** A unique identifier for this cluster (also the distributed object
     * id of the cluster chat object). */
    public int clusterOid;

    /** The bounds of the cluster in the scene. */
    public int x, y, width, height;

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
