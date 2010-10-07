//
// $Id: ServletWaiter.java 13028 2008-10-31 21:52:05Z jamie $

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
