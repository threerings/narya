//
// $Id: DSet.java,v 1.17 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;
import java.util.Iterator;

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
 * equal (using {@link Object#equal}) to the entry returned by the {@link
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
        public Object getKey ();
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
        for (int index = 0; source.hasNext(); index++) {
            Entry elem = (Entry)source.next();

            // expand the array if necessary
            if (index >= _entries.length) {
                expand(index);
            }

            // insert the item
            _entries[index] = elem;
            _size++;
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
            public boolean hasNext ()
            {
                // we need to scan to the next entry the first time
                if (_index < 0) {
                    scanToNext();
                }
                return (_index < _entries.length);
            }

            public Object next ()
            {
                Object val = _entries[_index];
                scanToNext();
                return val;
            }

            public void remove ()
            {
                throw new UnsupportedOperationException();
            }

            protected void scanToNext ()
            {
                for (_index++; _index < _entries.length; _index++) {
                    if (_entries[_index] != null) {
                        return;
                    }
                }
            }

            int _index = -1;
        };
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
        Object key = elem.getKey();
        int elength = _entries.length;
        int index = elength;

        // scan the array looking for a slot and/or the entry already in
        // the set
        for (int i = 0; i < elength; i++) {
            Entry el = _entries[i];
            // the array may be sparse
            if (el == null) {
                if (index == elength) {
                    index = i;
                }
            } else if (el.getKey().equals(key)) {
                return false;
            }
        }

        // expand the array if necessary
        if (index >= _entries.length) {
            expand(index);
        }

        // insert the item
        _entries[index] = elem;
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
        // scan the array looking for a matching entry
        int elength = _entries.length;
        for (int i = 0; i < elength; i++) {
            Entry el = _entries[i];
            if (el != null && el.getKey().equals(key)) {
                _entries[i] = null;
                _size--;
                return true;
            }
        }
        return false;
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
        Object key = elem.getKey();

        // scan the array looking for a matching entry
        int elength = _entries.length;
        for (int i = 0; i < elength; i++) {
            Entry el = _entries[i];
            if (el != null && el.getKey().equals(key)) {
                _entries[i] = elem;
                return true;
            }
        }

        return false;
    }

    /**
     * Generates a shallow copy of this object.
     */
    public Object clone ()
    {
        DSet nset = new DSet();
        nset._entries = new Entry[_entries.length];
        System.arraycopy(_entries, 0, nset._entries, 0, _entries.length);
        nset._size = _size;
        return nset;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
        out.writeInt(_size);
        int ecount = _entries.length;
        for (int ii = 0; ii < ecount; ii++) {
            if (_entries[ii] != null) {
                out.writeObject(_entries[ii]);
            }
        }
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        _size = in.readInt();
        _entries = new Entry[Math.max(_size, INITIAL_CAPACITY)];
        for (int ii = 0; ii < _size; ii++) {
            _entries[ii] = (Entry)in.readObject();
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

    protected void expand (int index)
    {
        // sanity check
        if (index < 0 || index > Short.MAX_VALUE) {
            Log.warning("Requested to expand to accomodate bogus index! " +
                        "[index=" + index + "].");
            Thread.dumpStack();
            index = 0;
        }

        // increase our length in powers of two until we're big enough
        int tlength = _entries.length;
        while (index >= tlength) {
            tlength *= 2;
        }

        // further sanity checks
        if (tlength > 4096) {
            Log.warning("Requested to expand to questionably large size " +
                        "[index=" + index + ", tlength=" + tlength + "].");
            Thread.dumpStack();
        }

        // create a new array and copy our data into it
        Entry[] elems = new Entry[tlength];
        System.arraycopy(_entries, 0, elems, 0, _entries.length);
        _entries = elems;
    }

    /** The entries of the set (in a sparse array). */
    protected Entry[] _entries = new Entry[INITIAL_CAPACITY];

    /** The number of entries in this set. */
    protected int _size;

    /** The default capacity of a set instance. */
    protected static final int INITIAL_CAPACITY = 2;
}
