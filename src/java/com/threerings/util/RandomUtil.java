//
// $Id: RandomUtil.java,v 1.6 2003/03/05 02:50:28 ray Exp $

package com.threerings.util;

import com.samskivert.util.IntListUtil;

import java.util.Random;

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
     *
     * For example, passing in {1, 0, 3, 4} will return
     * <table><tr><td>0</td><td>1/8th of the time</td></tr>
     * <tr><td>1</td><td>never</td></tr>
     * <tr><td>2</td><td>3/8th of the time</td></tr>
     * <tr><td>3</td><td>half of the time</td></tr></table>
     */
    public static int getWeightedIndex (int[] weights)
    {
        int pick = getInt(IntListUtil.sum(weights));
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
}
