//
// $Id$
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
import com.threerings.presents.data.InvocationCodes;

import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.SpeakObject;

/**
 * The basic user object class for Crowd users. Bodies have a username, a
 * location and a status.
 */
public class BodyObject extends ClientObject
    implements SpeakObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>username</code> field. */
    public static final String USERNAME = "username";

    /** The field name of the <code>location</code> field. */
    public static final String LOCATION = "location";

    /** The field name of the <code>status</code> field. */
    public static final String STATUS = "status";

    /** The field name of the <code>awayMessage</code> field. */
    public static final String AWAY_MESSAGE = "awayMessage";
    // AUTO-GENERATED: FIELDS END

    /**
     * The username associated with this body object. This should not be used
     * directly; in general {@link #getVisibleName} should be used unless you
     * specifically know that you want the username.
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

    /**
     * Checks whether or not this user has access to the specified
     * feature. Currently used by the chat system to regulate access to
     * chat broadcasts but also forms the basis of an extensible
     * fine-grained permissions system.
     *
     * @return null if the user has access, a fully-qualified translatable
     * message string indicating the reason for denial of access (or just
     * {@link InvocationCodes#ACCESS_DENIED} if you don't want to be
     * specific).
     */
    public String checkAccess (String feature, Object context)
    {
        // our default access control policy; how quaint
        if (ChatCodes.BROADCAST_ACCESS.equals(feature)) {
            return getTokens().isAdmin() ? null : ChatCodes.ACCESS_DENIED;
        } else if (ChatCodes.CHAT_ACCESS.equals(feature)) {
            return null;
        } else {
            return InvocationCodes.ACCESS_DENIED;
        }
    }

    /**
     * Returns this user's access control tokens.
     */
    public TokenRing getTokens ()
    {
        return EMPTY_TOKENS;
    }

    /**
     * Returns the name that should be displayed to other users and used for
     * the chat system. The default is to use {@link #username}.
     */
    public Name getVisibleName ()
    {
        return username;
    }

    /**
     * Creates a blank occupant info instance that will used to publish
     * information about the various bodies occupying a place.
     */
    public OccupantInfo createOccupantInfo ()
    {
        return new OccupantInfo(this);
    }

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

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>username</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setUsername (Name value)
    {
        Name ovalue = this.username;
        requestAttributeChange(
            USERNAME, value, ovalue);
        this.username = value;
    }

    /**
     * Requests that the <code>location</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setLocation (int value)
    {
        int ovalue = this.location;
        requestAttributeChange(
            LOCATION, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.location = value;
    }

    /**
     * Requests that the <code>status</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setStatus (byte value)
    {
        byte ovalue = this.status;
        requestAttributeChange(
            STATUS, Byte.valueOf(value), Byte.valueOf(ovalue));
        this.status = value;
    }

    /**
     * Requests that the <code>awayMessage</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setAwayMessage (String value)
    {
        String ovalue = this.awayMessage;
        requestAttributeChange(
            AWAY_MESSAGE, value, ovalue);
        this.awayMessage = value;
    }
    // AUTO-GENERATED: METHODS END

    /** The default (no tokens) access control. */
    protected static final TokenRing EMPTY_TOKENS = new TokenRing();
}
