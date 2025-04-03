//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import com.google.common.collect.Maps;

import com.samskivert.util.ArrayUtil;

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
    public static <S extends InvocationProvider, T extends InvocationService<?>>
        S createProviderProxy (Class<S> clazz, final T svc, final Client client)
    {
        return clazz.cast(Proxy.newProxyInstance(
            clazz.getClassLoader(), new Class<?>[] { clazz },
            new InvocationHandler() {
                public Object invoke (Object proxy, Method method, Object[] args)
                    throws Throwable {
                    Method smethod = _pmethods.get(method);
                    if (smethod == null) {
                        Class<?>[] ptypes = method.getParameterTypes();
                        _pmethods.put(method, smethod = svc.getClass().getMethod(
                            method.getName(), ArrayUtil.splice(ptypes, 0, 1)));
                    }
                    return smethod.invoke(svc, ArrayUtil.splice(args, 0, 1));
                }
            }));
    }

    /** Maps provider interface methods to service interface methods. */
    protected static HashMap<Method, Method> _pmethods = Maps.newHashMap();
}
