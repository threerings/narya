//
// $Id$
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

package com.threerings.presents.server;

import java.util.Iterator;

import com.samskivert.util.Histogram;
import com.samskivert.util.Invoker;
import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;

/**
 * Extends the generic {@link Invoker} and integrates it a bit more into
 * the Presents system.
 */
public class PresentsInvoker extends Invoker
    implements PresentsServer.Reporter
{
    /**
     * Creates an invoker that will post results to the supplied
     * distributed object manager.
     */
    public PresentsInvoker (PresentsDObjectMgr omgr)
    {
        super("presents.Invoker", omgr);
        _omgr = omgr;
        if (PERF_TRACK) {
            PresentsServer.registerReporter(this);
        }
    }

    /**
     * Will do a sophisticated shutdown of both itself and the DObjectManager
     * thread.
     */
    public void shutdown ()
    {
        _queue.append(new ShutdownUnit());
    }

    // documentation inherited from interface
    public void appendReport (StringBuffer buffer, long now, long sinceLast)
    {
        buffer.append("* presents.util.Invoker:\n");
        buffer.append("- Units executed: ").append(_unitsRun).append("\n");
        _unitsRun = 0;
        for (Iterator iter = _tracker.keySet().iterator(); iter.hasNext(); ) {
            Class key = (Class)iter.next();
            Histogram histo = (Histogram)_tracker.get(key);
            buffer.append("  ");
            buffer.append(StringUtil.shortClassName(key)).append(": ");
            buffer.append(histo.summarize()).append("\n");
            histo.clear();
        }
    }

    /**
     * This unit gets posted back and forth between the invoker and DObjectMgr
     * until both of their queues are empty and they can both be safely
     * shutdown.
     */
    protected class ShutdownUnit extends Unit
    {
        // run on the invoker thread
        public boolean invoke ()
        {
            if (checkLoops()) {
                return false;

            // if the invoker queue is not empty, we put ourselves back on it
            } else if (_queue.hasElements()) {
                _loopCount++;
                postUnit(this);
                return false;

            } else {
                // the invoker is empty, let's go over to the omgr
                _loopCount = 0;
                _passCount++;
                return true;
            }
        }

        // run on the dobj thread
        public void handleResult ()
        {
            if (checkLoops()) {
                return;

            // if the queue isn't empty, re-post
            } else if (!_omgr.queueIsEmpty()) {
                _loopCount++;
                _omgr.postRunnable(this);

            // if the invoker still has stuff and we're still under the pass
            // limit, go ahead and pass it back to the invoker
            } else if (_queue.hasElements() && (_passCount < MAX_PASSES)) {
                // pass the buck back to the invoker
                _loopCount = 0;
                postUnit(this);

            } else {
                // otherwise end it, and complain if we're ending it
                // because of passes
                if (_passCount >= MAX_PASSES) {
                    Log.warning("Shutdown Unit passed 50 times without " +
                        "finishing, shutting down harshly.");
                }
                doShutdown();
            }
        }

        /**
         * Check to make sure we haven't looped too many times.
         */
        protected boolean checkLoops ()
        {
            if (_loopCount > MAX_LOOPS) {
                Log.warning("Shutdown Unit looped on one thread 10000 times " +
                    "without finishing, shutting down harshly.");
                doShutdown();
                return true;
            }

            return false;
        }

        /**
         * Do the actual shutdown.
         */
        protected void doShutdown ()
        {
            _omgr.harshShutdown(); // end the dobj thread

            // end the invoker thread
            postUnit(new Unit() {
                public boolean invoke () {
                    _running = false;
                    return false;
                }
            });
        }

        /** The number of times we've been passed to the object manager. */
        protected int _passCount = 0;

        /** How many times we've looped on the thread we're currently on. */
        protected int _loopCount = 0;

        /** The maximum number of passes we allow before just ending things. */
        protected static final int MAX_PASSES = 50;

        /** The maximum number of loops we allow before just ending things. */
        protected static final int MAX_LOOPS = 10000;
    }

    protected PresentsDObjectMgr _omgr;
}
