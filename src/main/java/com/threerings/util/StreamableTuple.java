//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
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
