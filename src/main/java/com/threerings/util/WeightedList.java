//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import com.samskivert.util.RandomUtil;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Tuple;

public class WeightedList<T>
{
    public static <K> WeightedList<K> newList ()
    {
        return new WeightedList<K>();
    }

    public static <K> WeightedList<K> newList (K... entry)
    {
        return new WeightedList<K>().add(entry);
    }

    public static <K> WeightedList<K> newList (Float weight, K entry)
    {
        return new WeightedList<K>().add(weight, entry);
    }

    public static <K> WeightedList<K> newList (Float weight, K... entry)
    {
        return new WeightedList<K>().add(weight, entry);
    }

    /**
     * Adds the given items with a weight of 1.
     */
    public WeightedList<T> add (T... items)
    {
        return add(1f, items);
    }

    /**
     * Adds the given item with the given weight.
     */
    public WeightedList<T> add (float weight, T item)
    {
        _items.add(new Tuple<Float, T>(weight, item));
        _weights = null;
        return this;
    }

    /**
     * Adds all of the given items with the given weight.
     */
    public WeightedList<T> add (float weight, T... items)
    {
        for (T item : items) {
            add(weight, item);
        }
        return this;
    }

    /**
     * Returns the weight for the first found instance of the given item,
     * or -1 if we don't know about it.
     */
    public float getWeight (T item)
    {
        for (Tuple<Float, T> tup : _items) {
            if (tup.right.equals(item)) {
                return tup.left;
            }
        }

        return -1;
    }

    /**
     * Removes the record we have for this item at this weight.
     */
    public boolean remove (float weight, T item)
    {
        _weights = null;
        return _items.remove(new Tuple<Float, T>(weight, item));
    }

    public T pickRandom ()
    {
        return pickRandom(RandomUtil.rand);
    }

    public T pickRandom (Random rand)
    {
        if (_weights == null) {
            _weights = new float[_items.size()];

            for (int ii = 0; ii < _weights.length; ii++) {
                _weights[ii] = _items.get(ii).left;
            }
        }

        int idx = RandomUtil.getWeightedIndex(_weights, rand);
        return idx == -1 ? null : _items.get(idx).right;
    }

    public List<Tuple<Float, T>> getItems ()
    {
        return _items;
    }

    public int size ()
    {
        return _items.size();
    }

    @Override
    public String toString ()
    {
        return StringUtil.toString(_items);
    }

    protected float[] _weights;

    protected List<Tuple<Float, T>> _items = Lists.newArrayList();
}

