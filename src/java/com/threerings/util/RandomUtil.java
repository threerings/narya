//
// $Id: RandomUtil.java,v 1.9 2003/06/04 00:03:58 mdb Exp $

package com.threerings.util;

import java.util.Iterator;
import java.util.Random;

import com.samskivert.util.IntListUtil;

/**
 * Provides miscellaneous utility routines to simplify obtaining
 * useful random number values and to centralize seeding and proper
 * care and feeding of the pseudo-random number generator.
 */
public class RandomUtil
{
    /** The random number generator used by the methods in this class. */
    public static final Random rand = new Random();

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code>
     * value between 0 (inclusive) and the specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public static int getInt (int high)
    {
	return rand.nextInt(high);
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code>
     * value between <code>high</code> and <code>low</code>, exclusive of each.
     */
    public static int getInt (int high, int low)
    {
        return low + 1 + rand.nextInt(high - low - 1);
    }

    /**
     * Returns a pseudorandom, uniformly distributed float value between
     * 0.0 (inclusive) and the specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public static float getFloat (float high)
    {
        return rand.nextFloat() * high;
    }

    /**
     * Pick a random index from the array, weighted by the value of the
     * corresponding array element.
     *
     * @param weights an array of positive integers.
     * @return an index into the array, or -1 if the sum of the weights
     * is less than 1. 
     *
     * For example, passing in {1, 0, 3, 4} will return
     * <table><tr><td>0</td><td>1/8th of the time</td></tr>
     * <tr><td>1</td><td>never</td></tr>
     * <tr><td>2</td><td>3/8th of the time</td></tr>
     * <tr><td>3</td><td>half of the time</td></tr></table>
     */
    public static int getWeightedIndex (int[] weights)
    {
        int sum = IntListUtil.sum(weights);
        if (sum < 1) {
            return -1;
        }
        int pick = getInt(sum);
        for (int ii=0, nn=weights.length; ii < nn; ii++) {
            pick -= weights[ii];
            if (pick < 0) {
                return ii;
            }
        }

        // Impossible!
        Log.logStackTrace(new Throwable());
        return 0;
    }

    /**
     * Picks a random object from the supplied array of values. Even
     * weight is given to all elements of the array.
     *
     * @return a randomly selected item or null if the array is null or of
     * length zero.
     */
    public static Object pickRandom (Object[] values)
    {
        return (values == null || values.length == 0) ? null :
            values[getInt(values.length)];
    }

    /**
     * Picks a random object from the supplied array of values, not
     * including the specified skip object as a possible selection
     * (equality with the skipped object is referential rather than via
     * {@link Object#equals}). The element to be skipped must exist in the
     * array exactly once. Even weight is given to all elements of the
     * array except the skipped element.
     *
     * @return a randomly selected item or null if the array is null, of
     * length zero or contains only the skip item.
     */
    public static Object pickRandom (Object[] values, Object skip)
    {
        if (values == null || values.length < 2) {
            return null;
        }
        int index = getInt(values.length-1);
        for (int ii = 0; ii <= index; ii++) {
            if (values[ii] == skip) {
                index++;
            }
        }
        return (index >= values.length) ? null : values[index];
    }

    /**
     * Picks a random object from the supplied iterator (which must
     * iterate over exactly <code>count</code> objects.
     *
     * @return a randomly selected item.
     *
     * @exception NoSuchElementException thrown if the iterator provides
     * fewer than <code>count</code> elements.
     */
    public static Object pickRandom (Iterator iter, int count)
    {
        if (count < 1) {
            throw new IllegalArgumentException(
                "Must have at least one element [count=" + count + "]");
        }

        Object value = iter.next();
        for (int ii = 0, ll = getInt(count); ii < ll; ii++) {
            value = iter.next();
        }
        return value;
    }

    /**
     * Picks a random object from the supplied iterator (which must
     * iterate over exactly <code>count</code> objects. The specified skip
     * object will be skipped when selecting a random value. The skipped
     * object must exist exactly once in the set of objects returned by
     * the iterator.
     *
     * @return a randomly selected item.
     *
     * @exception NoSuchElementException thrown if the iterator provides
     * fewer than <code>count</code> elements.
     */
    public static Object pickRandom (Iterator iter, int count, Object skip)
    {
        if (count < 2) {
            throw new IllegalArgumentException(
                "Must have at least two elements [count=" + count + "]");
        }

        int index = getInt(count-1);
        Object value = null;
        do {
            value = iter.next();
            if (value == skip) {
                value = iter.next();
            }
        } while (index-- > 0);
        return value;
    }
}
