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

package com.threerings.nio;

import static com.threerings.NaryaLog.log;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.google.common.collect.Iterators;

/**
 * Exposes selected keys from {@link Selector#selectedKeys()} as an Iterable, removing them from the
 * selected set as they're iterated over.
 */
public class SelectorIterable
    implements Iterable<SelectionKey>
{
    /** The maximum allowed consecutive select() failures, after which we will declare ourselves
     * irreparably hosed. */
    public static final int MAX_SELECT_FAILURES = 20;

    /**
     * An interface for hearing about catastrophic selection failure. If select fails (in a
     * non-expected way) more than {@link #MAX_SELECT_FAILURES} times in a row, this interface will
     * be notified. This allows a server to reboot itself in an orderly manner, rather than
     * continue in an inoperable state.
     */
    public interface SelectFailureHandler {
        /** Called to report a select() failure. */
        void handleSelectFailure (Exception e);
    }

    /**
     * Creates an iterable for the given selector's selectedKeys.
     * @param selectLoopTime - the amount of time to wait in select, or 0 to skip the wait at all.
     * @param handler - a callback for the Selector going awol.
     */
    public SelectorIterable (Selector selector, int selectLoopTime, SelectFailureHandler handler)
    {
        _selector = selector;
        _selectLoopTime = selectLoopTime;
        _failureHandler = handler;
    }

    // from interface Iterable<SelectionKey>
    public Iterator<SelectionKey> iterator ()
    {
        return Iterators.consumingIterator(select().iterator());
    }

    protected Set<SelectionKey> select ()
    {
        try {
            // log.debug("Selecting from " + _selector.keys() + " (" + _selectLoopTime + ").");

            // check for incoming network events
            int eventCount =
                _selectLoopTime == 0 ?  _selector.selectNow() : _selector.select(_selectLoopTime);
            Set<SelectionKey> ready = _selector.selectedKeys();
            if (eventCount == 0 && ready.size() != 0) {
                log.warning("select() returned no selected sockets, but there are "
                    + ready.size() + " in the ready set.");
            }
            // clear the runtime error count
            _runtimeExceptionCount = 0;
            return ready;

        } catch (IOException ioe) {
            log.warning("Failure select()ing", "ioe", ioe);

        } catch (RuntimeException re) {
            // this block of code deals with a bug in the _selector that we observed on 2005-05-02,
            // instead of looping indefinitely after things go pear-shaped, shut us down in an
            // orderly fashion
            log.warning("Failure select()ing.", re);
            if (_runtimeExceptionCount++ >= MAX_SELECT_FAILURES) {
                _failureHandler.handleSelectFailure(re);
            }
        }
        return Collections.emptySet();
    }

    protected int _runtimeExceptionCount;
    protected final int _selectLoopTime;
    protected final Selector _selector;
    protected final SelectFailureHandler _failureHandler;
}
