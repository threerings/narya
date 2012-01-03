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

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

import static com.threerings.presents.Log.log;

/**
 * Provides the base class via which invocation service requests are dispatched.
 */
public abstract class InvocationDispatcher<T extends InvocationMarshaller<?>>
    implements InvocationManager.Dispatcher
{
    /** The invocation provider for whom we're dispatching. */
    public InvocationProvider provider;

    /**
     * Creates an instance of the appropriate {@link InvocationMarshaller} derived class for use
     * with this dispatcher.
     */
    public abstract T createMarshaller ();

    // from interface InvocationManager.Dispatcher
    public InvocationProvider getProvider () {
        return provider;
    }

    /**
     * Dispatches the specified method to our provider.
     */
    public void dispatchRequest (ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        log.warning("Requested to dispatch unknown method", "provider", provider,
                    "sourceOid", source.getOid(), "methodId", methodId, "args", args);
    }

    /**
     * Performs type casts in a way that works for parameterized types as well as simple types.
     */
    @SuppressWarnings("unchecked")
    protected <K> K cast (Object value)
    {
        return (K)value;
    }
}
