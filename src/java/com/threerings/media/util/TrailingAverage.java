//
// $Id: TrailingAverage.java,v 1.1 2003/04/30 00:38:35 mdb Exp $

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
