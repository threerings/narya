package com.threerings.presents.server;

/**
 * Test class to exercise the code in {@link VariableThrottle}.
 */
public class TestVariableThrottle
{
    /**
     * Exercise the code in {@link VariableThrottle}.
     */
    public static void main (String[] args)
    {
        testUpdate(4);
        testUpdate(5);
        testUpdate(6);
        testUpdate(7);
        testUpdate(8);
    }
    
    /**
     * Calls the {@link VariableThrottle#updateOpLimit} function a few times, interspersed with
     * calls to add operations. Prints the operations contained in the throttle after each change.
     */
    public static void testUpdate (int opCount)
    {
        // set up a throttle for 5 ops per millisecond
        TestThrottle throttle = new TestThrottle(5, 1);
        System.out.println("Testing updates with " + opCount + " operations");
        long time = 0;
        for (int ii = 0; ii < opCount; ++ii) {
            throttle.throttleOp(time += 1);
        }
        System.out.println("    " + throttle.opsToString());
        throttle.updateOpLimit(10);
        for (int ii = 0; ii < opCount; ++ii) {
            throttle.throttleOp(time += 1);
        }
        System.out.println("    " + throttle.opsToString());
        throttle.updateOpLimit(5);
        System.out.println("    " + throttle.opsToString());
        for (int ii = 0; ii < opCount; ++ii) {
            throttle.throttleOp(time += 1);
        }
        System.out.println("    " + throttle.opsToString());
        throttle.updateOpLimit(10);
        System.out.println("    " + throttle.opsToString());
    }
    
    /**
     * Constructs a string representation of all operation time stamps, starting from the oldest.
     */
    public static class TestThrottle extends VariableThrottle
    {
        public TestThrottle (int maxOps, long period)
        {
            super(maxOps, period);
        }
        
        public String opsToString ()
        {
            String hist = String.valueOf(_ops[_lastOp]);
            for (int ii = 1; ii < _ops.length; ++ii) {
                long tn = _ops[(_lastOp + ii) % _ops.length];
                hist += ", " + String.valueOf(tn);
            }
            return hist;
        }
    }
}
