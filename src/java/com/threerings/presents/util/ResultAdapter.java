//
// $Id: ResultAdapter.java,v 1.2 2004/08/27 02:20:26 mdb Exp $
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

package com.threerings.presents.util;

import com.samskivert.util.ResultListener;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.server.InvocationException;

/**
 * Adapts the response from a {@link ResultListener} to an {@link
 * InvocationService.ResultListener} if the failure is an instance fo
 * {@link InvocationException} the message will be passed on to the result
 * listener, otherwise they will be provided with {@link
 * InvocationCodes#INTERNAL_ERROR}.
 */
public class ResultAdapter implements ResultListener
{
    /**
     * Creates an adapter with the supplied listener.
     */
    public ResultAdapter (InvocationService.ResultListener listener)
    {
        _listener = listener;
    }

    // documentation inherited from interface
    public void requestCompleted (Object result)
    {
        _listener.requestProcessed(result);
    }

    // documentation inherited from interface
    public void requestFailed (Exception cause)
    {
        if (cause instanceof InvocationException) {
            _listener.requestFailed(cause.getMessage());
        } else {
            _listener.requestFailed(InvocationCodes.INTERNAL_ERROR);
        }
    }

    protected InvocationService.ResultListener _listener;
}
