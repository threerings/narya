//
// $Id: ClusterObject.java,v 1.3 2003/06/14 00:55:40 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.OidList;

import com.threerings.crowd.chat.data.SpeakObject;

/**
 * Used to dispatch chat in clusters.
 */
public class ClusterObject extends DObject
    implements SpeakObject
{
    /** The field name of the <code>occupants</code> field. */
    public static final String OCCUPANTS = "occupants";

    /**
     * Tracks the oid of the body objects that occupy this cluster.
     */
    public OidList occupants = new OidList();

    // documentation inherited
    public void applyToListeners (ListenerOp op)
    {
        for (int ii = 0, ll = occupants.size(); ii < ll; ii++) {
            op.apply(occupants.get(ii));
        }
    }

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
