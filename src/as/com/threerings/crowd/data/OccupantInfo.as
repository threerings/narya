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

import com.threerings.util.Cloneable;
import com.threerings.util.Integer;
import com.threerings.util.Name;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;

import com.threerings.presents.dobj.DSet_Entry;

import com.threerings.crowd.data.BodyObject;

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
public class OccupantInfo extends SimpleStreamableObject
    implements DSet_Entry, Cloneable
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
    public var bodyOid :int;

    /** The username of this occupant. */
    public var username :Name;

    /** The status of this occupant. */
    public var status :int = ACTIVE;

    /**
     * Constructs an occupant info record, optionally obtaining data from the
     * supplied BodyObject.
     */
    public function OccupantInfo (body :BodyObject = null)
    {
        if (body != null) {
            bodyOid = body.getOid();
            username = body.getVisibleName();
            status = body.status;
        }
    }

    /** Access to the body object id as an int. */
    public function getBodyOid () :int
    {
        return bodyOid;
    }

    /**
     * Generates a cloned copy of this instance.
     */
    public function clone () :Object
    {
        throw new Error("Clone not implemented. Implement if you need it.");
    }

    // documentation inherited from interface DSet_Entry
    public function getKey () :Object
    {
        return bodyOid;
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        bodyOid = (ins.readField(Integer) as Integer).value;
        username = (ins.readObject() as Name);
        status = ins.readByte();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        throw new Error();
//        super.writeObject(out);
//        out.writeObject(new Integer(bodyOid));
//        out.writeObject(username);
//        out.writeByte(status);
    }
}
}
