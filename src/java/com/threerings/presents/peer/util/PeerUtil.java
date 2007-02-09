//
// $Id: PeerClient.java 4509 2007-01-24 00:22:07Z dhoover $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.util;

import java.util.HashMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.server.InvocationProvider;

/**
 * Static methods of general utility for peer nodes.
 */
public class PeerUtil
{
    /**
     * Creates a proxy object implementing the specified provider interface (a subinterface of
     * {@link InvocationProvider} that forwards requests to the given service implementation
     * (a subinterface of {@link InvocationService} corresponding to the provider interface)
     * on the specified client.  This is useful for server entities that need to call a method
     * either on the current server (with <code>null</code> as the caller parameter) or on a
     * peer server.
     *
     * @param clazz the subclass of {@link InvocationProvider} desired to be implemented
     * @param svc the implementation of the corresponding subclass of {@link InvocationService}
     * @param client the client to pass to the service methods
     */
    public static <S extends InvocationProvider, T extends InvocationService>
        S createProviderProxy (Class<S> clazz, final T svc, final Client client)
    {
        return clazz.cast(Proxy.newProxyInstance(
            clazz.getClassLoader(), new Class[] { clazz },
            new InvocationHandler() {
                public Object invoke (Object proxy, Method method, Object[] args)
                    throws Throwable {
                    Method smethod = _pmethods.get(method);
                    if (smethod == null) {
                        Class[] ptypes = method.getParameterTypes();
                        ptypes[0] = Client.class;
                        _pmethods.put(method, smethod = svc.getClass().getMethod(
                            method.getName(), ptypes));
                    }
                    args[0] = client;
                    return smethod.invoke(svc, args);
                }
            }));
    }

    /** Maps provider interface methods to service interface methods. */
    protected static HashMap<Method, Method> _pmethods = new HashMap<Method, Method>();
}
