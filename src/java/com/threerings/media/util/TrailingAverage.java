//
// $Id: TrailingAverage.java,v 1.2 2004/08/27 02:12:47 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.media.util;

/**
 * Used to compute a trailing average of a value.
 */
public class TrailingAverage
{
    /**
     * Creates a trailing average instance with the default number of
     * values used to compute the average (10).
     */
    public TrailingAverage ()
    {
        this(10);
    }

    /**
     * Creates a trailing average instance with the specified number of
     * values used to compute the average.
     */
    public TrailingAverage (int history)
    {
        _history = new int[history];
    }

    /**
     * Records a new value.
     */
    public void record (int value)
    {
        _history[_index++%_history.length] = value;
    }

    /**
     * Returns the current averaged value.
     */
    public int value ()
    {
        int end = Math.min(_history.length, _index);
        int value = 0;
        for (int ii = 0; ii < end; ii++) {
            value += _history[ii];
        }
        return (end > 0) ? (value/end) : 0;
    }

    /**
     * Returns the current trailing average value as a string.
     */
    public String toString ()
    {
        return Integer.toString(value());
    }

    /** The history of values. */
    protected int[] _history;

    /** The index where we will next record a value. */
    protected int _index;
}
