//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.samskivert.util.RunQueue;

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.net.UsernamePasswordCreds;

import static com.threerings.NaryaLog.log;
import static org.junit.Assert.*;

/**
 * Utilities for Presents server tests.
 */
public class ServerTestUtil
{
    /**
     * Creates a test client, with a runqueue that will operate on a separate (daemon) thread and
     * which will be already started.
     */
    public static Client createClient (String username)
    {
        return new Client(new UsernamePasswordCreds(new Name(username), "test"),
                          new ClientRunQueue());
    }

    /**
     * Waits the specified number of seconds for the supplied latch to count down to zero.
     */
    public static void await (CountDownLatch latch, int seconds)
    {
        try {
            if (!latch.await(seconds, TimeUnit.SECONDS)) {
                fail("Timed out waiting for latch");
            }
        } catch (InterruptedException ie) {
            fail("Interrupted waiting for latch");
        }
    }

    protected static class ClientRunQueue extends Thread
        implements RunQueue
    {
        public ClientRunQueue () {
            super("Client run queue");
            setDaemon(true);
            start();
        }

        public void postRunnable (Runnable r) {
            _queue.offer(r);
        }

        public boolean isDispatchThread () {
            return Thread.currentThread() == this;
        }

        public boolean isRunning () {
            return true;
        }

        @Override public void run () {
            while (true) {
                Runnable r = null;
                try {
                    r = _queue.take();
                    r.run();
                } catch (Throwable t) {
                    log.warning("Client runnable failed", "run", r, t);
                }
            }
        }

        protected BlockingQueue<Runnable> _queue = new LinkedBlockingQueue<Runnable>();
    }
}
