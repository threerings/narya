//
// $Id: BodyObject.java,v 1.5 2002/10/30 00:42:37 mdb Exp $

package com.threerings.crowd.data;

import com.threerings.presents.data.ClientObject;

public class BodyObject extends ClientObject
{
    /** The field name of the <code>username</code> field. */
    public static final String USERNAME = "username";

    /** The field name of the <code>location</code> field. */
    public static final String LOCATION = "location";

    /** The field name of the <code>status</code> field. */
    public static final String STATUS = "status";

    /**
     * The username associated with this body object.
     */
    public String username;

    /**
     * The oid of the place currently occupied by this body or -1 if they
     * currently occupy no place.
     */
    public int location = -1;

    /**
     * The user's current status ({@link OccupantInfo#ACTIVE}, etc.).
     */
    public byte status;

    /**
     * The time at which the {@link #status} field was last updated. This
     * is only available on the server.
     */
    public transient long statusTime;

    // documentation inherited
    public String who ()
    {
        StringBuffer buf = new StringBuffer(username);
        buf.append(" (").append(getOid());
        if (status != OccupantInfo.ACTIVE) {
            buf.append(" ").append(OccupantInfo.X_STATUS[status]);
        }
        return buf.append(")").toString();
    }

    /**
     * Requests that the <code>username</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setUsername (String username)
    {
        this.username = username;
        requestAttributeChange(USERNAME, username);
    }

    /**
     * Requests that the <code>location</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setLocation (int location)
    {
        this.location = location;
        requestAttributeChange(LOCATION, new Integer(location));
    }

    /**
     * Requests that the <code>status</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setStatus (byte status)
    {
        this.status = status;
        requestAttributeChange(STATUS, new Byte(status));
    }
}
