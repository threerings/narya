//
// $Id: DirectionTest.java,v 1.3 2004/08/27 02:21:05 mdb Exp $
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

//         for (double theta = -Math.PI; theta <= Math.PI; theta += Math.PI/1000) {
//             orient = DirectionUtil.getFineDirection(theta);
//             System.out.println(Math.toDegrees(theta) + " => " +
//                                DirectionUtil.toShortString(orient));
//         }
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
