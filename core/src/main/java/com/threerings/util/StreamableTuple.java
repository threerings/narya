//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import com.samskivert.util.Tuple;

import com.threerings.io.Streamable;

/**
 * A {@link Tuple} extension that can be streamed. The contents of the tuple
 * must also be of streamable types.
 *
 * @see Streamable
 * @param <L> the type of the left-hand side of this tuple.
 * @param <R> the type of the right-hand side of this tuple.
 */
public class StreamableTuple<L, R> extends Tuple<L, R>
    implements Streamable
{
    /**
     * Creates a tuple with the specified two objects.
     */
    public static <L, R> StreamableTuple<L, R> newTuple (L left, R right)
    {
        return new StreamableTuple<L, R>(left, right);
    }

    /**
     * Constructs a tuple with the two specified objects.
     */
    public StreamableTuple (L left, R right)
    {
        super(left, right);
    }
}
