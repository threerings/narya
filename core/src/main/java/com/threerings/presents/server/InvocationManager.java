//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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
        _invobj = _omgr.registerObject(new DObject());

        log.debug("Created invocation service object", "oid", getOid());
    }

    /**
     * Returns the object id of the invocation services object.
     */
    public int getOid ()
    {
        return _invobj.getOid();
    }

    /**
     * Utility: Is the client subscribed to this object?
     */
    public boolean isSubscribed (ClientObject client, DObject dobj)
    {
        var session = _clmgr.getClient(client.username);
        return session != null && session.isSubscribed(dobj);
    }

    /**
     * Registers the supplied invocation service provider.
     *
     * @param provider the provider to be registered.
     * @param mclass the class of the invocation marshaller generated for the service.
     */
    public final <T extends InvocationMarshaller<?>> T registerProvider (
        DObject dobject, InvocationProvider provider, Class<T> mclass)
    {
        return registerProvider(provider, mclass, null, dobject);
    }

    /**
     * Registers the supplied invocation service provider.
     *
     * @param provider the provider to be registered.
     * @param mclass the class of the invocation marshaller generated for the service.
     */
    public final <T extends InvocationMarshaller<?>> T registerProvider (
        InvocationProvider provider, Class<T> mclass)
    {
        return registerProvider(provider, mclass, (String)null, (DObject)null);
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
    public final <T extends InvocationMarshaller<?>> T registerProvider (
        final InvocationProvider provider, Class<T> mclass, String group)
    {
        return registerProvider(provider, mclass, group, null);
    }

    /**
     * Register a provider either locally on the provied dobj or globally, and if globally
     * optionally in a group.
     */
    protected <T extends InvocationMarshaller<?>> T registerProvider (
        final InvocationProvider provider, Class<T> mclass, String group, DObject dobj)
    {
        _omgr.requireEventThread(); // sanity check

        if (dobj != null) {
          if (dobj.getOid() == 0) throw new RuntimeException("Dobj not set yet");
          if (dobj.getAccessController() == _omgr.getDefaultAccessController()) {
              log.warning("Registering a service on an object with the permissive default " +
                  "access controller; any client that can subscribe can invoke it",
                  "dobj", dobj.getClass().getSimpleName(), "marsh", mclass.getSimpleName());
          }
        }

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

        if (dobj == null) dobj = _invobj;
        var listener = _objectListeners.computeIfAbsent(dobj, EventRequestListener::new);
        // get the next invocation code
        final int invCode = listener.getNextCode();
        final int invOid = dobj.getOid();

        // create a marshaller instance and initialize it
        T marsh;
        try {
            marsh = mclass.getConstructor().newInstance();
            marsh.init(invOid, invCode,
                _standaloneClient == null ? null : _standaloneClient.getInvocationDirector());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException ee) {
            throw new RuntimeException(ee);
        }

        final Dispatcher dispatcher = customizeDispatcher(mclass, new Dispatcher() {
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
                    if (cause instanceof InvocationException ieCause) {
                        throw ieCause;
                    } else {
                        log.warning("Invocation service method failure",
                                    "provider", StringUtil.shortClassName(provider.getClass()),
                                    "method", m.getName(), "args", fargs, cause);
                        throw new InvocationException(InvocationCodes.E_INTERNAL_ERROR);
                    }
                }
            }
        });

        // add it to our listener
        listener.addDispatcher(invCode, dobj, dispatcher);

        // if it's a bootstrap service, slap it in the list
        if (group != null) _bootlists.put(group, marsh);

        log.debug("Registered service", "oid", invOid, "code", invCode, "marsh", marsh);
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

        int invOid = marsh.getInvocationOid();
        DObject dobj;
        if (invOid == _invobj.getOid()) dobj = _invobj;
        else {
            dobj = _omgr.getObject(invOid);
            if (dobj == null) return; // nothing to do (TODO: log?)
        }
        var listener = _objectListeners.get(dobj);
        if (listener == null || !listener.clearDispatcher(marsh, dobj)) {
          log.warning("Requested to remove unregistered marshaller?",
              "marsh", marsh, new Exception());
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
     * Get the class that is being used to dispatch the specified invocation, for
     * informational purposes.
     *
     * @return the Class, or null if no dispatcher is registered with
     * the specified code.
     */
    public Class<?> getDispatcherClass (InvocationRequestEvent ire)
    {
        var listener = _objectListeners.get(_omgr.getObject(ire.getTargetOid()));
        if (listener != null) {
            var dispatcher = listener.getDispatcher(ire.getInvCode());
            if (dispatcher != null) return dispatcher.getClass();
        }
        return null;
    }

    /**
     * Allow subclasses to modify behavior.
     */
    protected Dispatcher customizeDispatcher (
        Class<? extends InvocationMarshaller<?>> mclass, Dispatcher disp)
    {
        return disp;
    }

    /**
     * Called when we receive an invocation request message. Dispatches the request to the
     * appropriate invocation provider via the registered invocation dispatcher.
     */
    protected void dispatchRequest (InvocationRequestEvent ire, Dispatcher disp)
    {
        int clientOid = ire.getSourceOid();
        int invOid = ire.getTargetOid();
        int invCode = ire.getInvCode();
        int methodId = ire.getMethodId();
        Object[] args = ire.getArgs();

        // make sure the client is still around
        ClientObject source = (ClientObject)_omgr.getObject(clientOid);
        if (source == null) {
            log.info("Client no longer around for invocation request", "clientOid", clientOid,
                     "code", invCode, "methId", methodId, "args", args);
            return;
        }

        // scan the args, initializing any listeners and keeping track of the "primary" listener
        ListenerMarshaller rlist = null;
        int acount = args.length;
        for (int ii = 0; ii < acount; ii++) {
            Object arg = args[ii];
            if (arg instanceof ListenerMarshaller list) {
                list.callerOid = clientOid;
                list.omgr = _omgr;
                list.transport = ire.getTransport();
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
            log.warning("Dispatcher choked",
                "provider", disp.getProvider(), "caller", source.who(),
                "methId", methodId, "args", args, t);

            // avoid logging an error when the listener notices that it's been ignored.
            if (rlist != null) {
                rlist.setNoResponse();
            }
        }
    }

    protected interface Dispatcher {
        public InvocationProvider getProvider ();
        public void dispatchRequest (ClientObject source, int methodId, Object[] args)
            throws InvocationException;
    }

    protected class EventRequestListener
        implements EventListener
    {
        public EventRequestListener (DObject dobj)
        {
            _recents = new LRUHashMap<>(dobj == _invobj ? 10000 : 20);
        }

        public int getNextCode ()
        {
          return ++_lastCode;
        }

        public void addDispatcher (int invCode, DObject dobj, Dispatcher dispatcher)
        {
            if (_dispatchers.isEmpty()) dobj.addListener(this);
            _dispatchers.put(invCode, dispatcher);
        }

        public Dispatcher getDispatcher (int invCode)
        {
            return _dispatchers.get(invCode);
        }

        public boolean clearDispatcher (InvocationMarshaller<?> marsh, DObject dobj)
        {
            int invCode = marsh.getInvocationCode();
            var disp = _dispatchers.remove(invCode);
            if (disp == null) return false;
            // we removed it. Stop listening if it was the last one. (But we stay in the Weak map)
            if (_dispatchers.isEmpty()) {
                dobj.removeListener(this);
            }
            _recents.put(invCode, marsh.getClass().getName());
            return true;
        }

        // from EventListener
        public void eventReceived (DEvent event) {
            if (event instanceof InvocationRequestEvent ire) {
                var dispatcher = _dispatchers.get(ire.getInvCode());
                if (dispatcher != null) dispatchRequest(ire, dispatcher);
                else {
                    log.info("Received invocation request but dispatcher registration was " +
                        "already cleared",
                        "code", ire.getInvCode(), "methId", ire.getMethodId(),
                        "args", ire.getArgs(), "marsh", _recents.get(ire.getInvCode()));
                }
            }
        }

        /** The last code issued for this listener. */
        protected int _lastCode;

        /** A table of invocation dispatchers each mapped by a unique code. */
        protected final IntMap<Dispatcher> _dispatchers = IntMaps.newHashIntMap();

        /** Tracks recently registered services so that we can complain informatively
         * if a request comes in on a service we don't know about. */
        protected final Map<Integer, String> _recents;
    }

    protected final Map<DObject, EventRequestListener> _objectListeners = new WeakHashMap<>();

    /** The object on which we receive global invocation service requests. */
    protected final DObject _invobj;

    /** A reference to the standalone client, if any. */
    @Inject(optional=true) protected Client _standaloneClient;

    /** The ClientManager. */
    @Inject protected ClientManager _clmgr;

    /** The distributed object manager we're working with. */
    protected PresentsDObjectMgr _omgr;

    /** Maps bootstrap group to lists of services to be provided to clients at boot time. */
    protected final Multimap<String, InvocationMarshaller<?>> _bootlists =
      ArrayListMultimap.create();
}
