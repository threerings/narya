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

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Invoker;
import com.samskivert.util.Lifecycle;

import com.threerings.presents.server.PresentsDObjectMgr.LongRunnable;

import static com.threerings.presents.Log.log;

/**
 * Extends the generic {@link Invoker} and integrates it a bit more into the Presents system.
 */
@Singleton
public class PresentsInvoker extends ReportingInvoker
    implements Lifecycle.ShutdownComponent
{
    @Inject public PresentsInvoker (PresentsDObjectMgr omgr, Lifecycle cycle, ReportManager repmgr)
    {
        super("presents.Invoker", omgr, repmgr);
        cycle.addComponent(this);
        _omgr = omgr;
    }

    /**
     * Adds an invoker that may post to the PresentsInvoker and the DObjectManager and may be
     * posted to by the PresentsInvoker and DObjectManager. This invoker will be taken into
     * consideration by {@link #postRunnableWhenEmpty(Runnable)} and in waiting for invokers to
     * empty out on shutdown.
     */
    public void addInterdependentInvoker (Invoker invoker)
    {
        _interdependentInvokers.add(invoker);
    }

    /**
     * Posts the given runnable to this invoker when it, the DObjectManager and any interdependent
     * invokers are all empty.
     */
    public void postRunnableWhenEmpty (Runnable onEmpty)
    {
        postUnit(new EmptyingUnit(onEmpty));
    }

    @Override // from Thread
    public void start ()
    {
        log.warning(
                "PresentsInvoker should be started with a call to start(PresentsServer server).");
        super.start();
    }

    /**
     * Causes the invoker to begin execution for the PresentsServer.  We pass in the server instead
     * of injecting to prevent potential circular dependency issues.
     */
    public void start (PresentsServer server)
    {
        _server = server;
        super.start();
    }

    @Override // from Invoker, Lifecycle.ShutdownComponent
    public void shutdown ()
    {
        // this will do a sophisticated shutdown of both ourself and the dobjmgr; note: we
        // specifically avoid setting _shutdownRequested as it's OK for units to be posted to the
        // PresentsInvoker during the shutdown phase, we just delay shutdown until we're able to
        // make it to the shutdown unit without queueing up more units for processing
        postRunnableWhenEmpty(new Runnable() {
            public void run () {
                _omgr.harshShutdown(); // end the dobj thread

                // Now that things have emptied out, set _shutdownRequested and commit suicide
                PresentsInvoker.super.shutdown();
            }});
    }

    @Override // from Invoker
    protected void didShutdown ()
    {
        if (_server == null) {
            log.warning("Shutdown without assigned server");
        } else {
            _server.invokerDidShutdown();
        }
    }

    /**
     * This gets posted to this invoker over and over again until it, any interdependent invokers
     * and the DObjectManager are all empty.
     */
    protected class EmptyingUnit extends Unit {
        public EmptyingUnit (Runnable onEmpty) {
            _onEmpty = onEmpty;
        }

        @Override
        public boolean invoke ()
        {
            if (_loopCount > MAX_LOOPS) {
                log.warning("Emptying waiter looped on invoker 10000 times without finishing, "
                    + "running onEmpty while items remain in the queue.");
                _onEmpty.run();
                return false;

            // if the invoker queue is not empty, we put ourselves back on it
            } else if (getPendingUnits() > 0) {
                _loopCount++;
                postUnit(this);
                return false;

            } else if (++_passCount >= MAX_PASSES) {
                log.warning("Emptying waiter passed 50 times without finishing, running onEmpty "
                    + "while items remain in queue.");
                _onEmpty.run();
                return false;

            } else {
                _loopCount = 0;
                // The invoker is empty and running this. Check if everything else is empty.
                List<BlockingUnit> checkers =
                    Lists.newArrayListWithCapacity(_interdependentInvokers.size() + 1);
                for (Invoker invoker : _interdependentInvokers) {
                    checkers.add(new BlockingUnit(invoker));
                }
                checkers.add(new BlockingUnit());
                long checkStart = System.currentTimeMillis();
                while (true) {
                    synchronized (_checkMonitor) {
                        boolean unchecked = false;
                        for (BlockingUnit checker : checkers) {
                            if (!checker.run) {
                                unchecked = true;
                                break;
                            }
                        }

                        if (unchecked) {
                            long timeChecking = System.currentTimeMillis() - checkStart;
                            if (timeChecking >= CHECK_TIMEOUT) {
                                log.warning("Waited 5 minutes for all the blocking units to "
                                    + "no avail.  Running onEmpty while items may remain in "
                                    + "the queue.");
                                releaseCheckers(checkers);
                                _onEmpty.run();
                                return false;
                            }
                            // At least one checker hasn't started running yet.  We need to wait
                            // till they're all in place before looking at emptiness.
                            try {
                                _checkMonitor.wait(CHECK_TIMEOUT - timeChecking);
                            } catch (InterruptedException e) {
                                // Not a problem, we'll just check on the checkers again
                            }

                        } else {
                            // All the checkers are blocking their respective threads.  That means
                            // if their queues are empty, nothing else is running.
                            for (BlockingUnit checker : checkers) {
                                if (!checker.isEmpty()) {
                                    // Balls, we found an invoker with items in its queue.  Let
                                    // everybody get back to work.
                                    releaseCheckers(checkers);
                                    // Post on the busy unit, so when it clears we'll post back
                                    // here
                                    checker.post(new Runnable() {
                                        public void run () {
                                            PresentsInvoker.this.postUnit(EmptyingUnit.this);
                                        }
                                    });
                                    return false;
                                }
                            }
                            // All of the checkers are clean, so they can go back to their business
                            releaseCheckers(checkers);
                            if (getPendingUnits() > 0) {
                                // While we were waiting on the various invokers, one of them gave
                                // us something to do. We need to process that and try again.
                                postUnit(this);
                            } else {
                                // Everything is gloriously clean. Shut down this invoker and the
                                // DObjectManager
                                _onEmpty.run();
                            }
                            return false;
                        }
                    }
                }
            }
        }

        @Override
        public long getLongThreshold ()
        {
            return 60 * 1000;
        }

        protected void releaseCheckers (List<BlockingUnit> checkers)
        {
            for (BlockingUnit checker : checkers) {
                synchronized (checker) {
                    checker.released = true;
                    checker.notify();
                }
            }
        }

        /** The runnable to execute when all associated queues are empty or we've given up. */
        protected Runnable _onEmpty;

        /** The number of times we've been passed to the object manager. */
        protected int _passCount;

        /** How many times we've looped on the thread we're currently on. */
        protected int _loopCount;

        /** The maximum number of passes we allow before just ending things. */
        protected static final int MAX_PASSES = 50;

        /** The maximum number of loops we allow before just ending things. */
        protected static final int MAX_LOOPS = 10000;

        protected static final long CHECK_TIMEOUT = 5 * 60 * 1000L;
    }

    /**
     * Runs in an Invoker or the DObjectManager and blocks it until released by EmptyingUnit.
     */
    protected class BlockingUnit extends Unit implements LongRunnable {
        /**
         * If the run method of this checker has been entered. If this is true while released is
         * false, this checker is blocking its thread.
         */
        public boolean run;

        /** If this checker no longer needs to block its thread. */
        public boolean released;

        public BlockingUnit ()
        {
            _omgr.postRunnable(this);
        }

        public BlockingUnit (Invoker invoker)
        {
            _invoker = invoker;
            _invoker.postUnit(this);
        }

        public boolean isEmpty ()
        {
            return _invoker == null ? _omgr.queueIsEmpty() : (_invoker.getPendingUnits() == 0);
        }

        public void post (Runnable runnable)
        {
            if (_invoker != null) {
                _invoker.postRunnable(runnable);
            } else {
                _omgr.postRunnable(runnable);
            }
        }

        @Override
        public void run()
        {
            // Override Invoker.Unit's run altogether to allow this to be posted as a Runnable to
            // the DObjectMgr as well.
            run = true;
            synchronized (_checkMonitor) {
                _checkMonitor.notify();
            }
            synchronized (this) {
                while (!released) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // Not a problem, we'll check our invariant
                    }
                }
            }
        }

        @Override
        public boolean invoke ()
        {
            run();
            return false;
        }

        @Override
        public long getLongThreshold ()
        {
            // Don't bitch about being a long invoker unless this has been blocking for longer than
            // the EmptyingUnit timeout
            return EmptyingUnit.CHECK_TIMEOUT;
        }

        protected Invoker _invoker;
    }

    /**
     * Synchronizes between EmptyingUnit and its BlockingUnits when checking that all
     * interdependent invokers are blocked.
     */
    protected Object _checkMonitor = new Object();

    /** Invokers that may post to Presents and may be posted to by Presents. */
    protected List<Invoker> _interdependentInvokers = Lists.newArrayList();

    /** The distributed object manager with which we interoperate. */
    protected PresentsDObjectMgr _omgr;

    /** The server we're working for. */
    protected PresentsServer _server;
}
