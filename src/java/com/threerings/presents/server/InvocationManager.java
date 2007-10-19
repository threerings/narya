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

package com.threerings.presents.server;

import java.util.HashMap;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.LRUHashMap;
import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;
import com.threerings.util.StreamableArrayList;

import com.threerings.presents.Log;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller.ListenerMarshaller;
import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.EventListener;
import com.threerings.presents.dobj.InvocationRequestEvent;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.RootDObjectManager;

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
public class InvocationManager
    implements EventListener
{
    /**
     * Constructs an invocation manager which will use the supplied distributed object manager to
     * operate its invocation services. Generally only one invocation manager should be operational
     * in a particular system.
     */
    public InvocationManager (RootDObjectManager omgr)
    {
        _omgr = omgr;

        // create the object on which we'll listen for invocation requests
        DObject invobj = omgr.registerObject(new DObject());
        invobj.addListener(this);
        _invoid = invobj.getOid();

//         Log.info("Created invocation service object [oid=" + _invoid + "].");
    }

    /**
     * Returns the object id of the invocation services object.
     */
    public int getOid ()
    {
        return _invoid;
    }

    /**
     * Registers the supplied invocation dispatcher, returning a marshaller that can be used to
     * send requests to the provider for whom the dispatcher is proxying.
     *
     * @param dispatcher the dispatcher to be registered.
     */
    public InvocationMarshaller registerDispatcher (InvocationDispatcher dispatcher)
    {
        return registerDispatcher(dispatcher, null);
    }

    /**
     * @Deprecated use {@link #registerDispatcher(InvocationDispatcher)}.
     */
    public InvocationMarshaller registerDispatcher (
        InvocationDispatcher dispatcher, boolean bootstrap)
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
    public InvocationMarshaller registerDispatcher (InvocationDispatcher dispatcher, String group)
    {
        // get the next invocation code
        int invCode = nextInvCode();

        // create the marshaller and initialize it
        InvocationMarshaller marsh = dispatcher.createMarshaller();
        marsh.init(_invoid, invCode);

        // register the dispatcher
        _dispatchers.put(invCode, dispatcher);

        // if it's a bootstrap service, slap it in the list
        if (group != null) {
            StreamableArrayList<InvocationMarshaller> list = _bootlists.get(group);
            if (list == null) {
                _bootlists.put(group, list = new StreamableArrayList<InvocationMarshaller>());
            }
            list.add(marsh);
        }

        _recentRegServices.put(Integer.valueOf(invCode), marsh.getClass().getName());

//        Log.info("Registered service [marsh=" + marsh + "].");
        return marsh;
    }

    /**
     * Clears out a dispatcher registration. This should be called to free
     * up resources when an invocation service is no longer going to be
     * used.
     */
    public void clearDispatcher (InvocationMarshaller marsh)
    {
        if (marsh == null) {
            Log.warning("Refusing to unregister null marshaller.");
            Thread.dumpStack();
            return;
        }

        if (_dispatchers.remove(marsh.getInvocationCode()) == null) {
            Log.warning("Requested to remove unregistered marshaller? " +
                        "[marsh=" + marsh + "].");
            Thread.dumpStack();
        }
    }

    /**
     * Constructs a list of all bootstrap services registered in any of the supplied groups.
     */
    public StreamableArrayList<InvocationMarshaller> getBootstrapServices (String[] bootGroups)
    {
        StreamableArrayList<InvocationMarshaller> services =
            new StreamableArrayList<InvocationMarshaller>();
        for (String group : bootGroups) {
            StreamableArrayList<InvocationMarshaller> list = _bootlists.get(group);
            if (list != null) {
                services.addAll(list);
            }
        }
        return services;
    }

    /**
     * Get the class that is being used to dispatch the specified
     * invocation code, for informational purposes.
     * 
     * @return the Class, or null if no dispatcher is registered with
     * the specified code.
     */
    public Class getDispatcherClass (int invCode)
    {
        Object dispatcher = _dispatchers.get(invCode);
        return (dispatcher == null) ? null : dispatcher.getClass();
    }

    // documentation inherited from interface
    public void eventReceived (DEvent event)
    {
//         Log.info("Event received " + event + ".");

        if (event instanceof InvocationRequestEvent) {
            InvocationRequestEvent ire = (InvocationRequestEvent)event;
            dispatchRequest(ire.getSourceOid(), ire.getInvCode(),
                            ire.getMethodId(), ire.getArgs());
        }
    }

    /**
     * Called when we receive an invocation request message. Dispatches
     * the request to the appropriate invocation provider via the
     * registered invocation dispatcher.
     */
    protected void dispatchRequest (
        int clientOid, int invCode, int methodId, Object[] args)
    {
        // make sure the client is still around
        ClientObject source = (ClientObject)_omgr.getObject(clientOid);
        if (source == null) {
            Log.info("Client no longer around for invocation " +
                     "request [clientOid=" + clientOid +
                     ", code=" + invCode + ", methId=" + methodId +
                     ", args=" + StringUtil.toString(args) + "].");
            return;
        }

        // look up the dispatcher
        InvocationDispatcher disp = _dispatchers.get(invCode);
        if (disp == null) {
            Log.info("Received invocation request but dispatcher " +
                     "registration was already cleared [code=" + invCode +
                     ", methId=" + methodId +
                     ", args=" + StringUtil.toString(args) + ", marsh=" +
                     _recentRegServices.get(Integer.valueOf(invCode)) + "].");
            return;
        }

        // scan the args, initializing any listeners and keeping track of
        // the "primary" listener
        ListenerMarshaller rlist = null;
        int acount = args.length;
        for (int ii = 0; ii < acount; ii++) {
            Object arg = args[ii];
            if (arg instanceof ListenerMarshaller) {
                ListenerMarshaller list = (ListenerMarshaller)arg;
                list.omgr = _omgr;
                // keep track of the listener we'll inform if anything
                // goes horribly awry
                if (rlist == null) {
                    rlist = list;
                }
            }
        }

//         Log.debug("Dispatching invreq [caller=" + source.who() +
//                   ", disp=" + disp + ", methId=" + methodId +
//                   ", args=" + StringUtil.toString(args) + "].");

        // dispatch the request
        try {
            if (rlist != null) {
                rlist.setInvocationId(StringUtil.shortClassName(disp) +
                    ", methodId=" + methodId);
            }
            disp.dispatchRequest(source, methodId, args);

        } catch (InvocationException ie) {
            if (rlist != null) {
                rlist.requestFailed(ie.getMessage());

            } else {
                Log.warning("Service request failed but we've got no " +
                            "listener to inform of the failure " +
                            "[caller=" + source.who() + ", code=" + invCode +
                            ", dispatcher=" + disp + ", methodId=" + methodId +
                            ", args=" + StringUtil.toString(args) +
                            ", error=" + ie + "].");
            }

        } catch (Throwable t) {
            Log.warning("Dispatcher choked [disp=" + disp +
                        ", caller=" + source.who() + ", methId=" + methodId +
                        ", args=" + StringUtil.toString(args) + "].");
            Log.logStackTrace(t);

            // avoid logging an error when the listener notices that it's
            // been ignored.
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

    // debugging action...
    protected static final LRUHashMap<Integer,String> _recentRegServices =
        new LRUHashMap<Integer,String>(10000);

    /** The distributed object manager with which we're working. */
    protected RootDObjectManager _omgr;

    /** The object id of the object on which we receive invocation service requests. */
    protected int _invoid = -1;

    /** Used to generate monotonically increasing provider ids. */
    protected int _invCode;

    /** A table of invocation dispatchers each mapped by a unique code. */
    protected HashIntMap<InvocationDispatcher> _dispatchers =
        new HashIntMap<InvocationDispatcher>();

    /** A mapping from bootstrap group to lists of services that are to be provided to clients at
     * boot time. */
    protected HashMap<String,StreamableArrayList<InvocationMarshaller>> _bootlists =
        new HashMap<String,StreamableArrayList<InvocationMarshaller>>();

    /** The text that is appended to the procedure name when automatically generating a failure
     * response. */
    protected static final String FAILED_SUFFIX = "Failed";
}
