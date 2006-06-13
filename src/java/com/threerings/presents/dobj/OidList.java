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

package com.threerings.presents.dobj;

import com.threerings.io.Streamable;

/**
 * An oid list is used to store lists of object ids. The list will not
 * allow duplicate ids. This class is not synchronized, with the
 * expectation that all modifications of instances will take place on the
 * dobjmgr thread.
 */
public class OidList implements Streamable
{
    /**
     * Creates an empty oid list.
     */
    public OidList ()
    {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates an empty oid list with space for at least
     * <code>initialSize</code> object ids before it will need to expand.
     */
    public OidList (int initialSize)
    {
        // ensure that we have at least two slots or the expansion code
        // won't work
        _oids = new int[Math.max(initialSize, 2)];
    }

    /**
     * Returns the number of object ids in the list.
     */
    public int size ()
    {
        return _size;
    }

    /**
     * Adds the specified object id to the list if it is not already
     * there.
     *
     * @return true if the object was added, false if it was already in
     * the list.
     */
    public boolean add (int oid)
    {
        // check for existence
        for (int i = 0; i < _size; i++) {
            if (_oids[i] == oid) {
                return false;
            }
        }

        // make room if necessary
        if (_size+1 >= _oids.length) {
            expand();
        }

        // add the oid
        _oids[_size++] = oid;
        return true;
    }

    /**
     * Removes the specified oid from the list.
     *
     * @return true if the oid was in the list and was removed, false
     * otherwise.
     */
    public boolean remove (int oid)
    {
        // scan for the oid in question
        for (int i = 0; i < _size; i++) {
            if (_oids[i] == oid) {
                // shift the rest of the list back one
                System.arraycopy(_oids, i+1, _oids, i, --_size-i);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the specified oid is in the list, false if not.
     */
    public boolean contains (int oid)
    {
        for (int i = 0; i < _size; i++) {
            if (_oids[i] == oid) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the object id at the specified index. This does no boundary
     * checking.
     */
    public int get (int index)
    {
        return _oids[index];
    }

    public String toString ()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        for (int i = 0; i < _size; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(_oids[i]);
        }
        buf.append("}");
        return buf.toString();
    }

    private void expand ()
    {
        int[] oids = new int[_oids.length*2];
        System.arraycopy(_oids, 0, oids, 0, _oids.length);
        _oids = oids;
    }

    private int[] _oids;
    private int _size;

    protected static final int DEFAULT_SIZE = 4;
}
