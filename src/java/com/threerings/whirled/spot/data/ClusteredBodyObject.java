//
// $Id: ClusteredBodyObject.java,v 1.3 2004/02/25 14:50:28 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.whirled.data.ScenedBodyObject;

/**
 * Defines some required methods for a {@link BodyObject} that is to
 * participate in the Whirled Spot system.
 */
public interface ClusteredBodyObject extends ScenedBodyObject
{
    /**
     * Returns the field name of the cluster oid distributed object field.
     */
    public String getClusterField ();

    /**
     * Returns the oid of the cluster to which this user currently
     * belongs.
     */
    public int getClusterOid ();

    /**
     * Sets the oid of the cluster to which this user currently belongs.
     */
    public void setClusterOid (int clusterOid);
}
