//
// $Id: BodyObject.java,v 1.1 2002/02/08 23:10:36 mdb Exp $

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
     * value.
     */
    public void setUsername (String username)
    {
        requestAttributeChange(USERNAME, username);
    }

    /**
     * Requests that the <code>username</code> field be set to the
     * specified value and immediately updates the state of the object
     * to reflect the change. This should <em>only</em> be called on the
     * server and only then if you know what you're doing.
     */
    public void setUsernameImmediate (String username)
    {
        this.username = username;
        requestAttributeChange(USERNAME, username);
    }

    /**
     * Requests that the <code>location</code> field be set to the specified
     * value.
     */
    public void setLocation (int location)
    {
        requestAttributeChange(LOCATION, new Integer(location));
    }

    /**
     * Requests that the <code>location</code> field be set to the
     * specified value and immediately updates the state of the object
     * to reflect the change. This should <em>only</em> be called on the
     * server and only then if you know what you're doing.
     */
    public void setLocationImmediate (int location)
    {
        this.location = location;
        requestAttributeChange(LOCATION, new Integer(location));
    }
}
