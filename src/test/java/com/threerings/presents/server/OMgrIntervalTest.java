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

package com.threerings.presents.server;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests our handling of auto-canceling intervals created by the omgr if they expire after it has
 * shutdown.
 */
public class OMgrIntervalTest extends PresentsTestBase
{
    @Test public void testIntervals ()
    {
        final PresentsDObjectMgr omgr = getInstance(PresentsDObjectMgr.class);

        omgr.newInterval(new Runnable () {
            public void run () {
                if (++_count > 1) {
                    omgr.harshShutdown();
                }
            }
        }).schedule(100, true);

        omgr.run();

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        assertTrue(_count == 2);
    }

    protected int _count;
}
