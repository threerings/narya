//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import javax.annotation.Generated;
import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.Permission;

import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.SpeakObject;
import com.threerings.crowd.chat.data.UserMessage;

/**
 * The basic user object class for Crowd users. Bodies have a username, a location and a status.
 */
public class BodyObject extends ClientObject
    implements SpeakObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>location</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String LOCATION = "location";

    /** The field name of the <code>status</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String STATUS = "status";

    /** The field name of the <code>awayMessage</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String AWAY_MESSAGE = "awayMessage";
    // AUTO-GENERATED: FIELDS END

    /**
     * Identifies the place currently occupied by this body. null if they currently occupy no
     * place.
     */
    public Place location;

    /**
     * The user's current status ({@link OccupantInfo#ACTIVE}, etc.).
     */
    public byte status;

    /**
     * If non-null, this contains a message to be auto-replied whenever another user delivers a
     * tell message to this user.
     */
    public String awayMessage;

    /**
     * Returns the oid of the place occupied by this body or -1 if we occupy no place.
     */
    public int getPlaceOid ()
    {
        return (location == null) ? -1 : location.placeOid;
    }

    /**
     * Returns this user's access control tokens.
     */
    public TokenRing getTokens ()
    {
        return EMPTY_TOKENS;
    }

    /**
     * Returns the name that should be displayed to other users. The default is to use {@link
     * #username}.
     */
    public Name getVisibleName ()
    {
        return username;
    }

    /**
     * Creates a blank occupant info instance that will used to publish information about the
     * various bodies occupying a place.
     */
    public OccupantInfo createOccupantInfo (PlaceObject plobj)
    {
        return new OccupantInfo(this);
    }

    /**
     * Called when this body is about to enter the specified place. Configures our {@link
     * #location} field.
     *
     * @param place the identifying information for the place we are entering.
     * @param plobj the distributed object for the place we are entering.
     */
    public void willEnterPlace (Place place, PlaceObject plobj)
    {
        setLocation(place);
    }

    /**
     * Called when this body has left its occupied place. Clears our {@link #location} field.
     *
     * @param plobj the distributed object for the place we just departed. This might be null if
     * the place object has been destroyed.
     */
    public void didLeavePlace (PlaceObject plobj)
    {
        setLocation(null);
    }

    // from interface SpeakObject
    public void applyToListeners (ListenerOp op)
    {
        op.apply(this, getVisibleName());
    }

    // from interface SpeakObject
    public String getChatIdentifier (UserMessage message)
    {
        return SpeakObject.DEFAULT_IDENTIFIER;
    }

    /**
     * Return the {@link ClientObject} that represent our driving user's connection. This is
     * the reverse operation of {@code BodyLocator.forClient} and should match it. The default
     * implementation assumes the client object and the body object are one and the same.
     *
     * Do not return null here once this object is in the wild.
     */
    public ClientObject getClientObject ()
    {
        return this;
    }

    @Override
    public String who ()
    {
        StringBuilder buf = new StringBuilder(username.toString());
        buf.append(" (");
        addWhoData(buf);
        return buf.append(")").toString();
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>location</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setLocation (Place value)
    {
        Place ovalue = this.location;
        requestAttributeChange(
            LOCATION, value, ovalue);
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setAwayMessage (String value)
    {
        String ovalue = this.awayMessage;
        requestAttributeChange(
            AWAY_MESSAGE, value, ovalue);
        this.awayMessage = value;
    }
    // AUTO-GENERATED: METHODS END

    /**
     * Allows derived classes to add data to the who details.
     */
    protected void addWhoData (StringBuilder buf)
    {
        buf.append(getOid());
        if (status != OccupantInfo.ACTIVE) {
            buf.append(" ").append(getStatusTranslation());
        }
    }

    /**
     * Get a translation suffix for this occupant's status.
     * Can be overridden to translate nonstandard statuses.
     */
    protected String getStatusTranslation ()
    {
        return OccupantInfo.X_STATUS[status];
    }

    @Override
    protected PermissionPolicy createPermissionPolicy ()
    {
        return new CrowdPermissionPolicy();
    }

    protected class CrowdPermissionPolicy extends PermissionPolicy
    {
        @Override // from PermissionPolicy
        public String checkAccess (Permission perm, Object context) {
            if (perm == ChatCodes.BROADCAST_ACCESS) {
                return getTokens().isAdmin() ? null : ACCESS_DENIED;
            } else if (perm == ChatCodes.CHAT_ACCESS) {
                return null;
            } else {
                return super.checkAccess(perm, context);
            }
        }
    }

    /** The default (no tokens) access control. */
    protected static final TokenRing EMPTY_TOKENS = new TokenRing(0);
}
