//
// $Id: ClusterObject.java,v 1.2 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.OidList;

/**
 * Used to dispatch chat in clusters.
 */
public class ClusterObject extends DObject
{
    /** The field name of the <code>occupants</code> field. */
    public static final String OCCUPANTS = "occupants";

    /**
     * Tracks the oid of the body objects that occupy this cluster.
     */
    public OidList occupants = new OidList();

    /**
     * Requests that the specified oid be added to the
     * <code>occupants</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    public void addToOccupants (int oid)
    {
        requestOidAdd(OCCUPANTS, oid);
    }

    /**
     * Requests that the specified oid be removed from the
     * <code>occupants</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromOccupants (int oid)
    {
        requestOidRemove(OCCUPANTS, oid);
    }
}
