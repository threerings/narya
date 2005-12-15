//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

import com.samskivert.io.PersistenceException;
import com.samskivert.util.Invoker;

import com.threerings.presents.Log;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationCodes;

/**
 * Simplifies a common pattern which is to post an {@link Invoker} unit which
 * does some database operation and then calls back to an {@link
 * InvocationService.InvocationListener} of some sort. If the database operation
 * fails, the error will be logged and the result listener will be replied to
 * with {@link InvocationCodes#INTERNAL_ERROR}.
 */
public abstract class PersistingUnit extends Invoker.Unit
{
    public PersistingUnit (InvocationService.InvocationListener listener)
    {
        _listener = listener;
    }

    /**
     * This method is where the unit performs its persistent actions. Any
     * persistence exception will be caught and logged along with the output
     * from {@link #getFailureMessage}, if any.
     */
    public abstract void invokePersistent ()
        throws PersistenceException;

    /**
     * Handles the success case, which by default is to do nothing.
     */
    public void handleSuccess ()
    {
    }

    /**
     * Handles the failure case by logging the error and reporting an internal
     * error to the listener.
     */
    public void handleFailure (PersistenceException error)
    {
        Log.warning(getFailureMessage());
        Log.logStackTrace(error);
        _listener.requestFailed(InvocationCodes.INTERNAL_ERROR);
    }

    /**
     * Provides a custom failure message in the event that the persistent
     * action fails. This will be logged along with the exception.
     */
    public String getFailureMessage ()
    {
        return this + " failed.";
    }

    public boolean invoke ()
    {
        try {
            invokePersistent();
        } catch (PersistenceException pe) {
            _error = pe;
        }
        return true;
    }

    public void handleResult ()
    {
        if (_error != null) {
            handleFailure(_error);
        } else {
            handleSuccess();
        }
    }

    protected InvocationService.InvocationListener _listener;
    protected PersistenceException _error;
}
