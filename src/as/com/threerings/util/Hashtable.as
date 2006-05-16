package com.threerings.util {

import flash.errors.IllegalOperationError;

import flash.utils.Dictionary;

/**
 * An implementation of a Hashtable in actionscript. Any object (and null) may
 * be used as a key. Simple keys (Number, int, uint, Boolean, String) utilize
 * a Dictionary internally for storage; keys that implement Hashable are
 * stored efficiently, and any other key can also be used if the equalsFn
 * and hashFn are specified to the constructor.
 */
public class Hashtable
    implements Map
{
    /**
     * Construct a Hashtable
     *
     * @param loadFactor - A measure of how full the hashtable is allowed to
     *                     get before it is automatically resized. The default
     *                     value of 1.75 should be fine.
     * @param equalsFn   - (Optional) A function to use to compare object
     *                     equality for keys that are neither simple nor
     *                     implement Hashable. The signature should be
     *                     "function (o1, o2) :Boolean".
     * @param hashFn     - (Optional) A function to use to generate a hash
     *                     code for keys that are neither simple nor
     *                     implement Hashable. The signature should be
     *                     "function (obj) :*", where the return type is
     *                     numeric or String. Two objects that are equals
     *                     according to the specified equalsFn *must*
     *                     generate equal values when passed to the hashFn.
     */
    public function Hashtable (
            loadFactor :Number = 1.75,
            equalsFn :Function = null,
            hashFn :Function = null)
    {
        if ((equalsFn != null) != (hashFn != null)) {
            throw new ArgumentError("Both the equals and hash functions " +
                "must be specified, or neither.");
        }
        _loadFactor = loadFactor;
        _equalsFn = equalsFn;
        _hashFn = hashFn;
    }

    // documentation inherited from interface Map
    public function clear () :void
    {
        _simpleData = null;
        _simpleSize = 0;
        _entries = null;
        _entriesSize = 0;
    }

    // documentation inherited from interface Map
    public function containsKey (key :Object) :Boolean
    {
        return (undefined !== get(key));
    }

    // documentation inherited from interface Map
    public function get (key :Object) :*
    {
        if (isSimple(key)) {
            return (_simpleData == null) ? undefined : _simpleData[key];
        }

        if (_entries == null) {
            return undefined;
        }

        var hkey :Hashable = keyFor(key);
        var hash :int = hkey.hashCode();
        var index :int = indexFor(hash);
        var e :Entry = (_entries[index] as Entry);
        while (true) {
            if (e == null) {
                return null;
            }
            if (e.hash == hash && e.key.equals(hkey)) {
                return e.value;
            }
            e = e.next;
        }
    }

    // documentation inherited from interface Map
    public function isEmpty () :Boolean
    {
        return (size() == 0);
    }

    // documentation inherited from interface Map
    public function put (key :Object, value :Object) :*
    {
        if (isSimple(key)) {
            if (_simpleData == null) {
                _simpleData = new Dictionary();
            }

            var oldValue :* = _simpleData[key];
            _simpleData[key] = value;
            if (oldValue === undefined) {
                _simpleSize++;
            }
            return oldValue;
        }

        // lazy-create the array holding other hashables
        if (_entries == null) {
            _entries = new Array();
            _entries.length = DEFAULT_BUCKETS;
        }

        var hkey :Hashable = keyFor(key);
        var hash :int = hkey.hashCode();
        var index :int = indexFor(hash);
        var firstEntry :Entry = (_entries[index] as Entry);
        for (var e :Entry = firstEntry; e != null; e = e.next) {
            if (e.hash == hash && e.key.equals(hkey)) {
                var oldValue :* = e.value;
                e.value = value;
                return oldValue; // size did not change
            }
        }

        _entries[index] = new Entry(hash, hkey, value, firstEntry);
        _entriesSize++;
        // check to see if we should grow the map
        if (_entriesSize > _entries.length * _loadFactor) {
            resize(2 * _entries.length);
        }
        // indicate that there was no value previously stored for the key
        return undefined;
    }

    // documentation inherited from interface Map
    public function remove (key :Object) :*
    {
        if (isSimple(key)) {
            if (_simpleData == null) {
                return undefined;
            }

            var oldValue :* = _simpleData[key];
            if (oldValue !== undefined) {
                _simpleSize--;
            }
            delete _simpleData[key];
            return oldValue;
        }

        if (_entries == null) {
            return undefined;
        }

        var hkey :Hashable = keyFor(key);
        var hash :int = hkey.hashCode();
        var index :int = indexFor(hash);
        var prev :Entry = (_entries[index] as Entry);
        var e :Entry = prev;

        while (e != null) {
            var next :Entry = e.next;
            if (e.hash == hash && e.key.equals(hkey)) {
                if (prev == e) {
                    _entries[index] = next;
                } else {
                    prev.next = next;
                }
                _entriesSize--;
                // check to see if we should shrink the map
                if ((_entries.length > DEFAULT_BUCKETS) &&
                        (_entriesSize < _entries.length * _loadFactor * .125)) {
                    resize(Math.max(DEFAULT_BUCKETS, _entries.length / 2));
                }
                return e.value;
            }
            prev = e;
            e = next;
        }

        return undefined; // never found
    }

    // documentation inherited from interface Map
    public function size () :int
    {
        return _simpleSize + _entriesSize;
    }

    // documentation inherited from interface Map
    public function keys () :Array
    {
        var keys :Array = new Array();

        // get the simple keys first
        if (_simpleData != null) {
            for (var key :* in _simpleData) {
                keys.push(key);
            }
        }

        // get the more complex keys
        if (_entries != null) {
            for (var ii :int = _entries.length - 1; ii >= 0; ii--) {
                for (var e :Entry = (_entries[ii] as Entry); e != null;
                        e = e.next) {
                    if (e.key is KeyWrapper) {
                        keys.push((e.key as KeyWrapper).key);

                    } else {
                        keys.push(e.key)
                    }
                }
            }
        }

        return keys;
    }

    public function values () :Array
    {
        var vals :Array = new Array();

        // get the simple properties first
        if (_simpleData != null) {
            for each (var value :* in _simpleData) {
                vals.push(value);
            }
        }

        // get the more complex properties
        if (_entries != null) {
            for (var ii :int = _entries.length - 1; ii >= 0; ii--) {
                for (var e :Entry = (_entries[ii] as Entry); e != null;
                        e = e.next) {
                    vals.push(e.value);
                }
            }
        }

        return vals;
    }

    /**
     * Return a Hashable that represents the key.
     */
    protected function keyFor (key :Object) :Hashable
    {
        if (key is Hashable) {
            return (key as Hashable);

        } else if (_hashFn == null) {
            throw new IllegalOperationError("Illegal key specified " +
                "for Hashtable created without hashing functions.");

        } else {
            return new KeyWrapper(key, _equalsFn, _hashFn);
        }
    }

    /**
     * Return an index for the specified hashcode.
     */
    protected function indexFor (hash :int) :int
    {
        return Math.abs(hash) % _entries.length;
    }

    /**
     * Return true if the specified key may be used to store values in a
     * Dictionary object.
     */
    protected function isSimple (key :Object) :Boolean
    {
        return (key is String) || (key is Number) || (key is Boolean);
    }

    /**
     * Resize the entries with Hashable keys to optimize
     * the memory/performance tradeoff.
     */
    protected function resize (newSize :int) :void
    {
        var oldEntries :Array = _entries;
        _entries = new Array();
        _entries.length = newSize;

        // place all the old entries in the new map
        for (var ii :int = 0; ii < oldEntries.length; ii++) {
            var e :Entry = (oldEntries[ii] as Entry);
            while (e != null) {
                var next :Entry = e.next;
                var index :int = indexFor(e.hash);
                e.next = (_entries[index] as Entry);
                _entries[index] = e;
                e = next;
            }
        }
    }

    /** The current number of key/value pairs stored in the Dictionary. */
    protected var _simpleSize :int = 0;

    /** The current number of key/value pairs stored in the _entries. */
    protected var _entriesSize :int = 0;

    /** The load factor. */
    protected var _loadFactor :Number;

    /** If non-null, contains simple key/value pairs. */
    protected var _simpleData :Dictionary

    /** If non-null, contains Hashable keys and their values. */
    protected var _entries :Array;

    /** The hashing function to use for non-Hashable complex keys. */
    protected var _hashFn :Function;

    /** The equality function to use for non-Hashable complex keys. */
    protected var _equalsFn :Function;

    /** The default size for the bucketed hashmap. */
    protected static const DEFAULT_BUCKETS :int = 16;
}

} // end: package com.threerings.util

import com.threerings.util.Hashable;

class Entry
{
    public var key :Hashable;
    public var value :Object;
    public var hash :int;
    public var next :Entry;

    public function Entry (hash :int, key :Hashable, value :Object, next :Entry)
    {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
}

class KeyWrapper
    implements Hashable
{
    public var key :Object;

    public function KeyWrapper (
            key :Object, equalsFn :Function, hashFn :Function)
    {
        this.key = key;
        _equalsFn = equalsFn;
        var hashValue :* = hashFn(key);
        if (hashValue is String) {
            var uid :String = (hashValue as String);
            // examine at most 32 characters of the string
            var inc :int = int(Math.max(1, Math.ceil(uid.length / 32)));
            for (var ii :int = 0; ii < uid.length; ii += inc) {
                _hash = (_hash << 1) ^ int(uid.charCodeAt(ii));
            }

        } else {
            _hash = int(hashValue);
        }
    }

    // documentation inherited from interface Hashable
    public function equals (other :Object) :Boolean
    {
        return (other is KeyWrapper) &&
            Boolean(_equalsFn(key, (other as KeyWrapper).key));
    }

    // documentation inherited from interface Hashable
    public function hashCode () :int
    {
        return _hash;
    }

    protected var _key :Object;
    protected var _hash :int;
    protected var _equalsFn :Function;
}
