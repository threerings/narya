//
// $Id: OccupantInfo.java 3774 2005-12-03 03:05:06Z mdb $
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

import com.threerings.util.Integer;
import com.threerings.util.Name;

import com.threerings.presents.dobj.DSetEntry;

import com.threerings.crowd.data.BodyObject;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * The occupant info object contains all of the information about an
 * occupant of a place that should be shared with other occupants of the
 * place. These objects are stored in the place object itself and are
 * updated when bodies enter and exit a place.
 *
 * <p> A system that builds upon the Crowd framework can extend this class to
 * include extra information about their occupants. They will need to provide a
 * derived {@link BodyObject} that creates and configures their occupant info
 * in {@link BodyObject#createOccupantInfo}.
 *
 * <p> Note also that this class implements {@link Cloneable} which means
 * that if derived classes add non-primitive attributes, they are
 * responsible for adding the code to clone those attributes when a clone
 * is requested.
 */
public class OccupantInfo
    implements DSetEntry
{
    /** Constant value for {@link #status}. */
    public static const ACTIVE :int = 0;

    /** Constant value for {@link #status}. */
    public static const IDLE :int = 1;

    /** Constant value for {@link #status}. */
    public static const DISCONNECTED :int = 2;

    /** Maps status codes to human readable strings. */
    public static const X_STATUS :Array = [ "active", "idle", "discon" ];

    /** The body object id of this occupant (and our entry key). */
    public var bodyOid :Integer;

    /** The username of this occupant. */
    public var username :Name;

    /** The status of this occupant. */
    public var status :int = ACTIVE;

    /** Access to the body object id as an int. */
    public function getBodyOid () :int
    {
        return bodyOid.value;
    }

    // documentation inherited from interface DSetEntry
    public function getKey () :Object
    {
        return bodyOid;
    }

    // documentation inherited from superinterface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeObject(bodyOid);
        out.writeObject(username);
        out.writeByte(status);
    }

    // documentation inherited from superinterface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        bodyOid = (ins.readField(Integer) as Integer);
        username = (ins.readObject() as Name);
        status = ins.readByte();
    }
}
}
