//
// $Id: RandomUtil.java,v 1.5 2002/04/15 18:18:20 mdb Exp $

package com.threerings.util;

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
}
