//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Set;

import java.io.IOException;

import com.samskivert.util.ArrayUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import static com.threerings.presents.Log.log;

/**
 * The distributed set class provides a means by which an unordered set of objects can be
 * maintained as a distributed object field. Entries can be added to and removed from the set,
 * requests for which will generate events much like other distributed object fields.
 *
 * <p> Classes that wish to act as set entries must implement the {@link Entry} interface which
 * extends {@link Streamable} and adds the requirement that the object provide a key which will be
 * used to identify entry equality. Thus an entry is declared to be in a set of the object returned
 * by that entry's {@link Entry#getKey} method is equal (using {@link Object#equals}) to the entry
 * returned by the {@link Entry#getKey} method of some other entry in the set. Additionally, in the
 * case of entry removal, only the key for the entry to be removed will be transmitted with the
 * removal event to save network bandwidth. Lastly, the object returned by {@link Entry#getKey}
 * must be a {@link Streamable} type.
 *
 * @param <E> the type of entry stored in this set.
 */
public class DSet<E extends DSet.Entry>
    implements Iterable<E>, Streamable, Cloneable
{
    /**
     * Entries of the set must implement this interface.
     */
    public static interface Entry extends Streamable
    {
        /**
         * Each entry provide an associated key which is used to determine its uniqueness in the
         * set. See the {@link DSet} class documentation for further information.
         */
        Comparable<?> getKey ();
    }

    /**
     * Creates a new DSet of the appropriate generic type.
     */
    public static <E extends DSet.Entry> DSet<E> newDSet ()
    {
        return new DSet<E>();
    }

    /**
     * Creates a new DSet of the appropriate generic type.
     */
    public static <E extends DSet.Entry> DSet<E> newDSet (Iterable<? extends E> source)
    {
        return new DSet<E>(source);
    }

    /**
     * Compares the first comparable to the second. This is useful to avoid type safety warnings
     * when dealing with the keys of {@link DSet.Entry} values.
     */
    public static int compare (Comparable<?> c1, Comparable<?> c2)
    {
        @SuppressWarnings("unchecked") Comparable<Object> cc1 = (Comparable<Object>)c1;
        @SuppressWarnings("unchecked") Comparable<Object> cc2 = (Comparable<Object>)c2;
        return cc1.compareTo(cc2);
    }

    /**
     * Creates a distributed set and populates it with values from the supplied iterator. This
     * should be done before the set is unleashed into the wild distributed object world because no
     * associated entry added events will be generated. Additionally, this operation does not check
     * for duplicates when adding entries, so one should be sure that the iterator contains only
     * unique entries.
     *
     * @param source an iterator from which we will initially populate the set.
     */
    public DSet (Iterable<? extends E> source)
    {
        for (E e : source) {
            add(e);
        }
    }

    /**
     * Creates a distributed set and populates it with values from the supplied iterator. This
     * should be done before the set is unleashed into the wild distributed object world because no
     * associated entry added events will be generated. Additionally, this operation does not check
     * for duplicates when adding entries, so one should be sure that the iterator contains only
     * unique entries.
     *
     * @param source an iterator from which we will initially populate the set.
     */
    public DSet (Iterator<? extends E> source)
    {
        while (source.hasNext()) {
            add(source.next());
        }
    }

    /**
     * Creates a distributed set and populates it with values from the supplied array. This should
     * be done before the set is unleashed into the wild distributed object world because no
     * associated entry added events will be generated. Additionally, this operation does not check
     * for duplicates when adding entries, so one should be sure that the iterator contains only
     * unique entries.
     *
     * @param source an array from which we will initially populate the set.
     */
    public DSet (E[] source)
    {
        for (E element : source) {
            if (element != null) {
                add(element);
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
     * Returns <tt>true</tt> if this set contains no entries.
     */
    public boolean isEmpty ()
    {
        return _size == 0;
    }

    /**
     * Returns the number of entries in this set.
     */
    public int size ()
    {
        return _size;
    }

    /**
     * Returns true if the set contains an entry whose <code>getKey()</code> method returns a key
     * that <code>equals()</code> the key returned by <code>getKey()</code> of the supplied
     * entry. Returns false otherwise.
     */
    public boolean contains (E elem)
    {
        return containsKey(elem.getKey());
    }

    /**
     * Returns true if an entry in the set has a key that <code>equals()</code> the supplied
     * key. Returns false otherwise.
     */
    public boolean containsKey (Comparable<?> key)
    {
        return get(key) != null;
    }

    /**
     * Returns the entry that matches (<code>getKey().equals(key)</code>) the specified key or null
     * if no entry could be found that matches the key.
     */
    public E get (Comparable<?> key)
    {
        // determine where we'll be adding the new element
        int eidx = ArrayUtil.binarySearch(
            _entries, 0, _size, new SimpleEntry<Comparable<?>>(key), ENTRY_COMP);
        return (eidx < 0) ? null : _entries[eidx];
    }

    /**
     * Returns an iterator over the entries of this set. It does not support modification (nor
     * iteration while modifications are being made to the set). It should not be kept around as it
     * can quickly become out of date.
     *
     * @deprecated
     */
    @Deprecated
    public Iterator<E> entries ()
    {
        return iterator();
    }

    /**
     * Returns an iterator over the entries of this set. It does not support modification (nor
     * iteration while modifications are being made to the set). It should not be kept around as it
     * can quickly become out of date.
     */
    public Iterator<E> iterator ()
    {
        // the crazy sanity checks
        if (_size < 0 || _size > _entries.length || (_size > 0 && _entries[_size-1] == null)) {
            log.warning("DSet in a bad way", "size", _size, "entries", _entries, new Exception());
        }

        return new Iterator<E>() {
            public boolean hasNext () {
                checkComodification();
                return (_index < _size);
            }
            public E next () {
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
                    log.warning("Size changed during iteration", "ssize", _ssize, "nsize", _size,
                                "entries", _entries, new Exception());
                }
            }
            protected int _index = 0;
            protected int _ssize = _size;
            protected int _expectedModCount = _modCount;
        };
    }

    /**
     * Creates an <b>immutable</b> view of this distributed set as a Java set.
     */
    public Set<E> asSet ()
    {
        return new AbstractSet<E>() {
            @Override public boolean add (E o) {
                throw new UnsupportedOperationException();
            }
            @Override public boolean remove (Object o) {
                throw new UnsupportedOperationException();
            }
            @Override public boolean contains (Object o) {
                if (!(o instanceof DSet.Entry)) {
                    return false;
                }
                @SuppressWarnings("unchecked") E elem = (E)o;
                return DSet.this.contains(elem);
            }
            @Override public Iterator<E> iterator () {
                return DSet.this.iterator();
            }
            @Override public int size () {
                return DSet.this.size();
            }
        };
    }

    /**
     * Copies the elements of this distributed set into a newly created {@link ArrayList}.
     */
    public ArrayList<E> toArrayList () {
        ArrayList<E> list = new ArrayList<E>(size());
        for (E elem : this) list.add(elem);
        return list;
    }

    /** @deprecated use {@link #clone} or {@link #toArrayList}. */
    @Deprecated
    public E[] toArray (E[] array)
    {
        if (array == null) {
            @SuppressWarnings("unchecked") E[] copy = (E[])new Entry[size()];
            array = copy;
        }
        System.arraycopy(_entries, 0, array, 0, array.length);
        return array;
    }

    /** @deprecated use {@link #clone} or {@link #toArrayList}. */
    @Deprecated
    public Object[] toArray (Object[] array)
    {
        @SuppressWarnings("unchecked") E[] casted = (E[])array;
        return toArray(casted);
    }

    /**
     * Adds the specified entry to the set. This should not be called directly, instead the
     * associated <code>addTo{Set}()</code> method should be called on the distributed object that
     * contains the set in question.
     *
     * @return true if the entry was added, false if it was already in the set.
     */
    protected boolean add (E elem)
    {
        // determine where we'll be adding the new element
        int eidx = ArrayUtil.binarySearch(_entries, 0, _size, elem, ENTRY_COMP);

        // if the element is already in the set, bail now
        if (eidx >= 0) {
            log.warning("Refusing to add duplicate entry", "entry", elem, "set", this,
                        new Exception());
            return false;
        }

        // convert the index into happy positive land
        eidx = (eidx+1)*-1;

        // expand our entries array if necessary
        int elength = _entries.length;
        if (_size >= elength) {
            // sanity check
            if (elength > getWarningSize()) {
                log.warning("Requested to expand to questionably large size", "l", elength,
                            new Exception());
            }

            // create a new array and copy our data into it
            @SuppressWarnings("unchecked") E[] elems = (E[])new Entry[elength*2];
            System.arraycopy(_entries, 0, elems, 0, elength);
            _entries = elems;
        }

        // if the entry doesn't go at the end, shift the elements down to accomodate it
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
     * Removes the specified entry from the set. This should not be called directly, instead the
     * associated <code>removeFrom{Set}()</code> method should be called on the distributed object
     * that contains the set in question.
     *
     * @return true if the entry was removed, false if it was not in the set.
     */
    protected boolean remove (E elem)
    {
        return (null != removeKey(elem.getKey()));
    }

    /**
     * Removes from the set the entry whose key matches the supplied key. This should not be called
     * directly, instead the associated <code>removeFrom{Set}()</code> method should be called on
     * the distributed object that contains the set in question.
     *
     * @return the old matching entry if found and removed, null if not found.
     */
    protected E removeKey (Comparable<?> key)
    {
        // don't fail, but generate a warning if we're passed a null key
        if (key == null) {
            log.warning("Requested to remove null key.", new Exception());
            return null;
        }

        // look up this entry's position in our set
        int eidx = ArrayUtil.binarySearch(
            _entries, 0, _size, new SimpleEntry<Comparable<?>>(key), ENTRY_COMP);

        // if we found it, remove it
        if (eidx >= 0) {
            // extract the old entry
            E oldEntry = _entries[eidx];
            _size--;
            if ((_entries.length > INITIAL_CAPACITY) && (_size < _entries.length/8)) {
                // if we're using less than 1/8 of our capacity, shrink by half
                @SuppressWarnings("unchecked") E[] newEnts = (E[])new Entry[_entries.length/2];
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
     * Updates the specified entry by locating an entry whose key matches the key of the supplied
     * entry and overwriting it. This should not be called directly, instead the associated
     * <code>update{Set}()</code> method should be called on the distributed object that contains
     * the set in question.
     *
     * @return the old entry that was replaced, or null if it was not found (in which case nothing
     * is updated).
     */
    protected E update (E elem)
    {
        // look up this entry's position in our set
        int eidx = ArrayUtil.binarySearch(_entries, 0, _size, elem, ENTRY_COMP);

        // if we found it, update it
        if (eidx >= 0) {
            E oldEntry = _entries[eidx];
            _entries[eidx] = elem;
            _modCount++;
            return oldEntry;
        } else {
            return null;
        }
    }

    /**
     * Returns the minimum size where we should warn that we're getting a bit large.
     */
    protected int getWarningSize ()
    {
        return 2048;
    }

    /**
     * Generates a shallow copy of this object in a type safe manner.
     *
     * @deprecated clone() works just fine now.
     */
    @Deprecated
    public DSet<E> typedClone ()
    {
        return clone();
    }

    /**
     * Generates a shallow copy of this object.
     */
    @Override
    public DSet<E> clone ()
    {
        try {
            @SuppressWarnings("unchecked") DSet<E> nset = (DSet<E>)super.clone();
            @SuppressWarnings("unchecked") E[] copy = (E[])new Entry[_entries.length];
            nset._entries = copy;
            System.arraycopy(_entries, 0, nset._entries, 0, _entries.length);
            nset._modCount = 0;
            return nset;
        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse);
        }
    }

    @Override
    public String toString ()
    {
        StringBuilder buf = new StringBuilder("(");
        String prefix = "";
        for (E elem : _entries) {
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
        @SuppressWarnings("unchecked") E[] entries = (E[])new Entry[capacity];
        _entries = entries;
        for (int ii = 0; ii < _size; ii++) {
            @SuppressWarnings("unchecked") E entry = (E)in.readObject();
            _entries[ii] = entry;
        }
    }

    /** The entries of the set (in a sparse array). */
    @SuppressWarnings("unchecked") protected E[] _entries = (E[])new Entry[INITIAL_CAPACITY];

    /** The number of entries in this set. */
    protected int _size;

    /** Used to check for concurrent modification. */
    protected transient int _modCount;

    /** The default capacity of a set instance. */
    protected static final int INITIAL_CAPACITY = 2;

    /** Used for lookups and to keep the set contents sorted on insertions. */
    protected static Comparator<Entry> ENTRY_COMP = new Comparator<Entry>() {
        public int compare (Entry e1, Entry e2) {
            return DSet.compare(e1.getKey(), e2.getKey());
        }
    };
}
