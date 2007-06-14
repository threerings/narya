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

import com.samskivert.util.Tuple;

import com.threerings.io.Streamable;

/**
 * A {@link Tuple} extension that can be streamed. The contents of the tuple
 * must also be of streamable types.
 *
 * @see Streamable
 */
public class StreamableTuple<L, R> extends Tuple<L, R>
    implements Streamable
{
    /**
     * Constructs a blank tuple.
     */
    public StreamableTuple ()
    {
    }

    /**
     * Constructs a tuple with the two specified objects.
     */
    public StreamableTuple (L left, R right)
    {
        super(left, right);
    }
}
