//
// $Id: ClusteredBodyObject.java,v 1.1 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.crowd.data.BodyObject;
import com.threerings.whirled.data.ScenedBodyObject;

/**
 * Defines some required methods for a {@link BodyObject} that is to
 * participate in the Whirled Spot system.
 */
public interface ClusteredBodyObject extends ScenedBodyObject
{
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
