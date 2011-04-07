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

package com.threerings.presents.peer.data;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.peer.client.Mapping;
import com.threerings.presents.peer.client.MappingService;
import com.threerings.presents.peer.client.RemoteMapping;

/**
 * Provides the implementation of the {@link MappingService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from MappingService.java.")
public class MappingMarshaller extends InvocationMarshaller
    implements MappingService
{
    /** The method id used to dispatch {@link #get} requests. */
    public static final int GET = 1;

    // from interface MappingService
    public void get (short arg1, Object arg2, InvocationService.ResultListener arg3)
    {
        InvocationMarshaller.ResultMarshaller listener3 = new InvocationMarshaller.ResultMarshaller();
        listener3.listener = arg3;
        sendRequest(GET, new Object[] {
            Short.valueOf(arg1), arg2, listener3
        });
    }

    /** The method id used to dispatch {@link #put} requests. */
    public static final int PUT = 2;

    // from interface MappingService
    public void put (short arg1, Object arg2, Object arg3)
    {
        sendRequest(PUT, new Object[] {
            Short.valueOf(arg1), arg2, arg3
        });
    }

    /** The method id used to dispatch {@link #remove} requests. */
    public static final int REMOVE = 3;

    // from interface MappingService
    public void remove (short arg1, Object arg2)
    {
        sendRequest(REMOVE, new Object[] {
            Short.valueOf(arg1), arg2
        });
    }

    /** The method id used to dispatch {@link #sync} requests. */
    public static final int SYNC = 4;

    // from interface MappingService
    public void sync (short arg1, Object[] arg2, Object[] arg3)
    {
        sendRequest(SYNC, new Object[] {
            Short.valueOf(arg1), arg2, arg3
        });
    }

    /** The method id used to dispatch {@link #with} requests. */
    public static final int WITH = 5;

    // from interface MappingService
    public <K, V> void with (short arg1, RemoteMapping.Action<Mapping<K, V>> arg2)
    {
        sendRequest(WITH, new Object[] {
            Short.valueOf(arg1), arg2
        });
    }

    /** The method id used to dispatch {@link #withValue} requests. */
    public static final int WITH_VALUE = 6;

    // from interface MappingService
    public <K, V> void withValue (short arg1, K arg2, RemoteMapping.Action<V> arg3)
    {
        sendRequest(WITH_VALUE, new Object[] {
            Short.valueOf(arg1), arg2, arg3
        });
    }
}
