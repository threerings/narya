//
// $Id: RandomUtil.java,v 1.3 2002/01/15 18:05:01 shaper Exp $

package com.threerings.media.util;

import java.util.Random;

/**
 * Provides miscellaneous utility routines to simplify obtaining
 * useful random number values and to centralize seeding and proper
 * care and feeding of the pseudo-random number generator.
 */
public class RandomUtil
{
    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code>
     * value between 0 (inclusive) and the specified value
     * (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public static int getInt (int high)
    {
	return _rnd.nextInt(high);
    }

    /**
     * Returns a pseudorandom, uniformly distributed float value between
     * 0.0 (inclusive) and the specified value (inclusive).
     */
    public static float getFloat (float high)
    {
        return _rnd.nextFloat() * high;
    }

    /** The random object from which we choose random numbers. */
    protected static Random _rnd = new Random();
}
