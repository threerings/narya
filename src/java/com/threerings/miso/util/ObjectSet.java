//
// $Id: ObjectSet.java,v 1.3 2003/06/12 05:24:08 mdb Exp $

package com.threerings.miso.util;

import java.util.Arrays;
import java.util.Comparator;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.ListUtil;

import com.threerings.miso.Log;
import com.threerings.miso.data.ObjectInfo;

/**
 * Used to store an (arbitrarily) ordered, low-impact iteratable (doesn't
 * require object creation), set of {@link ObjectInfo} instances.
 */
public class ObjectSet
{
    /**
     * Inserts the supplied object into the set.
     *
     * @return true if it was inserted, false if the object was already in
     * the set.
     */
    public boolean insert (ObjectInfo info)
    {
        // bail if it's already in the set
        int ipos = indexOf(info);
        if (ipos >= 0) {
            // log a warning because the caller shouldn't be doing this
            Log.warning("Requested to add an object to a set that already " +
                        "contains such an object [ninfo=" + info +
                        ", oinfo=" + _objs[ipos] + "].");
            Thread.dumpStack();
            return false;
        }

        // otherwise insert it
        ipos = -(ipos+1);
        _objs = ListUtil.insert(_objs, ipos, info);
        _size++;
        return true;
    }

    /**
     * Returns true if the specified object is in the set, false if it is
     * not.
     */
    public boolean contains (ObjectInfo info)
    {
        return (indexOf(info) >= 0);
    }

    /**
     * Returns the number of objects in this set.
     */
    public int size ()
    {
        return _size;
    }

    /**
     * Returns the object with the specified index. The index must & be
     * between <code>0</code> and {@link #size}<code>-1</code>.
     */
    public ObjectInfo get (int index)
    {
        return (ObjectInfo)_objs[index];
    }

    /**
     * Removes the object at the specified index.
     */
    public void remove (int index)
    {
        ListUtil.remove(_objs, index);
        _size--;
    }

    /**
     * Removes the specified object from the set.
     *
     * @return true if it was removed, false if it was not in the set.
     */
    public boolean remove (ObjectInfo info)
    {
        int opos = indexOf(info);
        if (opos >= 0) {
            remove(opos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Clears out the contents of this set.
     */
    public void clear ()
    {
        _size = 0;
        Arrays.fill(_objs, null);
    }

    /**
     * Converts the contents of this object set to an array.
     */
    public ObjectInfo[] toArray ()
    {
        ObjectInfo[] info = new ObjectInfo[_size];
        System.arraycopy(_objs, 0, info, 0, _size);
        return info;
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        for (int ii = 0; ii < _size; ii++) {
            if (ii > 0) {
                buf.append(", ");
            }
            buf.append(_objs[ii]);
        }
        return buf.append("]").toString();
    }

    /**
     * Returns the index of the object or it's insertion index if it is
     * not in the set.
     */
    protected final int indexOf (ObjectInfo info)
    {
        return ArrayUtil.binarySearch(_objs, 0, _size, info, INFO_COMP);
    }

    /** Our sorted array of objects. */
    protected Object[] _objs = new Object[DEFAULT_SIZE];

    /** The number of objects in the set. */
    protected int _size;

    /** We simply sort the objects in order of their hash code. We don't
     * care about their order, it exists only to support binary search. */
    protected static final Comparator INFO_COMP = new Comparator() {
        public int compare (Object o1, Object o2) {
            ObjectInfo do1 = (ObjectInfo)o1;
            ObjectInfo do2 = (ObjectInfo)o2;
            if (do1.tileId == do2.tileId) {
                return ((do1.x << 16) + do1.y) - ((do2.x << 16) + do2.y);
            } else {
                return do1.tileId - do2.tileId;
            }
        }
    };

    /** We start big because we know these will in general contain at
     * least in the tens of objects. */
    protected static final int DEFAULT_SIZE = 16;
}
