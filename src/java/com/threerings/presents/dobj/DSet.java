//
// $Id: DSet.java,v 1.26 2003/02/18 19:50:34 mdb Exp $

package com.threerings.presents.dobj;

import java.util.Comparator;
import java.util.Iterator;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;

import com.threerings.presents.Log;

/**
 * The distributed set class provides a means by which an unordered set of
 * objects can be maintained as a distributed object field. Entries can be
 * added to and removed from the set, requests for which will generate
 * events much like other distributed object fields.
 *
 * <p> Classes that wish to act as set entries must implement the {@link
 * Entry} interface which extends {@link Streamable} and adds the
 * requirement that the object provide a key which will be used to
 * identify entry equality. Thus an entry is declared to be in a set of
 * the object returned by that entry's {@link Entry#getKey} method is
 * equal (using {@link Object#equals}) to the entry returned by the {@link
 * Entry#getKey} method of some other entry in the set. Additionally, in
 * the case of entry removal, only the key for the entry to be removed
 * will be transmitted with the removal event to save network
 * bandwidth. Lastly, the object returned by {@link Entry#getKey} must be
 * a {@link Streamable} type.
 */
public class DSet
    implements Streamable, Cloneable
{
    /**
     * Entries of the set must implement this interface.
     */
    public static interface Entry extends Streamable
    {
        /**
         * Each entry provide an associated key which is used to determine
         * its uniqueness in the set. See the {@link DSet} class
         * documentation for further information.
         */
        public Comparable getKey ();
    }

    /**
     * Creates a distributed set and populates it with values from the
     * supplied iterator. This should be done before the set is unleashed
     * into the wild distributed object world because no associated entry
     * added events will be generated. Additionally, this operation does
     * not check for duplicates when adding entries, so one should be sure
     * that the iterator contains only unique entries.
     *
     * @param source an iterator from which we will initially populate the
     * set.
     */
    public DSet (Iterator source)
    {
        while (source.hasNext()) {
            add((Entry)source.next());
        }
    }

    /**
     * Creates a distributed set and populates it with values from the
     * supplied array. This should be done before the set is unleashed
     * into the wild distributed object world because no associated entry
     * added events will be generated. Additionally, this operation does
     * not check for duplicates when adding entries, so one should be sure
     * that the iterator contains only unique entries.
     *
     * @param source an array from which we will initially populate the
     * set.
     */
    public DSet (Entry[] source)
    {
        for (int ii = 0; ii < source.length; ii++) {
            if (source[ii] != null) {
                add(source[ii]);
            }
        }
    }

    /**
     * Constructs an empty distributed set.
     */
    public DSet ()
    {
    }

    /**
     * Returns the number of entries in this set.
     */
    public int size ()
    {
        return _size;
    }

    /**
     * Returns true if the set contains an entry whose
     * <code>getKey()</code> method returns a key that
     * <code>equals()</code> the key returned by <code>getKey()</code> of
     * the supplied entry. Returns false otherwise.
     */
    public boolean contains (Entry elem)
    {
        return containsKey(elem.getKey());
    }

    /**
     * Returns true if an entry in the set has a key that
     * <code>equals()</code> the supplied key. Returns false otherwise.
     */
    public boolean containsKey (Object key)
    {
        return get(key) != null;
    }

    /**
     * Returns the entry that matches (<code>getKey().equals(key)</code>)
     * the specified key or null if no entry could be found that matches
     * the key.
     */
    public Entry get (Object key)
    {
        // scan the array looking for a matching entry
        int elength = _entries.length;
        for (int i = 0; i < elength; i++) {
            // the array may be sparse
            if (_entries[i] != null) {
                Entry elem = _entries[i];
                if (elem.getKey().equals(key)) {
                    return elem;
                }
            }
        }
        return null;
    }

    /**
     * Returns an iterator over the entries of this set. It does not
     * support modification (nor iteration while modifications are being
     * made to the set). It should not be kept around as it can quickly
     * become out of date.
     */
    public Iterator entries ()
    {
        return new Iterator() {
            public boolean hasNext () {
                return (_index < _size);
            }
            public Object next () {
                return _entries[_index++];
            }
            public void remove () {
                throw new UnsupportedOperationException();
            }
            protected int _index = 0;
        };
    }

    /**
     * Copies the elements of this distributed set into the supplied
     * array. If the array is not large enough to hold all of the
     * elements, as many as fit into the array will be copied. If the
     * <code>array</code> argument is null, an object array of sufficient
     * size to contain all of the elements of this set will be created and
     * returned.
     */
    public Object toArray (Object[] array)
    {
        if (array == null) {
            array = new Object[size()];
        }
        System.arraycopy(_entries, 0, array, 0, array.length);
        return array;
    }

    /**
     * Adds the specified entry to the set. This should not be called
     * directly, instead the associated <code>addTo{Set}()</code> method
     * should be called on the distributed object that contains the set in
     * question.
     *
     * @return true if the entry was added, false if it was already in
     * the set.
     */
    protected boolean add (Entry elem)
    {
        // determine where we'll be adding the new element
        int eidx = ArrayUtil.binarySearch(
            _entries, 0, _size, elem, ENTRY_COMP);

        // if the element is already in the set, bail now
        if (eidx >= 0) {
            return false;
        }

        // convert the index into happy positive land
        eidx = (eidx+1)*-1;

        // expand our entries array if necessary
        int elength = _entries.length;
        if (_size >= elength) {
            // sanity check
            if (elength > 2048) {
                Log.warning("Requested to expand to questionably large size " +
                            "[length=" + elength + "].");
                Thread.dumpStack();
            }

            // create a new array and copy our data into it
            Entry[] elems = new Entry[elength*2];
            System.arraycopy(_entries, 0, elems, 0, elength);
            _entries = elems;
        }

        // if the entry doesn't go at the end, shift the elements down to
        // accomodate it
        if (eidx < _size) {
            System.arraycopy(_entries, eidx, _entries, eidx+1, _size-eidx);
        }

        // stuff the entry into the array and note that we're bigger
        _entries[eidx] = elem;
        _size++;

        return true;
    }

    /**
     * Removes the specified entry from the set. This should not be called
     * directly, instead the associated <code>removeFrom{Set}()</code>
     * method should be called on the distributed object that contains the
     * set in question.
     *
     * @return true if the entry was removed, false if it was not in the
     * set.
     */
    protected boolean remove (Entry elem)
    {
        return removeKey(elem.getKey());
    }

    /**
     * Removes from the set the entry whose key matches the supplied
     * key. This should not be called directly, instead the associated
     * <code>removeFrom{Set}()</code> method should be called on the
     * distributed object that contains the set in question.
     *
     * @return true if a matching entry was removed, false if no entry
     * in the set matched the key.
     */
    protected boolean removeKey (Object key)
    {
        // look up this entry's position in our set
        int eidx = ArrayUtil.binarySearch(
            _entries, 0, _size, key, ENTRY_COMP);

        // if we found it, remove it
        if (eidx >= 0) {
            // shift the remaining elements down
            System.arraycopy(_entries, eidx+1, _entries, eidx, _size-eidx-1);
            _entries[--_size] = null;
            return true;

        } else {
            return false;
        }
    }

    /**
     * Updates the specified entry by locating an entry whose key matches
     * the key of the supplied entry and overwriting it. This should not
     * be called directly, instead the associated
     * <code>update{Set}()</code> method should be called on the
     * distributed object that contains the set in question.
     *
     * @return true if the entry was updated, false if it was not
     * already in the set (in which case nothing is updated).
     */
    protected boolean update (Entry elem)
    {
        // look up this entry's position in our set
        int eidx = ArrayUtil.binarySearch(
            _entries, 0, _size, elem, ENTRY_COMP);

        // if we found it, update it
        if (eidx >= 0) {
            _entries[eidx] = elem;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Generates a shallow copy of this object.
     */
    public Object clone ()
    {
        try {
            DSet nset = (DSet)super.clone();
            nset._entries = new Entry[_entries.length];
            System.arraycopy(_entries, 0, nset._entries, 0, _entries.length);
            nset._size = _size;
            return nset;
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("WTF? " + cnse);
        }
    }

    /**
     * Generates a string representation of this set instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("(");
        String prefix = "";
        for (int i = 0; i < _entries.length; i++) {
            Entry elem = _entries[i];
            if (elem != null) {
                buf.append(prefix);
                prefix = ", ";
                buf.append(elem);
            }
        }
        buf.append(")");
        return buf.toString();
    }

    /** The entries of the set (in a sparse array). */
    protected Entry[] _entries = new Entry[INITIAL_CAPACITY];

    /** The number of entries in this set. */
    protected int _size;

    /** The default capacity of a set instance. */
    protected static final int INITIAL_CAPACITY = 2;

    /** Used for lookups and to keep the set contents sorted on
     * insertions. */
    protected static Comparator ENTRY_COMP = new Comparator() {
        public int compare (Object o1, Object o2) {
            Comparable c1 = (o1 instanceof Entry) ?
                ((Entry)o1).getKey() : (Comparable)o1;
            Comparable c2 = (o2 instanceof Entry) ?
                ((Entry)o2).getKey() : (Comparable)o2;
            return c1.compareTo(c2);
        }
    };
}
