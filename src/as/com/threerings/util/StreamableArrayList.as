package com.threerings.util {

import mx.collections.ArrayCollection;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class StreamableArrayList extends ArrayCollection
    implements Streamable
{
    /**
     * Returns the index of the specified object in this collection, using
     * Equalable.equals() if possible.
     */
    public function indexOf (object :Object) :int
    {
        return ArrayUtil.indexOf(source, object);
    }

    /**
     * Removes the first instance of the supplied object from this array, using
     * Equalable.equals() to determine equality. Returns the removed object if
     * a match was found, null otherwise.
     */
    public function remove (object :Object) :Object
    {
        return ArrayUtil.removeFirst(source, object);
    }

    /**
     * Adds all of the elements of the supplied collection to the end of this
     * list.
     */
    public function addAll (list :ArrayCollection) :void
    {
        for (var ii :int = 0; ii < list.length; ii++) {
            addItem(list.getItemAt(ii));
        }
    }

    /**
     * Extracts and returns a sublist from this list.
     */
    public function subList (startIdx :int, endIdx :int) :ArrayCollection
    {
        var newlist :ArrayCollection = new ArrayCollection();
        for (var ii :int = startIdx; ii <= endIdx; ii++) {
            newlist.addItem(getItemAt(ii));
        }
        return newlist;
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(source.length);
        for (var ii :int = 0; ii < source.length; ii++) {
            out.writeObject(source[ii]);
        }
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        var ecount :int = ins.readInt();
        for (var ii :int = 0; ii < ecount; ii++) {
            source[ii] = ins.readObject();
        }
    }
}
}
