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

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

/**
 * Provides the base class via which invocation service requests are
 * dispatched.
 */
public abstract class InvocationDispatcher
{
    /** The invocation provider for whom we're dispatching. */
    public InvocationProvider provider;

    /**
     * Creates an instance of the appropriate {@link InvocationMarshaller}
     * derived class for use with this dispatcher.
     */
    public abstract InvocationMarshaller createMarshaller ();

    /**
     * Dispatches the specified method to our provider.
     */
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        Log.warning("Requested to dispatch unknown method " +
                    "[provider=" + provider +
                    ", sourceOid=" + source.getOid() +
                    ", methodId=" + methodId +
                    ", args=" + StringUtil.toString(args) + "].");
    }
}
