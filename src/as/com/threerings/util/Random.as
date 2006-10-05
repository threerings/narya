package com.threerings.util {

// A simple psuedo random number generator that allows seeding.
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
        setSeed(seed);
    }

    /**
     * Sets the seed of the generator.  This will result in the same generator
     * sequence of values as a new generator created with the specified seed.
     */
    public function setSeed (seed :uint) :void
    {
        seed = uint(seed ^ multiplier);
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
        return Number(next(32) / (1 << 32));
    }

    protected function next (bits :int) :uint
    {
        seed = uint(seed * multiplier + addend)
        return uint(seed >>> (32 - bits));
    }

    protected var seed :uint;
    protected static var seedUniquifier :uint = 2812526361;
    protected static const multiplier :uint = 452871439;
    protected static const addend :uint = 11;
}
}
