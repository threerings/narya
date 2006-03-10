package com.threerings.util {

/**
 * Java has Iterator, ActionScript has IViewCursor.
 * The problem is, IViewCursor defines 14 methods and 5 read-only properties.
 * That is a serious PITA to write for every collection that might desire
 * iteration. This provides a simpler alternative.
 */
public interface Iterator
{
    /**
     * Is there another element available?
     */
    function hasNext () :Boolean;

    /**
     * Returns the next element.
     */
    function next () :Object;

    // TODO: remove() ?
}
}
