//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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
 * The basic user object class for Crowd users. Bodies have a location and a status.
 */
public class BodyObject extends ClientObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>location</code> field. */
    public static const LOCATION :String = "location";

    /** The field name of the <code>status</code> field. */
    public static const STATUS :String = "status";

    /** The field name of the <code>awayMessage</code> field. */
    public static const AWAY_MESSAGE :String = "awayMessage";
    // AUTO-GENERATED: FIELDS END

    /**
     * The oid of the place currently occupied by this body or -1 if they currently occupy no
     * place.
     */
    public var location :Place;

    /**
     * The user's current status ({@link OccupantInfo#ACTIVE}, etc.).
     */
    public var status :int;

    /**
     * If non-null, this contains a message to be auto-replied whenever another user delivers a
     * tell message to this user.
     */
    public var awayMessage :String;

    /**
     * Returns this user's access control tokens.
     */
    public function getTokens () :TokenRing
    {
        return new TokenRing();
    }

    /**
     * Returns the name that should be displayed to other users. The default is to use
     * <code>username</code>.
     * @see com.threerings.presents.data.ClientObject#username
     */
    public function getVisibleName () :Name
    {
        return username;
    }

    /**
     * Returns the oid of the place occupied by this body or -1 if we occupy no place.
     */
    public function getPlaceOid () :int
    {
        return (location == null) ? -1 : location.placeOid;
    }

//    // AUTO-GENERATED: METHODS START
//    /**
//     * Requests that the <code>username</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setUsername (value :Name) :void
//    {
//        var ovalue :Name = this.username;
//        requestAttributeChange(
//            USERNAME, value, ovalue);
//        this.username = value;
//    }
//
//    /**
//     * Requests that the <code>location</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setLocation (value :int) :void
//    {
//        var ovalue :int = this.location;
//        requestAttributeChange(
//            LOCATION, value, ovalue);
//        this.location = value;
//    }
//
//    /**
//     * Requests that the <code>status</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setStatus (value :int) :void
//    {
//        var ovalue :int = this.status;
//        requestAttributeChange(
//            STATUS, Byte.valueOf(value), Byte.valueOf(ovalue));
//        this.status = value;
//    }
//
//    /**
//     * Requests that the <code>awayMessage</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setAwayMessage (value :String) :void
//    {
//        var ovalue :String = this.awayMessage;
//        requestAttributeChange(
//            AWAY_MESSAGE, value, ovalue);
//        this.awayMessage = value;
//    }
//    // AUTO-GENERATED: METHODS END
//
//    override public function writeObject (out :ObjectOutputStream) :void
//    {
//        super.writeObject(out);
//
//        out.writeObject(username);
//        out.writeInt(location);
//        out.writeByte(status);
//        out.writeField(awayMessage);
//    }

    override public function who () :String
    {
        var who :String = username.toString() + " (" + getOid();
        if (status != OccupantInfo.ACTIVE) {
            who += (" " + getStatusTranslation());
        }
        who += ")";
        return who;
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        location = Place(ins.readObject());
        status = ins.readByte();
        awayMessage = (ins.readField(String) as String);
    }

    /**
     * Get a translation suffix for this occupant's status.
     * Can be overridden to translate nonstandard statuses.
     */
    protected function getStatusTranslation () :String
    {
        return OccupantInfo.X_STATUS[status];
    }
}
}
