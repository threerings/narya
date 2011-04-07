//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.server;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.peer.client.Mapping;
import com.threerings.presents.peer.client.MappingService;
import com.threerings.presents.peer.client.RemoteMapping;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * Defines the server-side of the {@link MappingService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from MappingService.java.")
public interface MappingProvider extends InvocationProvider
{
    /**
     * Handles a {@link MappingService#get} request.
     */
    void get (ClientObject caller, short arg1, Object arg2, InvocationService.ResultListener arg3)
        throws InvocationException;

    /**
     * Handles a {@link MappingService#put} request.
     */
    void put (ClientObject caller, short arg1, Object arg2, Object arg3);

    /**
     * Handles a {@link MappingService#remove} request.
     */
    void remove (ClientObject caller, short arg1, Object arg2);

    /**
     * Handles a {@link MappingService#sync} request.
     */
    void sync (ClientObject caller, short arg1, Object[] arg2, Object[] arg3);

    /**
     * Handles a {@link MappingService#with} request.
     */
    <K, V> void with (ClientObject caller, short arg1, RemoteMapping.Action<Mapping<K, V>> arg2);

    /**
     * Handles a {@link MappingService#withValue} request.
     */
    <K, V> void withValue (ClientObject caller, short arg1, K arg2, RemoteMapping.Action<V> arg3);
}
