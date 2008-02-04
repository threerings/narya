//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.util {

/**
 * Contains methods that should be in Array, but aren't. Additionally
 * contains methods that understand the interfaces in this package.
 * So, for example, removeFirst() understands Equalable and will remove
 * an element that is equals() to the specified element, rather than just
 * === (strictly equals) to the specified element.
 */
public class ArrayUtil
{
    /**
     * Sort the specified array according to natural order- all elements
     * must implement Comparable or be null.
     */
    public static function sort (arr :Array) :void
    {
        arr.sort(function (obj1 :Object, obj2 :Object) :int {
            if (obj1 == obj2) { // same object or both null
                return 0;
            } else if (obj1 == null) {
                return -1;
            } else if (obj2 == null) {
                return 1;
            } else {
                return Comparable(obj1).compareTo(obj2);
            }
        });
    }

    /**
     * Randomly shuffle the elements in the specified array.
     *
     * @param rando a random number generator to use, or null if you don't care.
     */
    public static function shuffle (arr :Array, rando :Random = null) :void
    {
        var randFunc :Function = (rando != null) ? rando.nextInt :
            function (n :int) :int {
                return int(Math.random() * n);
            };
        // starting from the end of the list, repeatedly swap the element in
        // question with a random element previous to it up to and including
        // itself
        for (var ii :int = arr.length - 1; ii > 0; ii--) {
            var idx :int = randFunc(ii + 1);
            var tmp :Object = arr[idx];
            arr[idx] = arr[ii];
            arr[ii] = tmp;
        }
    }

    /**
     * Returns the index of the first item in the array for which the predicate function
     * returns true, or -1 if no such item was found. The predicate function should be of type:
     *   function (element :*) :Boolean { }
     *
     * @return the zero-based index of the matching element, or -1 if none found.
     */
    public static function indexIf (arr :Array, predicate :Function) :int
    {
        if (arr != null) {
            for (var ii :int = 0; ii < arr.length; ii++) {
                if (predicate(arr[ii])) {
                    return ii;
                }
            }
        }
        return -1; // never found
    }

    /**
     * Returns the first index of the supplied element in the array. Note that if the element
     * implements Equalable, an element that is equals() will have its index returned, instead
     * of requiring the search element to be === (strictly equal) to an element in the array
     * like Array.indexOf().
     *
     * @return the zero-based index of the matching element, or -1 if none found.
     */
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
     * @return true if the specified element, or one that is Equalable.equals() to it, is
     * contained in the array.
     */
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
     * Do the two arrays contain elements that are all equals()?
     */
    public static function equals (ar1 :Array, ar2 :Array) :Boolean
    {
        if (ar1 === ar2) {
            return true;

        } else if (ar1 == null || ar2 == null || ar1.length != ar2.length) {
            return false;
        }

        for (var jj :int = 0; jj < ar1.length; jj++) {
            if (!Util.equals(ar1[jj], ar2[jj])) {
                return false;
            }
        }
        return true;
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
