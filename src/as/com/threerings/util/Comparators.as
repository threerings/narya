//
// $Id$

package com.threerings.util {

/**
 * Contains sorting Comparators.
 * These functions are suitable for passing to Array.sort(), or with a flex Sort object.
 */
public class Comparators
{
    /**
     * A standard Comparator for comparing Comparable values.
     */
    public static function COMPARABLE (c1 :Comparable, c2 :Comparable, ... ignored) :int
    {
        if (c1 == c2) { // same object -or- both null
            return 0;
        } else if (c1 == null) {
            return -1;
        } else if (c2 == null) {
            return 1;
        } else {
            return c1.compareTo(c2);
        }
    }

    /**
     * Create a Comparator function that reverses the ordering of the specified Comparator.
     */
    public static function createReverse (comparator :Function) :Function
    {
        return function (o1 :Object, o2 :Object, ... ignored) :int {
            return comparator(o2, o1); // simply reverse the ordering
        }
    }
}
}
