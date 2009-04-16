//
// $Id$

package com.threerings.util {

/**
 * A HashMap that stores values weakly: if not referenced anywhere else, they may
 * be garbage collected. Note that the size might change unexpectedly as things are removed.
 */
public class WeakValueHashMap extends HashMap
{
    /**
     * @inheritDoc
     */
    public function WeakValueHashMap (
        loadFactor :Number = 1.75,
        equalsFn :Function = null,
        hashFn :Function = null)
    {
        super(loadFactor, equalsFn, hashFn);
    }

    /** @inheritDoc */
    override public function get (key :Object) :*
    {
        var result :* = super.get(key);
        if (result is WeakReference) { // could also just be undefined or null
            result = WeakReference(result).get();
            if (result === undefined) {
                super.remove(key);
            }
        }
        return result;
    }

    /** @inheritDoc */
    override public function put (key :Object, value :Object) :*
    {
        // store nulls directly
        return unwrap(super.put(key, (value == null) ? null : new WeakReference(value)));
    }

    /** @inheritDoc */
    override public function remove (key :Object) :*
    {
        return unwrap(super.remove(key));
    }

    /** @inheritDoc */
    override public function size () :int
    {
        prune();
        return super.size(); // alternately, we could prune and increment a count for all present..
    }

    /** @inheritDoc */
    override public function forEach (fn :Function) :void
    {
        prune(fn); // just prune & run the forEach at the same time
    }

    /**
     * Prune garbage collected values from the map, optionally calling a function for each
     * found key/value pair.
     */
    protected function prune (fn :Function = null) :void
    {
        var removeKeys :Array = [];
        super.forEach(function (key :*, value :WeakReference) :void {
            var rawVal :* = (value == null) ? null : value.get();
            if (rawVal === undefined) {
                removeKeys.push(key);
            } else if (fn != null) {
                fn(key, rawVal);
            }
        });
        for each (var key :Object in removeKeys) {
            super.remove(key); // slightly more efficient, since we don't need to unwrap
        }
    }

    /**
     * Unwrap a possible WeakReference for returning to the user.
     */
    protected function unwrap (val :*) :*
    {
        return (val is WeakReference) ? WeakReference(val).get() : val;
    }
}
}
