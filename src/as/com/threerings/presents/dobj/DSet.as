package com.threerings.presents.dobj {

import flash.util.StringBuilder;

import mx.collections.IViewCursor;

import com.threerings.util.Equalable;

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
    implements Streamable
{
    /**
     * Returns the number of entries in this set.
     */
    public function size () :int
    {
        return _entries.length;
    }

    /**
     * Returns true if the set contains an entry whose
     * <code>getKey()</code> method returns a key that
     * <code>equals()</code> the key returned by <code>getKey()</code> of
     * the supplied entry. Returns false otherwise.
     */
    public function contains (elem :DSetEntry) :Boolean
    {
        return containsKey(elem.getKey());
    }

    /**
     * Returns true if an entry in the set has a key that
     * <code>equals()</code> the supplied key. Returns false otherwise.
     */
    public function containsKey (key :Object) :Boolean
    {
        return getByKey(key) != null;
    }

    /**
     * Returns the entry that matches (<code>getKey().equals(key)</code>)
     * the specified key or null if no entry could be found that matches
     * the key.
     */
    public function getByKey (key :Object) :DSetEntry
    {
        // o(n) for now
        for each (var entry :DSetEntry in _entries) {
            if (isSameKey(key, entry.getKey())) {
                return entry;
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
    public function getCursor () :IViewCursor
    {
        return null; // jesus, what a pain in the ass to make our
        // own IViewCursor since we can't have inner classes

//        // the crazy sanity checks
//        if (_size < 0 ||_size > _entries.length ||
//            (_size > 0 && _entries[_size-1] == null)) {
//            Log.warning("DSet in a bad way [size=" + _size +
//                        ", entries=" + StringUtil.toString(_entries) + "].");
//            Thread.dumpStack();
//        }
//
//        return new Iterator() {
//            public boolean hasNext () {
//                checkComodification();
//                return (_index < _size);
//            }
//            public Object next () {
//                checkComodification();
//                return _entries[_index++];
//            }
//            public void remove () {
//                throw new UnsupportedOperationException();
//            }
//            protected void checkComodification () {
//                if (_modCount != _expectedModCount) {
//                    throw new ConcurrentModificationException();
//                }
//                if (_ssize != _size) {
//                    Log.warning("Size changed during iteration " +
//                                "[ssize=" + _ssize + ", nsize=" + _size +
//                                ", entsries=" + StringUtil.toString(_entries) +
//                                "].");
//                    Thread.dumpStack();
//                }
//            }
//            protected int _index = 0;
//            protected int _ssize = _size;
//            protected int _expectedModCount = _modCount;
//        };
    }

    /**
     * Copies the elements of this distributed set into the supplied
     * array. If the array is not large enough to hold all of the
     * elements, as many as fit into the array will be copied. If the
     * <code>array</code> argument is null, an object array of sufficient
     * size to contain all of the elements of this set will be created and
     * returned.
     */
    public function toArray () :Array
    {
        return _entries.concat(null); // makes a copy
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
    internal function add (elem :DSetEntry) :Boolean
    {
        if (contains(elem)) {
            Log.warning("Refusing to add duplicate entry [set=" + this +
                  ", entry=" + elem + "].");
            return false;
        }

        _entries.push(elem);
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
    internal function remove (elem :DSetEntry) :Boolean
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
    internal function removeKey (key :Object) :DSetEntry
    {
        // o(n) for now
        for (var ii :int = 0; ii < _entries.length; ii++) {
            var entry :DSetEntry = _entries[ii];
            if (isSameKey(key, entry.getKey())) {
                _entries.splice(ii, 1);
                return entry;
            }
        }
        return null;
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
    internal function update (elem :DSetEntry) :DSetEntry
    {
        var key :Object = elem.getKey();
        for (var ii :int = 0; ii < _entries.length; ii++) {
            var entry :DSetEntry = _entries[ii];
            if (isSameKey(key, entry.getKey())) {
                _entries[ii] = elem;
                return entry; // return the old entry
            }
        }
        return null;
    }

    /**
     * Internal function to determine whether a key matches the key for
     * an existing entry.
     */
    private function isSameKey (key :Object, otherKey :Object) :Boolean
    {
        return (key is Equalable) ? (key as Equalable).equals(otherKey)
                                  : (key === otherKey);
    }

    /**
     * Generates a string representation of this set instance.
     */
    public function toString () :String
    {
        var buf :StringBuilder = new StringBuilder("(");
        buf.append(_entries.toString());
        buf.append(")");
        return buf.toString();
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeObject(_entries);
        out.writeInt(_entries.length);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        _entries = (ins.readObject() as Array);
        _entries.length = ins.readInt(); // then read the length and limit it
    }

    /** The entries of the set (in a sparse array). */
    protected var _entries :Array;
}
}
