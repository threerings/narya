package com.threerings.presents.dobj {

import com.threerings.io.Streamable;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

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
        _oids = new Array();
    }

    /**
     * Returns the number of object ids in the list.
     */
    public function size () :int
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
    public function add (oid :int) :Boolean
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
    public function getAt (index :int) :int
    {
        return _oids[index];
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        trace("Not implemented");
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        // TODO: this is some custom shit. We need to work out array streaming
        _oids = new Array();
        var ecount :int = ins.readInt();
        for (var ii :int = 0; ii < ecount; ii++) {
            _oids.push(ins.readInt());
        }
        _oids.length = ins.readInt(); // then, limit the length to the size
    }


    public override function toString () :String
    {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(_oids.toString());
        buf.append("}");
        return buf.toString();
    }

    private var _oids :Array;
}
}
