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

    /**
     * Picks a random object from the supplied array of values. Even weight is given to all
     * elements of the array.
     *
     * @return a randomly selected item or null if the array is null or of length zero.
     */
    public static function pickRandom (values :Array, randomFn :Function = null) :Object
    {
        if (values == null || values.length == 0) {
            return null;
        } else {
            var rand :Number = (randomFn == null) ? Math.random() : randomFn();
            return values[Math.floor(rand * values.length)];
        }
    }
}
}
