//
// $Id: OidList.java,v 1.4 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An oid list is used to store lists of object ids. The list will not
 * allow duplicate ids. This class is not synchronized, with the
 * expectation that all modifications of instances will take place on the
 * dobjmgr thread.
 */
public class OidList
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
        StringBuffer buf = new StringBuffer();
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

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeInt(_size);
        for (int i = 0; i < _size; i++) {
            out.writeInt(_oids[i]);
        }
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        _size = in.readInt();
        _oids = new int[Math.max(DEFAULT_SIZE, _size*2)];
        for (int i = 0; i < _size; i++) {
            _oids[i] = in.readInt();
        }
    }

    private int[] _oids;
    private int _size;

    protected static final int DEFAULT_SIZE = 4;
}
