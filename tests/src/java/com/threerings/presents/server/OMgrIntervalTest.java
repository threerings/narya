//
// $Id$

package com.threerings.presents.server;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

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
