//
// $Id: MathUtil.java,v 1.1 2001/10/12 00:03:03 mdb Exp $

package com.threerings.parlor.util;

import java.util.Random;

/**
 * Contains math-related utility functions that are pertinent to game
 * development.
 */
public class MathUtil
{
    /**
     * Returns a pseudo-random integer in the range from zero (inclusive)
     * to <code>maxValue</code> (exclusive). This presently uses an
     * instance of {@link Random} which uses a 48-bit seed, which is
     * modified using a linear congruential formula. (See Donald Knuth,
     * The Art of Computer Programming, Volume 2, Section 3.2.1.)
     */
    public static int random (int maxValue)
    {
        return _random.nextInt(maxValue);
    }

    protected static Random _random = new Random();
}
