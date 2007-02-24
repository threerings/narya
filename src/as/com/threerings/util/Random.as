//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.util
{

/**
 * A seedable pseudorandom generator of the Mersenne Twister variety, with an extremely
 * long period. Note that this is not a cryptographically sound generator.
 * This implementation is based on one found at --
 *    http://onegame.bona.jp/tips/mersennetwister.html
 * An explanation of the algorithm can be found at --
 *    http://en.wikipedia.org/wiki/Mersenne_twister
 */
public class Random
{
    /**
     * Creates a pseudo random number generation.
     * 
     * @param seed a seed of 0 will randomly seed the generator, anything
     * other than 0 will create a generator with the specified seed.
     */
    public function Random (seed :uint = 0)
    {
        if (seed == 0) {
            seed = uint(seedUniquifier++ + uint(Math.random() * 4294967295));
        }
        x = new Array();
        setSeed(seed);
    }

    /**
     * Sets the seed of the generator.  This will result in the same generator
     * sequence of values as a new generator created with the specified seed.
     */
    public function setSeed (seed :uint) :void
    {
        x[0] = seed;
        for (var i :int = 1; i < N; i++) {
            x[i] = imul(1812433253, x[i - 1] ^ (x[i - 1] >>> 30)) + i;
            x[i] &= 0xffffffff;
        }
        p = 0;
        q = 1;
        r = M;
    }

    /**
     * Returns the an int value n where 0 <= value < n.
     *
     * @param n the range to return.  If this is set to 0 it will return a
     * random integer value.  Anything less than 0 will thrown an error.
     */
    public function nextInt (n :int=0) :int
    {
        if (n < 0)
            throw new Error("n must be positive");

        if (n == 0) return int(next(32));

        // Eyeball Sun's documenation of java.util.Random for an explanation
        // of this while loop.
        var bits :int, val :int;
        do {
            bits = int(next(31));
            val = bits % n;
        } while (bits - val + (n - 1) < 0);
        return val;
    }

    /**
     * Returns a random Boolean value.
     */
    public function nextBoolean () :Boolean
    {
        return next(1) != 0;
    }

    /**
     * Returns a random Number where 0.0 <= value < 1.0.
     */
    public function nextNumber () :Number
    {
        return next(32) / 4294967296;
    }

    protected function next (bits: int) :uint
    {
        var y :uint = (x[p] & UPPER_MASK) | (x[q] & LOWER_MASK);
        x[p] = x[r] ^ (y >>> 1) ^ ((y & 1) * MATRIX_A);
        y = x[p];

        if (++p == N) { p = 0; }
        if (++q == N) { q = 0; }
        if (++r == N) { r = 0; }

        y ^= (y >>> 11);
        y ^= (y << 7) & 0x9d2c5680;
        y ^= (y << 15) & 0xefc60000;
        y ^= (y >>> 18);

        return y >>> (32 - bits);
    }

    protected function imul (a: Number, b: Number): Number {
        var al :Number = a & 0xffff;
        var ah :Number = a >>> 16;
        var bl :Number = b & 0xffff;
        var bh :Number = b >>> 16;
        var ml :Number = al * bl;
        var mh :Number = ((((ml >>> 16) + al * bh) & 0xffff) + ah * bl) & 0xffff;

        return (mh << 16) | (ml & 0xffff);
    }

    protected var x :Array;
    protected var p :int;
    protected var q :int;
    protected var r :int;

    protected static var seedUniquifier :uint = 2812526361;

    protected static const N :int = 624;
    protected static const M :int = 397;
    protected static const UPPER_MASK :uint = 0x80000000;
    protected static const LOWER_MASK :uint = 0x7fffffff;
    protected static const MATRIX_A :uint   = 0x9908b0df;

}
}
