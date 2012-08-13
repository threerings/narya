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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;
import com.samskivert.util.LRUHashMap;
import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;

import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller.ListenerMarshaller;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.EventListener;
import com.threerings.presents.dobj.InvocationRequestEvent;
import com.threerings.presents.net.Transport;

import static com.threerings.presents.Log.log;

/**
 * The invocation services provide client to server invocations (service requests) and server to
 * client invocations (responses and notifications). Via this mechanism, the client can make
 * requests of the server, be notified of its response and the server can asynchronously invoke
 * code on the client.
 *
 * <p> Invocations are like remote procedure calls in that they are named and take arguments. All
 * arguments must be {@link Streamable} objects, primitive types, or String objects. All arguments
 * are passed by value (by serializing and unserializing the arguments); there is no special
 * facility provided for referencing non-local objects (it is assumed that the distributed object
 * facility will already be in use for any objects that should be shared).
 *
 * <p> The server invocation manager listens for invocation requests from the client and passes
 * them on to the invocation provider registered for the requested invocation module. It also
 * provides a mechanism by which responses and asynchronous notification invocations can be
 * delivered to the client.
 */
@Singleton
public class InvocationManager
    implements EventListener
{
    /**
     * Constructs an invocation manager which will use the supplied distributed object manager to
     * operate its invocation services. Generally only one invocation manager should be operational
     * in a particular system.
     */
    @Inject public InvocationManager (PresentsDObjectMgr omgr)
    {
        _omgr = omgr;
        _omgr._invmgr = this;

        // create the object on which we'll listen for invocation requests
        DObject invobj = _omgr.registerObject(new DObject());
        invobj.addListener(this);
        _invoid = invobj.getOid();

        log.debug("Created invocation service object", "oid", _invoid);
    }

    /**
     * Returns the object id of the invocation services object.
     */
    public int getOid ()
    {
        return _invoid;
    }

    /**
     * Registers the supplied invocation service provider.
     *
     * @param provider the provider to be registered.
     * @param mclass the class of the invocation marshaller generated for the service.
     */
    public <T extends InvocationMarshaller<?>> T registerProvider (
        InvocationProvider provider, Class<T> mclass)
    {
        return registerProvider(provider, mclass, null);
    }

    /**
     * Registers the supplied invocation service provider.
     *
     * @param provider the provider to be registered.
     * @param mclass the class of the invocation marshaller generated for the service.
     * @param group the bootstrap group in which this marshaller is to be registered, or null if it
     * is not a bootstrap service. <em>Do not:</em> register a marshaller with multiple boot
     * groups. You must collect shared marshaller into as fine grained a set of groups as necessary
     * and have different types of clients specify the list of groups they need.
     */
    public <T extends InvocationMarshaller<?>> T registerProvider (
        final InvocationProvider provider, Class<T> mclass, String group)
    {
        _omgr.requireEventThread(); // sanity check

        // find the invocation provider interface class (defaulting to the concrete class to cope
        // with legacy non-interface based providers)
        Class<?> pclass = provider.getClass();
        String pname = mclass.getSimpleName().replaceAll("Marshaller", "Provider");
      OUTER:
        for (Class<?> sclass = pclass; sclass != null; sclass = sclass.getSuperclass()) {
            for (Class<?> iclass : sclass.getInterfaces()) {
                if (InvocationProvider.class.isAssignableFrom(iclass) &&
                    iclass.getSimpleName().equals(pname)) {
                    pclass = iclass;
                    break OUTER;
                }
            }
        }

        // determine the invocation service code mappings
        final Map<Integer,Method> invmeths = Maps.newHashMap();
        for (Method method : pclass.getMethods()) {
            Class<?>[] ptypes = method.getParameterTypes();
            // only consider methods whose first argument is of type ClientObject; this is a
            // non-issue if we are looking at an auto-generated FooProvider interface, but is
            // necessary to avoid problems for legacy concrete FooProvider implementations that
            // also happen to have overloaded methods with the same name as invocation service
            // methods; I'm looking at you ChatProvider...
            if (ptypes.length == 0 || !ClientObject.class.isAssignableFrom(ptypes[0])) {
                continue;
            }
            try {
                Field code = mclass.getField(StringUtil.unStudlyName(method.getName()));
                invmeths.put(code.getInt(null), method);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae); // Field.get failed? shouldn't happen
            } catch (NoSuchFieldException nsfe) {
                // not a problem, they just added some extra methods to their provider
            }
        }

        // get the next invocation code
        int invCode = nextInvCode();

        // create a marshaller instance and initialize it
        T marsh;
        try {
            marsh = mclass.newInstance();
            marsh.init(_invoid, invCode, _standaloneClient == null ?
                null : _standaloneClient.getInvocationDirector());
        } catch (IllegalAccessException ie) {
            throw new RuntimeException(ie);
        } catch (InstantiationException ie) {
            throw new RuntimeException(ie);
        }

        // register the dispatcher
        _dispatchers.put(invCode, new Dispatcher() {
            public InvocationProvider getProvider () {
                return provider;
            }

            public void dispatchRequest (ClientObject source, int methodId, Object[] args)
                throws InvocationException {
                // locate the method to be invoked
                Method m = invmeths.get(methodId);
                if (m == null) {
                    String pclass = StringUtil.shortClassName(provider.getClass());
                    log.warning("Requested to dispatch unknown method", "source", source.who(),
                                "methodId", methodId, "provider", pclass, "args", args);
                    throw new InvocationException(InvocationCodes.E_INTERNAL_ERROR);
                }

                // prepare the arguments: the ClientObject followed by the service method args
                Object[] fargs = new Object[args.length+1];
                System.arraycopy(args, 0, fargs, 1, args.length);
                fargs[0] = source;

                // actually invoke the method, and cope with failure
                try {
                    m.invoke(provider, fargs);
                } catch (IllegalAccessException ie) {
                    throw new RuntimeException(ie); // should never happen
                } catch (InvocationTargetException ite) {
                    Throwable cause = ite.getCause();
                    if (cause instanceof InvocationException) {
                        throw (InvocationException)cause;
                    } else {
                        log.warning("Invocation service method failure",
                                    "provider", StringUtil.shortClassName(provider.getClass()),
                                    "method", m.getName(), "args", fargs, cause);
                        throw new InvocationException(InvocationCodes.E_INTERNAL_ERROR);
                    }
                }
            }
        });

        // if it's a bootstrap service, slap it in the list
        if (group != null) {
            _bootlists.put(group, marsh);
        }

        _recentRegServices.put(Integer.valueOf(invCode), marsh.getClass().getName());

        log.debug("Registered service", "code", invCode, "marsh", marsh);
        return marsh;
    }

    /**
     * Registers the supplied invocation dispatcher, returning a marshaller that can be used to
     * send requests to the provider for whom the dispatcher is proxying.
     *
     * @param dispatcher the dispatcher to be registered.
     */
    public <T extends InvocationMarshaller<?>> T registerDispatcher (
        InvocationDispatcher<T> dispatcher)
    {
        return registerDispatcher(dispatcher, null);
    }

    /**
     * @deprecated use {@link #registerDispatcher(InvocationDispatcher)}.
     */
    @Deprecated public <T extends InvocationMarshaller<?>> T registerDispatcher (
        InvocationDispatcher<T> dispatcher, boolean bootstrap)
    {
        return registerDispatcher(dispatcher, null);
    }

    /**
     * Registers the supplied invocation dispatcher, returning a marshaller that can be used to
     * send requests to the provider for whom the dispatcher is proxying.
     *
     * @param dispatcher the dispatcher to be registered.
     * @param group the bootstrap group in which this marshaller is to be registered, or null if it
     * is not a bootstrap service. <em>Do not:</em> register a dispatcher with multiple boot
     * groups. You must collect shared dispatchers into as fine grained a set of groups as
     * necessary and have different types of clients specify the list of groups they need.
     */
    public <T extends InvocationMarshaller<?>> T registerDispatcher (
        InvocationDispatcher<T> dispatcher, String group)
    {
        _omgr.requireEventThread(); // sanity check

        // get the next invocation code
        int invCode = nextInvCode();

        // create the marshaller and initialize it
        T marsh = dispatcher.createMarshaller();
        marsh.init(_invoid, invCode, _standaloneClient == null ?
            null : _standaloneClient.getInvocationDirector());

        // register the dispatcher
        _dispatchers.put(invCode, dispatcher);

        // if it's a bootstrap service, slap it in the list
        if (group != null) {
            _bootlists.put(group, marsh);
        }

        _recentRegServices.put(Integer.valueOf(invCode), marsh.getClass().getName());

        log.debug("Registered service", "code", invCode, "marsh", marsh);
        return marsh;
    }

    /**
     * Clears out a dispatcher registration. This should be called to free up resources when an
     * invocation service is no longer going to be used.
     */
    public void clearDispatcher (InvocationMarshaller<?> marsh)
    {
        _omgr.requireEventThread(); // sanity check

        if (marsh == null) {
            log.warning("Refusing to unregister null marshaller.", new Exception());
            return;
        }

        if (_dispatchers.remove(marsh.getInvocationCode()) == null) {
            log.warning("Requested to remove unregistered marshaller?", "marsh", marsh,
                        new Exception());
        }
    }

    /**
     * Constructs a list of all bootstrap services registered in any of the supplied groups.
     */
    public List<InvocationMarshaller<?>> getBootstrapServices (String[] bootGroups)
    {
        List<InvocationMarshaller<?>> services = Lists.newArrayList();
        for (String group : bootGroups) {
            services.addAll(_bootlists.get(group));
        }
        return services;
    }

    /**
     * Get the class that is being used to dispatch the specified invocation code, for
     * informational purposes.
     *
     * @return the Class, or null if no dispatcher is registered with
     * the specified code.
     */
    public Class<?> getDispatcherClass (int invCode)
    {
        Object dispatcher = _dispatchers.get(invCode);
        return (dispatcher == null) ? null : dispatcher.getClass();
    }

    // documentation inherited from interface
    public void eventReceived (DEvent event)
    {
        log.debug("Event received", "event", event);

        if (event instanceof InvocationRequestEvent) {
            InvocationRequestEvent ire = (InvocationRequestEvent)event;
            dispatchRequest(ire.getSourceOid(), ire.getInvCode(),
                            ire.getMethodId(), ire.getArgs(), ire.getTransport());
        }
    }

    /**
     * Called when we receive an invocation request message. Dispatches the request to the
     * appropriate invocation provider via the registered invocation dispatcher.
     */
    protected void dispatchRequest (
        int clientOid, int invCode, int methodId, Object[] args, Transport transport)
    {
        // make sure the client is still around
        ClientObject source = (ClientObject)_omgr.getObject(clientOid);
        if (source == null) {
            log.info("Client no longer around for invocation request", "clientOid", clientOid,
                     "code", invCode, "methId", methodId, "args", args);
            return;
        }

        // look up the dispatcher
        Dispatcher disp = _dispatchers.get(invCode);
        if (disp == null) {
            log.info("Received invocation request but dispatcher registration was already cleared",
                     "code", invCode, "methId", methodId, "args", args,
                     "marsh", _recentRegServices.get(Integer.valueOf(invCode)));
            return;
        }

        // scan the args, initializing any listeners and keeping track of the "primary" listener
        ListenerMarshaller rlist = null;
        int acount = args.length;
        for (int ii = 0; ii < acount; ii++) {
            Object arg = args[ii];
            if (arg instanceof ListenerMarshaller) {
                ListenerMarshaller list = (ListenerMarshaller)arg;
                list.callerOid = clientOid;
                list.omgr = _omgr;
                list.transport = transport;
                // keep track of the listener we'll inform if anything
                // goes horribly awry
                if (rlist == null) {
                    rlist = list;
                }
            }
        }

        log.debug("Dispatching invreq", "caller", source.who(), "provider", disp.getProvider(),
                  "methId", methodId, "args", args);

        // dispatch the request
        try {
            if (rlist != null) {
                rlist.setInvocationId(
                    StringUtil.shortClassName(disp.getProvider()) + ", methodId=" + methodId);
            }
            disp.dispatchRequest(source, methodId, args);

        } catch (InvocationException ie) {
            if (rlist != null) {
                rlist.requestFailed(ie.getMessage());

            } else {
                log.warning("Service request failed but we've got no listener to inform of " +
                            "the failure", "caller", source.who(), "code", invCode,
                            "provider", disp.getProvider(), "methodId", methodId, "args", args,
                            "error", ie);
            }

        } catch (Throwable t) {
            log.warning("Dispatcher choked", "provider", disp.getProvider(), "caller", source.who(),
                        "methId", methodId, "args", args, t);

            // avoid logging an error when the listener notices that it's been ignored.
            if (rlist != null) {
                rlist.setNoResponse();
            }
        }
    }

    /**
     * Used to generate monotonically increasing provider ids.
     */
    protected synchronized int nextInvCode ()
    {
        return _invCode++;
    }

    protected interface Dispatcher {
        public InvocationProvider getProvider ();
        public void dispatchRequest (ClientObject source, int methodId, Object[] args)
            throws InvocationException;
    }

    /** The object id of the object on which we receive invocation service requests. */
    protected int _invoid = -1;

    /** Used to generate monotonically increasing provider ids. */
    protected int _invCode;

    /** A reference to the standalone client, if any. */
    @Inject(optional=true) protected Client _standaloneClient;

    /** The distributed object manager we're working with. */
    protected PresentsDObjectMgr _omgr;

    /** A table of invocation dispatchers each mapped by a unique code. */
    protected IntMap<Dispatcher> _dispatchers = IntMaps.newHashIntMap();

    /** Maps bootstrap group to lists of services to be provided to clients at boot time. */
    protected Multimap<String, InvocationMarshaller<?>> _bootlists = ArrayListMultimap.create();

    /** Tracks recently registered services so that we can complain informatively if a request
     * comes in on a service we don't know about. */
    protected final Map<Integer, String> _recentRegServices =
        new LRUHashMap<Integer, String>(10000);
}
