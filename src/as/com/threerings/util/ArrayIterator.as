package com.threerings.util {

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
        return _arr[_index++];
    }

    /** The array we're iterating over. */
    protected var _arr :Array;

    /** The current index. */
    protected var _index :int;
}
}
