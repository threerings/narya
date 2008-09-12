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
}
