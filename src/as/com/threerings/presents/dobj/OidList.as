//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.dobj {

import com.threerings.util.ArrayUtil;

import com.threerings.io.Streamable;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.util.StringBuilder;

/**
 * An oid list is used to store lists of object ids. The list will not
 * allow duplicate ids. This class is not synchronized, with the
 * expectation that all modifications of instances will take place on the
 * dobjmgr thread.
 */
public class OidList
    implements Streamable
{
    /**
     * Creates an empty oidlist.
     */
    public function OidList ()
    {
        _oids = TypedArray.create(int);
    }

    /**
     * Returns the number of object ids in the list.
     */
    public function size () :int
    {
        return _oids.length;
    }

    /**
     * Adds the specified object id to the list if it is not already
     * there.
     *
     * @return true if the object was added, false if it was already in
     * the list.
     */
    public function add (oid :int) :Boolean
    {
        // check for existence
        if (ArrayUtil.contains(_oids, oid)) {
            return false;
        }

        // add the oid
        _oids[_oids.length] = oid;
        return true;
    }

    /**
     * Removes the specified oid from the list.
     *
     * @return true if the oid was in the list and was removed, false
     * otherwise.
     */
    public function remove (oid :int) :Boolean
    {
        // scan for the oid in question
        return ArrayUtil.removeFirst(_oids, oid);
    }

    /**
     * Returns true if the specified oid is in the list, false if not.
     */
    public function contains (oid :int) :Boolean
    {
        return ArrayUtil.contains(_oids, oid);
    }

    /**
     * Returns the object id at the specified index. This does no boundary
     * checking.
     */
    public function get (index :int) :int
    {
        return _oids[index];
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        throw new Error();
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        _oids = (ins.readField("[I") as TypedArray);
        _oids.length = ins.readInt();
    }

    public function toString () :String
    {
        var buf :StringBuilder = new StringBuilder();
        buf.append("{");
        buf.append(_oids.toString());
        buf.append("}");
        return buf.toString();
    }

    private var _oids :TypedArray;
}
}
