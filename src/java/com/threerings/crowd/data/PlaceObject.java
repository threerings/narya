//
// $Id: PlaceObject.java,v 1.5 2002/03/18 23:21:26 mdb Exp $

package com.threerings.crowd.data;

import com.threerings.presents.dobj.*;

public class PlaceObject extends DObject
{
    /** The field name of the <code>occupants</code> field. */
    public static final String OCCUPANTS = "occupants";

    /** The field name of the <code>occupantInfo</code> field. */
    public static final String OCCUPANT_INFO = "occupantInfo";

    /**
     * Tracks the oid of the body objects of all of the occupants of this
     * place.
     */
    public OidList occupants = new OidList();

    /**
     * Contains an info record (of type {@link OccupantInfo} for each
     * occupant that contains information about that occupant that needs
     * to be known by everyone in the place.
     */
    public DSet occupantInfo = new DSet();

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

    /**
     * Requests that the specified entry be added to the
     * <code>occupantInfo</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToOccupantInfo (DSet.Entry elem)
    {
        requestEntryAdd(OCCUPANT_INFO, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>occupantInfo</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromOccupantInfo (Object key)
    {
        requestEntryRemove(OCCUPANT_INFO, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>occupantInfo</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateOccupantInfo (DSet.Entry elem)
    {
        requestEntryUpdate(OCCUPANT_INFO, elem);
    }

    /**
     * Requests that the <code>occupantInfo</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setOccupantInfo (DSet occupantInfo)
    {
        this.occupantInfo = occupantInfo;
        requestAttributeChange(OCCUPANT_INFO, occupantInfo);
    }
}
