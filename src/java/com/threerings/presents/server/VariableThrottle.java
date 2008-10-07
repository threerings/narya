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

package com.threerings.presents.server;

import com.samskivert.util.Throttle;

/**
 * Throttle subclass that allows changing the operation limit.
 */
public class VariableThrottle extends Throttle
{
    /**
     * Creates a new updateable throttle.
     */
    public VariableThrottle (int operations, long period)
    {
        super(operations, period);
    }
    
    /**
     * Updates the number of operations for this throttle to a new maximum, retaining the current
     * history of operations if the limit is being increased and truncating the oldest operations
     * if the limit is decreased.
     * @param operations the new maximum number of operations
     */
    public void updateOpLimit (int operations)
    {
        long[] ops = new long[operations];
        if (operations > _ops.length) {
            // Copy to a larger buffer, leaving zeroes at the beginning
            int lastOp = _lastOp + operations - _ops.length;
            System.arraycopy(_ops, 0, ops, 0, _lastOp);
            System.arraycopy(_ops, _lastOp, ops, lastOp, _ops.length - _lastOp);

        } else if (operations < _ops.length) {
            // Copy to a smaller buffer, truncating older operations
            int lastOp = (_lastOp + _ops.length - operations) % _ops.length;
            int endCount = Math.min(_ops.length - lastOp, operations);
            System.arraycopy(_ops, lastOp, ops, 0, endCount);
            System.arraycopy(_ops, 0, ops, endCount, operations - endCount);
            _lastOp = 0;
        }
        _ops = ops;
    }
    
    public String toString ()
    {
        return "VariableThrottle [" + _ops.length + " per " + _period + "ms]";
    }
}
