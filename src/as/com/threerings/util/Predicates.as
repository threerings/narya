//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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
 * Predicates suitable for Array.filter() and other needs.
 */
public class Predicates
{
    /**
     * Return a predicate that tests for null (or undefined) items.
     */
    public static function isNull () :Function
    {
        //return not(notNull());
        return function (item :*, ... _) :Boolean {
            return (item == null);
        };
    }

    /**
     * Return a predicate that tests for items that are not null (or undefined).
     */
    public static function notNull () :Function
    {
        return function (item :*, ... _) :Boolean {
            return (item != null);
        };
    }

    /**
     * Return a predicate that tests for items that are "is" the specified class.
     */
    public static function instanceOf (clazz :Class) :Function
    {
        return function (item :*, ... _) :Boolean {
            return (item is clazz);
        };
    }

    /**
     * Return a predicate that is the negation of the specified predicate.
     */
    public static function not (pred :Function) :Function
    {
        return function (... args) :Boolean {
            return !pred.apply(null, args);
        };
    }

    /**
     * Return a predicate that is true if all the specified predicate Functions are true
     * for any item.
     */
    public static function and (... predicates) :Function
    {
        return function (... args) :Boolean {
            for each (var pred :Function in predicates) {
                if (!pred.apply(null, args)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Return a predicate that is true if any of the specified predicate Functions are true
     * for any item.
     */
    public static function or (... predicates) :Function
    {
        return function (... args) :Boolean {
            for each (var pred :Function in predicates) {
                if (pred.apply(null, args)) {
                    return true;
                }
            }
            return false;
        };
    }
}
}
