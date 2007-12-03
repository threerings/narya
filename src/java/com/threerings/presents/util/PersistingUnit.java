//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.util;

import java.util.logging.Level;

import com.samskivert.util.Invoker;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.server.InvocationException;

import static com.threerings.presents.Log.log;

/**
 * Simplifies a common pattern which is to post an {@link Invoker} unit which does some database
 * operation and then calls back to an {@link InvocationService.InvocationListener} of some
 * sort. If the database operation fails, the error will be logged and the result listener will be
 * replied to with {@link InvocationCodes#INTERNAL_ERROR}.
 */
public abstract class PersistingUnit extends Invoker.Unit
{
    public PersistingUnit (InvocationService.InvocationListener listener)
    {
        this("UnknownPersistingUnit", listener);
    }

    public PersistingUnit (String name, InvocationService.InvocationListener listener)
    {
        super(name);
        _listener = listener;
    }

    /**
     * This method is where the unit performs its persistent actions. Any persistence exception
     * will be caught and logged along with the output from {@link #getFailureMessage}, if any.
     */
    public abstract void invokePersistent ()
        throws Exception;

    /**
     * Handles the success case, which by default is to do nothing.
     */
    public void handleSuccess ()
    {
    }

    /**
     * Handles the failure case by logging the error and reporting an internal error to the
     * listener.
     */
    public void handleFailure (Exception error)
    {
        if (error instanceof InvocationException) {
            _listener.requestFailed(error.getMessage());
        } else {
            log.log(Level.WARNING, getFailureMessage(), error);
            _listener.requestFailed(InvocationCodes.INTERNAL_ERROR);
        }
    }

    @Override // from Invoker.Unit
    public boolean invoke ()
    {
        try {
            invokePersistent();
        } catch (Exception pe) {
            _error = pe;
        }
        return true;
    }

    @Override // from Invoker.Unit
    public void handleResult ()
    {
        if (_error != null) {
            handleFailure(_error);
        } else {
            handleSuccess();
        }
    }

    /**
     * If the listener is known to be a ConfirmListener, this will cast it and report that the
     * request was processed.
     */
    protected void reportRequestProcessed ()
    {
        ((InvocationService.ConfirmListener)_listener).requestProcessed();
    }

    /**
     * If the listener is known to be a ResultListener, this will cast it and report that the
     * request was processed.
     */
    protected void reportRequestProcessed (Object result )
    {
        ((InvocationService.ResultListener)_listener).requestProcessed(result);
    }

    /**
     * Provides a custom failure message in the event that the persistent action fails. This will
     * be logged along with the exception.
     */
    protected String getFailureMessage ()
    {
        return this + " failed.";
    }

    protected InvocationService.InvocationListener _listener;
    protected Exception _error;
}
