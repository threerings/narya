//
// $Id: Cluster.java,v 1.1 2003/02/13 21:55:22 mdb Exp $

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

    /** Used for {@link #geyKey}. */
    protected transient Integer _key;
}
