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

package com.threerings.web.server;

import java.util.concurrent.Callable;

import com.samskivert.servlet.util.ServiceWaiter;
import com.samskivert.util.RunQueue;

import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.server.InvocationException;
import com.threerings.web.gwt.ServiceException;

import static com.threerings.admin.Log.log;

/**
 * Used to bridge the gap between synchronous servlets and our asynchronous
 * game server architecture.
 *
 * <p> We don't want to use service waiters as a permanent solution, as that
 * will result in a zillion servlet threads hanging around waiting for results
 * all over the goddamned place. Instead we want to rearchitect Jetty to
 * support asynchronous servlets that give up their thread when they need to
 * block and grab a new thread out of the pool when they're ready to go
 * again. This is a tall order, but I think they might be working on something
 * like this for Jetty 6.0.
 */
public class ServletWaiter<T> extends ServiceWaiter<T>
{
    public ServletWaiter (String ident)
    {
        _ident = ident;
    }

    /**
     * Waits for our asynchronous result and returns it if all is well. If
     * anything goes wrong (a timeout or a asynchronous call failure) the
     * exception is logged and a {@link ServiceException} is thrown.
     */
    public T waitForResult ()
        throws ServiceException
    {
        try {
            if (waitForResponse()) {
                return getArgument();
            } else {
                throw getError();
            }

        } catch (InvocationException ie) {
            // pass these through without a fuss
            throw new ServiceException(ie.getMessage());

        } catch (Exception e) {
            log.warning(_ident + " failed.", e);
            throw new ServiceException(InvocationCodes.E_INTERNAL_ERROR);
        }
    }

    /**
     * Posts a result getter to be executed on the run queue thread and blocks waiting for the
     * result.
     */
    public static <T> T queueAndWait (
        RunQueue runq, final String name, final Callable<T> action)
        throws ServiceException
    {
        final ServletWaiter<T> waiter = new ServletWaiter<T>(name);
        runq.postRunnable(new Runnable() {
            public void run () {
                try {
                    waiter.postSuccess(action.call());
                } catch (final Exception e) {
                    waiter.postFailure(e);
                }
            }
        });
        return waiter.waitForResult();
    }

    protected String _ident;
}
