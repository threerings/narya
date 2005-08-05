//
// $Id$

package com.threerings.openal;

import java.io.IOException;

import com.samskivert.util.Interval;
import com.samskivert.util.Queue;
import com.samskivert.util.RunQueue;

/**
 * Tests the OpenAL sound system.
 */
public class TestSoundManager
{
    public static void main (String[] args)
    {
        if (args.length == 0) {
            System.err.println("Usage: TestSoundManager sound.wav");
            System.exit(-1);
        }

        final Thread current = Thread.currentThread();
        RunQueue rqueue = new RunQueue() {
            public void postRunnable (Runnable r) {
                _queue.append(r);
            }
            public boolean isDispatchThread () {
                return (current == Thread.currentThread());
            }
        };

        SoundManager smgr = SoundManager.createSoundManager(rqueue);
        ClipProvider provider = new WaveDataClipProvider();
        final SoundGroup group = smgr.createGroup(provider, 5);
        final String path = args[0];

        // queue up an interval to play a sound over and over
        Interval i = new Interval(rqueue) {
            public void expired () {
                Sound sound = group.getSound(path);
                sound.play(true);
            }
        };
        i.schedule(100L, true);

        while (true) {
            Runnable r = (Runnable)_queue.get();
            r.run();
        }
    }

    protected static Queue _queue = new Queue();
}
