//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
