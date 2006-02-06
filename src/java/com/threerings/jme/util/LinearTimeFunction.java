//
// $Id: LinearTimeFunction.java 3122 2004-09-18 22:57:08Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.jme.util;

/**
 * Varies a value linearly with time.
 */
public class LinearTimeFunction extends TimeFunction
{
    public LinearTimeFunction (float start, float end, float duration)
    {
        super(start, end, duration);
        _range = (end - start);
    }

    // documentation inherited
    protected float computeValue ()
    {
        return (_elapsed * _range / _duration) + _start;
    }

    protected float _range;
}
