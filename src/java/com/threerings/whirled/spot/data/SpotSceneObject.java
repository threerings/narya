//
// $Id: SpotSceneObject.java,v 1.1 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.presents.dobj.DSet;
import com.threerings.whirled.data.SceneObject;

/**
 * Extends the {@link SceneObject} with information specific to spots.
 */
public class SpotSceneObject extends SceneObject
{
    /** The field name of the <code>occupantLocs</code> field. */
    public static final String OCCUPANT_LOCS = "occupantLocs";

    /** A distributed set containing {@link SceneLocation} records for all
     * occupants of this scene. */
    public DSet occupantLocs = new DSet();

    /**
     * Requests that the specified entry be added to the
     * <code>occupantLocs</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToOccupantLocs (DSet.Entry elem)
    {
        requestEntryAdd(OCCUPANT_LOCS, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>occupantLocs</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromOccupantLocs (Object key)
    {
        requestEntryRemove(OCCUPANT_LOCS, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>occupantLocs</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateOccupantLocs (DSet.Entry elem)
    {
        requestEntryUpdate(OCCUPANT_LOCS, elem);
    }

    /**
     * Requests that the <code>occupantLocs</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setOccupantLocs (DSet occupantLocs)
    {
        this.occupantLocs = occupantLocs;
        requestAttributeChange(OCCUPANT_LOCS, occupantLocs);
    }
}
