//
// $Id: BodyObject.java,v 1.10 2004/08/27 02:12:33 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.data;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.crowd.chat.data.SpeakObject;

/**
 * The basic user object class for Crowd users. Bodies have a username, a
 * location and a status.
 */
public class BodyObject extends ClientObject
    implements SpeakObject
{
    /** The field name of the <code>username</code> field. */
    public static final String USERNAME = "username";

    /** The field name of the <code>location</code> field. */
    public static final String LOCATION = "location";

    /** The field name of the <code>status</code> field. */
    public static final String STATUS = "status";

    /** The field name of the <code>awayMessage</code> field. */
    public static final String AWAY_MESSAGE = "awayMessage";

    /**
     * The username associated with this body object.
     */
    public Name username;

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

    /**
     * If non-null, this contains a message to be auto-replied whenever
     * another user delivers a tell message to this user.
     */
    public String awayMessage;

    // documentation inherited
    public void applyToListeners (ListenerOp op)
    {
        op.apply(getOid());
    }

    // documentation inherited
    public String who ()
    {
        StringBuffer buf = new StringBuffer(username.toString());
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
    public void setUsername (Name username)
    {
        requestAttributeChange(USERNAME, username);
        this.username = username;
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
        requestAttributeChange(LOCATION, new Integer(location));
        this.location = location;
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
        requestAttributeChange(STATUS, new Byte(status));
        this.status = status;
    }

    /**
     * Requests that the <code>awayMessage</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setAwayMessage (String awayMessage)
    {
        requestAttributeChange(AWAY_MESSAGE, awayMessage);
        this.awayMessage = awayMessage;
    }
}
