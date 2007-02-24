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
