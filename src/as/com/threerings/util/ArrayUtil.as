package com.threerings.util {

/**
 * Contains methods that should be in Array, but aren't.
 */
public class ArrayUtil
{
    /**
     * Return the first index of the specified element, or -1 if not present.
     */
    public static function indexOf (arr :Array, element :Object) :int
    {
        // return mx.utils.ArrayUtil.getItemIndex(element, arr);
        //
        for (var ii :int = 0; ii < arr.length; ii++) {
            if (arr[ii] === element) {
                return ii;
            }
        }
        return -1;
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
            if (arr[ii] === element) {
                arr.splice(ii--, 1);
                if (firstOnly) {
                    return;
                }
            }
        }
    }
}
}
