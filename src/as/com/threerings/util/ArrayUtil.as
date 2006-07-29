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

    public static function contains (arr :Array, element :Object) :Boolean
    {
        return (indexOf(arr, element) != -1);
    }

    /**
     * Remove the first instance of the specified element from the array.
     *
     * @return true if an element was removed, false otherwise.
     */
    public static function removeFirst (arr :Array, element :Object) :Boolean
    {
        return removeImpl(arr, element, true);
    }

    /**
     * Remove the last instance of the specified element from the array.
     *
     * @return true if an element was removed, false otherwise.
     */
    public static function removeLast (arr :Array, element :Object) :Boolean
    {
        arr.reverse();
        var removed :Boolean = removeFirst(arr, element);
        arr.reverse();
        return removed;
    }

    /**
     * Removes all instances of the specified element from the array.
     *
     * @return true if at least one element was removed, false otherwise.
     */
    public static function removeAll (arr :Array, element :Object) :Boolean
    {
        return removeImpl(arr, element, false);
    }

    /**
     * Implementation of remove methods.
     */
    private static function removeImpl (
        arr :Array, element :Object, firstOnly :Boolean) :Boolean
    {
        var removed :Boolean = false;
        for (var ii :int = 0; ii < arr.length; ii++) {
            if (Util.equals(arr[ii], element)) {
                arr.splice(ii--, 1);
                if (firstOnly) {
                    return true;
                }
                removed = true;
            }
        }
        return removed;
    }
}
}
