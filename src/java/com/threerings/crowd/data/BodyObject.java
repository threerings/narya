//
// $Id: BodyObject.java,v 1.2 2002/02/20 23:35:42 mdb Exp $

package com.threerings.crowd.data;

import com.threerings.presents.data.ClientObject;

public class BodyObject extends ClientObject
{
    /** The field name of the <code>username</code> field. */
    public static final String USERNAME = "username";

    /** The field name of the <code>location</code> field. */
    public static final String LOCATION = "location";

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
}
