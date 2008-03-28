//
// $Id$

package com.threerings.util {

/**
 * Random Random utilities.
 */
public class RandomUtil
{
    /**
     * Return the picked index from a weighted list.
     *
     * @param weights an Array containing Numbers, ints, or uints. All values should be
     * non-negative.
     * @param a random function that returns (0 <= value < 1), if null then Math.random() will
     * be used.
     */
    public static function getWeightedIndex (weights :Array, randomFn :Function = null) :int
    {
        var sum :Number = 0;
        for each (var n :Number in weights) {
            sum += n;
        }
        if (sum < 0) {
            return -1;
        }
        var pick :Number = ((randomFn == null) ? Math.random() : randomFn()) * sum;
        for (var ii :int = 0; ii < weights.length; ii++) {
            pick -= Number(weights[ii]);
            if (pick < 0) {
                return ii;
            }
        }

        // since we're dealing with floats, it's possible that a rounding error left us here
        return 0; // TODO: largest weighted? Re-pick?
    }
}
}
