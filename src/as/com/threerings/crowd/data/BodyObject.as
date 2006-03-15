//
// $Id: BodyObject.java 3774 2005-12-03 03:05:06Z mdb $
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

package com.threerings.crowd.data {

import com.threerings.util.Byte;
import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;

import com.threerings.crowd.chat.data.ChatCodes;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * The basic user object class for Crowd users. Bodies have a username, a
 * location and a status.
 */
public class BodyObject extends ClientObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>username</code> field. */
    public static const USERNAME :String = "username";

    /** The field name of the <code>location</code> field. */
    public static const LOCATION :String = "location";

    /** The field name of the <code>status</code> field. */
    public static const STATUS :String = "status";

    /** The field name of the <code>awayMessage</code> field. */
    public static const AWAY_MESSAGE :String = "awayMessage";
    // AUTO-GENERATED: FIELDS END

    /**
     * The username associated with this body object. This should not be used
     * directly; in general {@link #getVisibleName} should be used unless you
     * specifically know that you want the username.
     */
    public var username :Name;

    /**
     * The oid of the place currently occupied by this body or -1 if they
     * currently occupy no place.
     */
    public var location :int = -1;

    /**
     * The user's current status ({@link OccupantInfo#ACTIVE}, etc.).
     */
    public var status :int;

    /**
     * If non-null, this contains a message to be auto-replied whenever
     * another user delivers a tell message to this user.
     */
    public var awayMessage :String;

//    /**
//     * Checks whether or not this user has access to the specified
//     * feature. Currently used by the chat system to regulate access to
//     * chat broadcasts but also forms the basis of an extensible
//     * fine-grained permissions system.
//     *
//     * @return null if the user has access, a fully-qualified translatable
//     * message string indicating the reason for denial of access (or just
//     * {@link InvocationCodes#ACCESS_DENIED} if you don't want to be
//     * specific).
//     */
//    public String checkAccess (String feature, Object context)
//    {
//        // our default access control policy; how quaint
//        if (ChatCodes.BROADCAST_ACCESS.equals(feature)) {
//            return getTokens().isAdmin() ? null : ChatCodes.ACCESS_DENIED;
//        } else if (ChatCodes.CHAT_ACCESS.equals(feature)) {
//            return null;
//        } else {
//            return InvocationCodes.ACCESS_DENIED;
//        }
//    }
//
//    /**
//     * Returns this user's access control tokens.
//     */
//    public TokenRing getTokens ()
//    {
//        return EMPTY_TOKENS;
//    }

    /**
     * Returns the name that should be displayed to other users and used for
     * the chat system. The default is to use {@link #username}.
     */
    public function getVisibleName () :Name
    {
        return username;
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(username);
        out.writeInt(location);
        out.writeByte(status);
        out.writeField(awayMessage);
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        username = (ins.readObject() as Name);
        location = ins.readInt();
        status = ins.readByte();
        awayMessage = (ins.readField(String) as String);
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
    public function setUsername (value :Name) :void
    {
        var ovalue :Name = this.username;
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
    public function setLocation (value :int) :void
    {
        var ovalue :int = this.location;
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
    public function setStatus (value :int) :void
    {
        var ovalue :int = this.status;
        requestAttributeChange(
            STATUS, new Byte(value), new Byte(ovalue));
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
    public function setAwayMessage (value :String) :void
    {
        var ovalue :String = this.awayMessage;
        requestAttributeChange(
            AWAY_MESSAGE, value, ovalue);
        this.awayMessage = value;
    }
    // AUTO-GENERATED: METHODS END
}
}
