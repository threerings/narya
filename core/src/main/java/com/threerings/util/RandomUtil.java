//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Maintained for backwards compatibility with old Game Gardens games.
 *
 * @deprecated moved to {@link com.samskivert.util.RandomUtil}.
 */
@Deprecated
public class RandomUtil
{
    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static int getInt (int high)
    {
        return com.samskivert.util.RandomUtil.getInt(high);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static int getInt (int high, int low)
    {
        return com.samskivert.util.RandomUtil.getInt(high, low);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static float getFloat (float high)
    {
        return com.samskivert.util.RandomUtil.getFloat(high);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static int getWeightedIndex (int[] weights)
    {
        return com.samskivert.util.RandomUtil.getWeightedIndex(weights);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static int getWeightedIndex (float[] weights)
    {
        return com.samskivert.util.RandomUtil.getWeightedIndex(weights);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static <T> T pickRandom (T[] values)
    {
        return com.samskivert.util.RandomUtil.pickRandom(values);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static <T> T pickRandom (T[] values, T skip)
    {
        return com.samskivert.util.RandomUtil.pickRandom(values, skip);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static <T> T pickRandom (Collection<T> values)
    {
        return com.samskivert.util.RandomUtil.pickRandom(values);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static <T> T pickRandom (List<T> values)
    {
        return com.samskivert.util.RandomUtil.pickRandom(values);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static <T> T pickRandom (List<T> values, T skip)
    {
        return com.samskivert.util.RandomUtil.pickRandom(values, skip);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static <T> T pickRandom (List<T> values, T skip, Random r)
    {
        return com.samskivert.util.RandomUtil.pickRandom(values, skip, r);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static <T> T pickRandom (Iterator<T> iter, int count)
    {
        return com.samskivert.util.RandomUtil.pickRandom(iter, count);
    }

    /**
     * @deprecated use {@link com.samskivert.util.RandomUtil}.
     */
    @Deprecated
    public static <T> T pickRandom (Iterator<T> iter, int count, T skip)
    {
        return com.samskivert.util.RandomUtil.pickRandom(iter, count, skip);
    }
}
