//
// $Id$

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
