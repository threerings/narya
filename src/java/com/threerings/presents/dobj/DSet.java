//
// $Id: DSet.java,v 1.16 2002/03/18 23:21:26 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.io.Streamable;

/**
 * The distributed set class provides a means by which an unordered set of
 * objects can be maintained as a distributed object field. Entries can be
 * added to and removed from the set, requests for which will generate
 * events much like other distributed object fields.
 *
 * <p> A set can be either homogenous, whereby the type of object to be
 * contained in the set is configured before the set is used and does not
 * change; or heterogenous, whereby a set can contain any type of entry as
 * long as it implements {@link Entry}. Homogenous sets take advantage of
 * their homogeneity by not transfering the classname of each entry as it
 * is sent over the wire.
 *
 * <p> Classes that wish to act as set entries must implement the {@link
 * Entry} interface which extends {@link Streamable} and adds the
 * requirement that the object provide a key which will be used to
 * identify entry equality. Thus an entry is declared to be in a set of
 * the object returned by that entry's <code>geyKey()</code> method is
 * equal (using <code>equal()</code>) to the entry returned by the
 * <code>getKey()</code> method of some other entry in the set.
 * Additionally, in the case of entry removal, only the key for the entry
 * to be removed will be transmitted with the removal event to save
 * network bandwidth. Lastly, the object returned by <code>getKey()</code>
 * must be a valid distributed object type.
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
     * Constructs a distributed set that will contain the specified entry
     * type.
     */
    public DSet (Class entryType)
    {
        setEntryType(entryType);
    }

    /**
     * Creates a distributed set and populates it with values from the
     * supplied iterator. This should be done before the set is unleashed
     * into the wild distributed object world because no associated entry
     * added events will be generated. Additionally, this operation does
     * not check for duplicates when adding entries, so one should be sure
     * that the iterator contains only unique entries.
     *
     * @param entryType the type of entries that will be stored in this
     * set. <em>Only</em> entries of this <em>exact</em> type may be
     * stored in the set.
     * @param source an iterator from which we will initially populate the
     * set.
     */
    public DSet (Class entryType, Iterator source)
    {
        this(source);
        setEntryType(entryType);
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
     * Constructs a distributed set without specifying the entry type. The
     * set will assume that it is heterogenous, unless a homogenous class
     * type is otherwise specified via {@link #setEntryType}.
     */
    public DSet ()
    {
    }

    /**
     * Returns true if this set contains only entries of exactly the same
     * type, false if not.
     */
    public boolean homogenous ()
    {
        return _entryType != null;
    }

    /**
     * Indicates what type of entries will be stored in this set. This can
     * be called multiple times before the set is used (in the event that
     * one wishes to further specialize the contents of a set that has
     * already been configured to use a particular entry type), but once
     * the set goes into use, it must not be changed. Also bear in mind
     * that the class of entries added to the set are not checked at
     * runtime, and adding entries of invalid class will simply result in
     * the serialization mechanism failing when an event is dispatched to
     * broadcast the addition of an entry.
     */
    public void setEntryType (Class entryType)
    {
        _entryType = entryType;
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
     * Serializes this instance to the supplied output stream.
     */
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        if (_entryType == null) {
            out.writeUTF("");
        } else {
            out.writeUTF(_entryType.getName());
        }
        out.writeInt(_size);
        int elength = _entries.length;
        for (int i = 0; i < elength; i++) {
            Entry elem = _entries[i];
            if (elem != null) {
                if (_entryType == null) {
                    out.writeUTF(elem.getClass().getName());
                }
                elem.writeTo(out);
            }
        }
    }

    /**
     * Unserializes this instance from the supplied input stream.
     */
    public void readFrom (DataInputStream in)
        throws IOException
    {
        // read our entry class and forName() it (if we read an entry
        // class, we're a homogenous set; otherwise we're heterogenous)
        String eclass = in.readUTF();
        try {
            if (!StringUtil.blank(eclass)) {
                _entryType = Class.forName(eclass);
            }

        } catch (Exception e) {
            String err = "Unable to instantiate entry class [err=" + e + "]";
            throw new IOException(err);
        }

        // find out how many entries we'll be reading
        _size = in.readInt();

        // make sure we can fit _size entries
        expand(_size);

        for (int i = 0; i < _size; i++) {
            _entries[i] = readEntry(in);
        }
    }

    /**
     * Reads an entry from the wire and unserializes it. Takes into
     * account whether or not we're a homogenous set.
     */
    public Entry readEntry (DataInputStream in)
        throws IOException
    {
        try {
            Entry elem = null;

            // instantiate the appropriate entry instance
            if (_entryType != null) {
                elem = (Entry)_entryType.newInstance();
            } else {
                elem = (Entry)Class.forName(in.readUTF()).newInstance();
            }

            // unserialize it and return it
            elem.readFrom(in);
            return elem;

        } catch (Exception e) {
            Log.warning("Unable to unserialize set entry " +
                        "[set=" + this + "].");
            Log.logStackTrace(e);
            return null;
        }
   }

    /**
     * Generates a shallow copy of this object.
     */
    public Object clone ()
    {
        DSet nset = new DSet(_entryType);
        nset._entries = new Entry[_entries.length];
        System.arraycopy(_entries, 0, nset._entries, 0, _entries.length);
        nset._size = _size;
        return nset;
    }

    /**
     * Generates a string representation of this set instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        if (_entryType == null) {
            buf.append("etype=NONE");
        } else {
            buf.append("etype=").append(_entryType.getName());
        }
        buf.append(", elems=(");
        String prefix = "";
        for (int i = 0; i < _entries.length; i++) {
            Entry elem = _entries[i];
            if (elem != null) {
                buf.append(prefix);
                prefix = ", ";
                buf.append(elem);
            }
        }
        buf.append(")]");
        return buf.toString();
    }

    protected void expand (int index)
    {
        // sanity check
        if (index < 0) {
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

    /** The type of entry this set holds. */
    protected Class _entryType;

    /** The entries of the set (in a sparse array). */
    protected Entry[] _entries = new Entry[INITIAL_CAPACITY];

    /** The number of entries in this set. */
    protected int _size;

    /** The default capacity of a set instance. */
    protected static final int INITIAL_CAPACITY = 2;
}
