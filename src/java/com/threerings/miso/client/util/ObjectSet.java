//
// $Id: ObjectSet.java,v 1.2 2002/10/15 21:01:39 ray Exp $

package com.threerings.miso.scene.util;

import java.util.Arrays;
import java.util.Comparator;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.ListUtil;

import com.threerings.miso.scene.SceneObject;

/**
 * Used to store an (arbitrarily) ordered, low-impact iteratable (doesn't
 * require object creation), set of {@link SceneObject} instances.
 */
public class ObjectSet
{
    /**
     * Inserts the supplied scene object into the set.
     *
     * @return true if it was inserted, false if the object was already in
     * the set.
     */
    public boolean insert (SceneObject scobj)
    {
        // bail if it's already in the set
        int ipos = indexOf(scobj);
        if (ipos >= 0) {
            return false;
        }

        // otherwise insert it
        ipos = -(ipos+1);
        _scobjs = ListUtil.insert(_scobjs, ipos, scobj);
        _size++;
        return true;
    }

    /**
     * Returns true if the specified object is in the set, false if it is
     * not.
     */
    public boolean contains (SceneObject scobj)
    {
        return (indexOf(scobj) >= 0);
    }

    /**
     * Returns the number of scene objects in this set.
     */
    public int size ()
    {
        return _size;
    }

    /**
     * Returns the scene object with the specified index. The index must &
     * be between <code>0</code> and {@link #size}<code>-1</code>.
     */
    public SceneObject get (int index)
    {
        return (SceneObject)_scobjs[index];
    }

    /**
     * Removes the object at the specified index.
     */
    public void remove (int index)
    {
        ListUtil.remove(_scobjs, index);
        _size--;
    }

    /**
     * Removes the specified scene object from the set.
     *
     * @return true if it was removed, false if it was not in the set.
     */
    public boolean remove (SceneObject scobj)
    {
        int opos = indexOf(scobj);
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
        Arrays.fill(_scobjs, null);
    }

    /**
     * Returns the index of the object or it's insertion index if it is
     * not in the set.
     */
    protected final int indexOf (SceneObject scobj)
    {
        return ArrayUtil.binarySearch(_scobjs, 0, _size, scobj, SCOBJ_COMP);
    }

    /** Our sorted array of scene objects. */
    protected Object[] _scobjs = new Object[DEFAULT_SIZE];

    /** The number of objects in the set. */
    protected int _size;

    /** We simply sort the scene objects in order of their hash code. We
     * don't care about their orderer, it simply needs to exist to support
     * binary search. */
    protected static final Comparator SCOBJ_COMP = new Comparator() {
        public int compare (Object o1, Object o2) {
            return o1.hashCode() - o2.hashCode();
        }
    };

    /** We start big because we know these will in general contain at
     * least in the tens of objects. */
    protected static final int DEFAULT_SIZE = 16;
}
