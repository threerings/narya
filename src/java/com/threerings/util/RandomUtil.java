//
// $Id: RandomUtil.java,v 1.1 2001/10/23 02:01:29 shaper Exp $

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

    /** The random object from which we choose random numbers. */
    public static Random _rnd = new Random();
}
