//
// $Id$

package com.threerings.util {

/**
 * Contains sorting Comparators.
 */
public class Comparators
{
    /**
     * A standard Comparator for comparing Comparable values.
     */
    public static function COMPARABLE (c1 :Comparable, c2 :Comparable) :int
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
}
}
