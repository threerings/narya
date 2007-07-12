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

import com.samskivert.util.ResultListener;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.server.InvocationException;

/**
 * Adapts the response from a {@link InvocationService.ResultListener} to a {@link ResultListener}.
 * In the event of failure, the failure string is wrapped in an {@link InvocationException}.
 */
public class InvocationAdapter implements InvocationService.ResultListener
{
    public InvocationAdapter (ResultListener<Object> target)
    {
        _target = target;
    }

    // from InvocationService.ResultListener
    public void requestProcessed (Object result)
    {
        _target.requestCompleted(result);
    }

    // from InvocationService.ResultListener
    public void requestFailed (String cause)
    {
        _target.requestFailed(new InvocationException(cause));
    }

    protected ResultListener<Object> _target;
}
