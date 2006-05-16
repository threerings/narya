package com.threerings.util {

import flash.utils.Dictionary;

import mx.utils.ObjectUtil;

/**
 * An implementation of a HashMap in actionscript.
 */
public class HashMap
{
    public function HashMap (loadFactor :Number = 1.75)
    {
        _loadFactor = loadFactor;
    }

    public function clear () :void
    {
        _simpleData = null;
        _size = 0;
    }

    public function containsKey (key :Object) :Boolean
    {
        return (undefined !== get(key));
    }

    /**
     * Get the value stored in the map for the specified key, or
     * <code>undefined</code> if there is no value for that key.
     */
    public function get (key :Object) :*
    {
        if (ObjectUtil.isSimple(key)) {
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

    public function isEmpty () :Boolean
    {
        return (_size == 0);
    }

    public function put (key :Object, value :Object) :*
    {
        if (ObjectUtil.isSimple(key)) {
            if (_simpleData == null) {
                _simpleData = new Dictionary();
            }

            var oldValue :* = _simpleData[key];
            _simpleData[key] = value;
            if (oldValue === undefined) {
                _size++;
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
        _size++;
        // check to see if we should grow the map
        if (_size > _entries.length * _loadFactor) {
            resize(2 * _entries.length);
        }
        // indicate that there was no value previously stored for the key
        return undefined;
    }

    public function remove (key :Object) :*
    {
        if (ObjectUtil.isSimple(key)) {
            if (_simpleData == null) {
                return undefined;
            }

            var oldValue :* = _simpleData[key];
            if (oldValue !== undefined) {
                _size--;
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
                _size--;
                // check to see if we should shrink the map
                if ((_entries.length > DEFAULT_BUCKETS) &&
                        (_size < _entries.length * _loadFactor * .125)) {
                    resize(Math.max(DEFAULT_BUCKETS, _entries.length / 2));
                }
                return e.value;
            }
            prev = e;
            e = next;
        }

        return undefined; // never found
    }

    public function size () :int
    {
        return _size;
    }

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

    protected function keyFor (key :Object) :Hashable
    {
        return (key is Hashable) ? (key as Hashable) : new KeyWrapper(key);
    }

    protected function indexFor (hash :int) :int
    {
        return Math.abs(hash % _entries.length);
    }

    protected function resize (newSize :int) :void
    {
        var oldEntries :Array = _entries;
        _entries = new Array();
        _entries.length = newSize;

        // place all the old entries in the new map
        for (var ii :int = 0; ii < oldEntries.length; ii++) {
            var e :Entry = (oldEntries[ii] as Entry);
            if (e != null) {
                do {
                    var next :Entry = e.next;
                    var index :int = indexFor(e.hash);
                    e.next = _entries[index];
                    _entries[index] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    protected var _size :int = 0;

    protected var _loadFactor :Number;

    protected var _simpleData :Dictionary

    protected var _entries :Array;

    /** The default size for the bucketed hashmap. */
    protected static const DEFAULT_BUCKETS :int = 16;
}

} // end: package

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

import mx.utils.ObjectUtil;

class KeyWrapper
    implements Hashable
{
    public var key :Object;

    public function KeyWrapper (key :Object)
    {
        this.key = key;
        _hash = 0;
        var uid :String = ObjectUtil.toString(key);
        for (var ii :int = 0; ii < uid.length; ii++) {
            _hash = (_hash << 1) ^ int(uid.charCodeAt(ii));
        }
    }

    public function equals (other :Object) :Boolean
    {
        return (other is KeyWrapper) &&
            (0 == ObjectUtil.compare(key, (other as KeyWrapper).key));
    }

    public function hashCode () :int
    {
        return _hash;
    }

    protected var _key :Object;
    protected var _hash :int;
}