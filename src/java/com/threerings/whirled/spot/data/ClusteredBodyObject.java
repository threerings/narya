//
// $Id: ClusteredBodyObject.java,v 1.2 2003/05/06 00:21:59 mdb Exp $

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
