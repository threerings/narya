//
// $Id: DirectionTest.java,v 1.1 2002/06/26 02:54:56 mdb Exp $

package com.threerings.util;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests the {@link Direction} class.
 */
public class DirectionTest extends TestCase
    implements DirectionCodes
{
    public DirectionTest ()
    {
        super(DirectionTest.class.getName());
    }

    public void runTest ()
    {
        int orient = NORTH;

        for (int i = 0; i < FINE_DIRECTION_COUNT; i++) {
//             System.out.print(DirectionUtil.toShortString(orient) + " -> ");
            orient = DirectionUtil.rotateCW(orient, 1);
        }
//         System.out.println(DirectionUtil.toShortString(orient));
        assertTrue("CW rotate", orient == NORTH);

        for (int i = 0; i < FINE_DIRECTION_COUNT; i++) {
//             System.out.print(DirectionUtil.toShortString(orient) + " -> ");
            orient = DirectionUtil.rotateCCW(orient, 1);
        }
//         System.out.println(DirectionUtil.toShortString(orient));
        assertTrue("CCW rotate", orient == NORTH);
    }

    public static Test suite ()
    {
        return new DirectionTest();
    }

    public static void main (String[] args)
    {
        DirectionTest test = new DirectionTest();
        test.runTest();
    }
}
