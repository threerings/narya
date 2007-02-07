package com.threerings.util {

import flash.errors.IllegalOperationError;

/**
 * Provides a generic iterator for an Array.
 * No co-modification checking is done.
 */
public class ArrayIterator
    implements Iterator
{
    /**
     * Create an ArrayIterator.
     */
    public function ArrayIterator (arr :Array)
    {
        _arr = arr;
        _index = 0;
    }

    // documentation inherited from interface Iterator
    public function hasNext () :Boolean
    {
        return (_index < _arr.length);
    }

    // documentation inherited from interface Iterator
    public function next () :Object
    {
        _lastIndex = _index;
        return _arr[_index++];
    }

    // documentation inherited from interface Iterator
    public function remove () :void
    {
        if (_lastIndex == -1) {
            throw new IllegalOperationError();

        } else {
            _arr.splice(_lastIndex, 1);
            _lastIndex = -1;
            _index--; // since _lastIndex is always before _index
        }
    }

    /** The array we're iterating over. */
    protected var _arr :Array;

    /** The current index. */
    protected var _index :int;

    /** The last-removed index. */
    protected var _lastIndex :int = -1;
}
}
