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

import java.io.IOException;

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
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
    public boolean containsKey (Comparable key)
    {
        return get(key) != null;
    }

    /**
     * Returns the entry that matches (<code>getKey().equals(key)</code>)
     * the specified key or null if no entry could be found that matches
     * the key.
     */
    public Entry get (Comparable key)
    {
        // determine where we'll be adding the new element
        int eidx = ArrayUtil.binarySearch(
            _entries, 0, _size, key, ENTRY_COMP);

        return (eidx < 0) ? null : _entries[eidx];
    }

    /**
     * Returns an iterator over the entries of this set. It does not
     * support modification (nor iteration while modifications are being
     * made to the set). It should not be kept around as it can quickly
     * become out of date.
     *
     * @deprecated
     */
    public Iterator entries ()
    {
        return iterator();
    }

    /**
     * Returns an iterator over the entries of this set. It does not
     * support modification (nor iteration while modifications are being
     * made to the set). It should not be kept around as it can quickly
     * become out of date.
     */
    public Iterator iterator ()
    {
        // the crazy sanity checks
        if (_size < 0 ||_size > _entries.length ||
            (_size > 0 && _entries[_size-1] == null)) {
            Log.warning("DSet in a bad way [size=" + _size +
                        ", entries=" + StringUtil.toString(_entries) + "].");
            Thread.dumpStack();
        }

        return new Iterator() {
            public boolean hasNext () {
                checkComodification();
                return (_index < _size);
            }
            public Object next () {
                checkComodification();
                return _entries[_index++];
            }
            public void remove () {
                throw new UnsupportedOperationException();
            }
            protected void checkComodification () {
                if (_modCount != _expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                if (_ssize != _size) {
                    Log.warning("Size changed during iteration " +
                                "[ssize=" + _ssize + ", nsize=" + _size +
                                ", entsries=" + StringUtil.toString(_entries) +
                                "].");
                    Thread.dumpStack();
                }
            }
            protected int _index = 0;
            protected int _ssize = _size;
            protected int _expectedModCount = _modCount;
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
    public Object[] toArray (Object[] array)
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
            Log.warning("Refusing to add duplicate entry [set=" + this +
                        ", entry=" + elem + "].");
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
        _modCount++;

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
        return (null != removeKey(elem.getKey()));
    }

    /**
     * Removes from the set the entry whose key matches the supplied
     * key. This should not be called directly, instead the associated
     * <code>removeFrom{Set}()</code> method should be called on the
     * distributed object that contains the set in question.
     *
     * @return the old matching entry if found and removed, null if not
     * found.
     */
    protected Entry removeKey (Object key)
    {
        // look up this entry's position in our set
        int eidx = ArrayUtil.binarySearch(
            _entries, 0, _size, key, ENTRY_COMP);

        // if we found it, remove it
        if (eidx >= 0) {
            // extract the old entry
            Entry oldEntry = _entries[eidx];
            _size--;
            if ((_entries.length > INITIAL_CAPACITY) &&
                    (_size < _entries.length/8)) {
                // if we're using less than 1/8 of our capacity, shrink by half
                Entry[] newEnts = new Entry[_entries.length/2];
                System.arraycopy(_entries, 0, newEnts, 0, eidx);
                System.arraycopy(_entries, eidx+1, newEnts, eidx, _size-eidx);
                _entries = newEnts;

            } else {
                // shift entries past the removed one downwards
                System.arraycopy(_entries, eidx+1, _entries, eidx, _size-eidx);
                _entries[_size] = null;
            }
            _modCount++;
            return oldEntry;

        } else {
            return null;
        }
    }

    /**
     * Updates the specified entry by locating an entry whose key matches
     * the key of the supplied entry and overwriting it. This should not
     * be called directly, instead the associated
     * <code>update{Set}()</code> method should be called on the
     * distributed object that contains the set in question.
     *
     * @return the old entry that was replaced, or null if it was not
     * found (in which case nothing is updated).
     */
    protected Entry update (Entry elem)
    {
        // look up this entry's position in our set
        int eidx = ArrayUtil.binarySearch(
            _entries, 0, _size, elem, ENTRY_COMP);

        // if we found it, update it
        if (eidx >= 0) {
            Entry oldEntry = _entries[eidx];
            _entries[eidx] = elem;
            _modCount++;
            return oldEntry;
        } else {
            return null;
        }
    }

    /**
     * Generates a shallow copy of this object.
     */
    public Object clone ()
    {
        try {
            DSet nset = (DSet)super.clone();
            nset._entries = (Entry[])_entries.clone();
            nset._modCount = 0;
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

    /** Custom writer method. @see Streamable. */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.writeInt(_size);
        for (int ii = 0; ii < _size; ii++) {
            out.writeObject(_entries[ii]);
        }
    }

    /** Custom reader method. @see Streamable. */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        _size = in.readInt();
        // ensure our capacity is a power of 2 (for consistency)
        int capacity = INITIAL_CAPACITY;
        while (capacity < _size) {
            capacity <<= 1;
        }
        _entries = new Entry[capacity];
        for (int ii = 0; ii < _size; ii++) {
            _entries[ii] = (Entry) in.readObject();
        }
    }

    /** The entries of the set (in a sparse array). */
    protected Entry[] _entries = new Entry[INITIAL_CAPACITY];

    /** The number of entries in this set. */
    protected int _size;

    /** Used to check for concurrent modification. */
    protected transient int _modCount;

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
