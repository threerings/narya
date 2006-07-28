package com.threerings.util {

/**
 * Contains methods that should be in Array, but aren't. Additionally
 * contains methods that understand the interfaces in this package.
 * So, for example, removeFirst() understands Equalable and will remove
 * an element that is equals() to the specified element, rather than just
 * ===.
 */
public class ArrayUtil
{
    /**
     * Sort the specified array according to natural order- all elements
     * must implement Comparable.
     */
    public static function sort (arr :Array) :void
    {
        // ensure every item is Comparable
        if (!arr.every(function (item :*, index :int, array :Array) :Boolean 
                {
                    return (item is Comparable);
                })) {
            throw new Error("Not all elements are Comparable instances.");
        }

        arr.sort(function (obj1 :Object, obj2 :Object) :int {
            return (obj1 as Comparable).compareTo(obj2);
        });
    }

    public static function indexOf (arr :Array, element :Object) :int
    {
        if (arr != null) {
            for (var ii :int = 0; ii < arr.length; ii++) {
                if (Util.equals(arr[ii], element)) {
                    return ii;
                }
            }
        }
        return -1; // never found
    }

    /**
     * Remove the first instance of the specified element from the array.
     */
    public static function removeFirst (arr :Array, element :Object) :void
    {
        removeImpl(arr, element, true);
    }

    /**
     * Remove the last instance of the specified element from the array.
     */
    public static function removeLast (arr :Array, element :Object) :void
    {
        arr.reverse();
        removeFirst(arr, element);
        arr.reverse();
    }

    /**
     * Removes all instances of the specified element from the array.
     */
    public static function removeAll (arr :Array, element :Object) :void
    {
        removeImpl(arr, element, false);
    }

    /**
     * Implementation of remove methods.
     */
    private static function removeImpl (
        arr :Array, element :Object, firstOnly :Boolean) :void
    {
        for (var ii :int = 0; ii < arr.length; ii++) {
            if (Util.equals(arr[ii], element)) {
                arr.splice(ii--, 1);
                if (firstOnly) {
                    return;
                }
            }
        }
    }
}
}
