//
// $Id: PlaceObject.java,v 1.2 2002/02/08 23:54:25 mdb Exp $

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
     * <code>occupants</code> oid list.
     */
    public void addToOccupants (int oid)
    {
        requestOidAdd(OCCUPANTS, oid);
    }

    /**
     * Requests that the specified oid be removed from the
     * <code>occupants</code> oid list.
     */
    public void removeFromOccupants (int oid)
    {
        requestOidRemove(OCCUPANTS, oid);
    }

    /**
     * Requests that the specified element be added to the
     * <code>occupantInfo</code> set.
     */
    public void addToOccupantInfo (DSet.Element elem)
    {
        requestElementAdd(OCCUPANT_INFO, elem);
    }

    /**
     * Requests that the element matching the supplied key be removed from
     * the <code>occupantInfo</code> set.
     */
    public void removeFromOccupantInfo (Object key)
    {
        requestElementRemove(OCCUPANT_INFO, key);
    }

    /**
     * Requests that the specified element be updated in the
     * <code>occupantInfo</code> set.
     */
    public void updateOccupantInfo (DSet.Element elem)
    {
        requestElementUpdate(OCCUPANT_INFO, elem);
    }

    /**
     * Requests that the <code>occupantInfo</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * elements of a distributed set, but certain situations call for a
     * complete replacement of the set value.
     */
    public void setOccupantInfo (DSet value)
    {
        requestAttributeChange(OCCUPANT_INFO, value);
    }
}
