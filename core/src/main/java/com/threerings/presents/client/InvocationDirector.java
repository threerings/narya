//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.samskivert.util.HashIntMap;

import com.threerings.presents.client.InvocationReceiver.Registration;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller.ListenerMarshaller;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.EventListener;
import com.threerings.presents.dobj.InvocationNotificationEvent;
import com.threerings.presents.dobj.InvocationRequestEvent;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.net.Transport;

import static com.threerings.presents.Log.log;

/**
 * Handles the client side management of the invocation services.
 */
public class InvocationDirector
    implements EventListener
{
    /**
     * Initializes the invocation director. This is called when the client establishes a connection
     * with the server.
     *
     * @param omgr the distributed object manager via which the invocation manager will send and
     * receive events.
     * @param cloid the oid of the object on which invocation notifications as well as invocation
     * responses will be received.
     * @param client a reference to the client for whom we're doing our business.
     */
    public void init (DObjectManager omgr, final int cloid, Client client)
    {
        // sanity check
        if (_clobj != null) {
            log.warning("Zoiks, client object around during invmgr init!");
            cleanup();
        }

        // keep these for later
        _omgr = omgr;
        _client = client;

        // add ourselves as a subscriber to the client object
        _omgr.subscribeToObject(cloid, new Subscriber<ClientObject>() {
            public void objectAvailable (ClientObject clobj) {
                // add ourselves as an event listener
                clobj.addListener(InvocationDirector.this);

                // keep a handle on this bad boy
                _clobj = clobj;

                // assign a mapping to already registered receivers
                assignReceiverIds();

                // let the client know that we're ready to go now that we've got our subscription
                // to the client object
                _client.gotClientObject(_clobj);
            }

            public void requestFailed (int oid, ObjectAccessException cause) {
                // aiya! we were unable to subscribe to the client object.  we're hosed!
                log.warning("Invocation director unable to subscribe to client object",
                            "cloid", cloid, "cause", cause + "]!");
                _client.getClientObjectFailed(cause);
            }
        });
    }

    /**
     * Set the maximum time to keep listeners if we haven't heard a response back.
     */
    public void setMaximumListenerAge (long milliseconds)
    {
        Preconditions.checkArgument(milliseconds > 0);
        _listenerMaxAge = milliseconds;
    }

    /**
     * Clears out our session information. This is called when the client ends its session with the
     * server.
     */
    public void cleanup ()
    {
        // wipe our client object, receiver mappings and listener mappings
        _clobj = null;
        _receivers.clear();
        _listeners.clear();

        // also reset our counters
        _requestId = 0;
        _receiverId = 0;
    }

    /**
     * Registers an invocation notification receiver by way of its notification event decoder.
     */
    public void registerReceiver (InvocationDecoder decoder)
    {
        // add the receiver to the list
        _reclist.add(decoder);

        // if we're already online, assign a receiver id to this decoder
        if (_clobj != null) {
            assignReceiverId(decoder);
        }
    }

    /**
     * Removes a receiver registration.
     */
    public void unregisterReceiver (String receiverCode)
    {
        // remove the receiver from the list
        for (Iterator<InvocationDecoder> iter = _reclist.iterator(); iter.hasNext(); ) {
            InvocationDecoder decoder = iter.next();
            if (decoder.getReceiverCode().equals(receiverCode)) {
                iter.remove();
            }
        }

        // if we're logged on, clear out any receiver id mapping
        if (_clobj != null) {
            Registration rreg = _clobj.receivers.get(receiverCode);
            if (rreg == null) {
                log.warning("Receiver unregistered for which we have no id to code mapping",
                            "code", receiverCode);
            } else {
                _receivers.remove(rreg.receiverId);
//                 Log.info("Cleared receiver " + StringUtil.shortClassName(decoder) +
//                          " " + rreg + ".");
            }
            _clobj.removeFromReceivers(receiverCode);
        }
    }

    /**
     * Called when we log on; generates mappings for all receivers registered prior to logon.
     */
    protected void assignReceiverIds ()
    {
        // pack all the events into a single transaction
        _clobj.startTransaction();
        try {
            // clear out our previous registrations
            _clobj.setReceivers(new DSet<Registration>());

            for (InvocationDecoder decoder : _reclist) {
                assignReceiverId(decoder);
            }
        } finally {
            _clobj.commitTransaction();
        }
    }

    /**
     * Assigns a receiver id to this decoder and publishes it in the {@link ClientObject#receivers}
     * field.
     */
    protected void assignReceiverId (InvocationDecoder decoder)
    {
        Registration reg = new Registration(decoder.getReceiverCode(), nextReceiverId());
        // stick the mapping into the client object
        _clobj.addToReceivers(reg);
        // and map the receiver in our receivers table
        _receivers.put(reg.receiverId, decoder);
//         Log.info("Registered receiver " + StringUtil.shortClassName(decoder) + " " + reg + ".");
    }

    /**
     * Requests that the specified invocation request be packaged up and sent to the supplied
     * invocation oid.
     */
    public void sendRequest (int invOid, int invCode, int methodId, Object[] args)
    {
        sendRequest(invOid, invCode, methodId, args, Transport.DEFAULT);
    }

    /**
     * Requests that the specified invocation request be packaged up and sent to the supplied
     * invocation oid.
     */
    public void sendRequest (
        int invOid, int invCode, int methodId, Object[] args, Transport transport)
    {
        if (_clobj == null) {
            log.warning("Dropping invocation request on shutdown director", "code", invCode,
                        "methodId", methodId);
            return;
        }

        // configure any invocation listener marshallers among the arguments
        int acount = args.length;
        for (int ii = 0; ii < acount; ii++) {
            Object arg = args[ii];
            if (arg instanceof ListenerMarshaller) {
                ListenerMarshaller lm = (ListenerMarshaller)arg;
                lm.requestId = nextRequestId();
                lm.mapStamp = System.currentTimeMillis();
                // create a mapping for this marshaller so that we can properly dispatch responses
                // sent to it
                _listeners.put(lm.requestId, lm);
            }
        }

        // create an invocation request event
        InvocationRequestEvent event = new InvocationRequestEvent(invOid, invCode, methodId, args);
        event.setTransport(transport);

        // because invocation directors are used on the server, we set the source oid here so that
        // invocation requests are properly attributed to the right client object when created by
        // server-side entities only sort of pretending to be a client
        event.setSourceOid(_clobj.getOid());

//         Log.info("Sending invreq " + event + ".");

        // now dispatch the event
        _omgr.postEvent(event);
    }

    /**
     * Process notification and response events arriving on user object.
     */
    public void eventReceived (DEvent event)
    {
        if (event instanceof InvocationResponseEvent) {
            InvocationResponseEvent ire = (InvocationResponseEvent)event;
            handleInvocationResponse(ire.getRequestId(), ire.getMethodId(), ire.getArgs());

        } else if (event instanceof InvocationNotificationEvent) {
            InvocationNotificationEvent ine = (InvocationNotificationEvent)event;
            handleInvocationNotification(ine.getReceiverId(), ine.getMethodId(), ine.getArgs());

        } else if (event instanceof MessageEvent) {
            MessageEvent mevt = (MessageEvent)event;
            if (mevt.getName().equals(ClientObject.CLOBJ_CHANGED)) {
                handleClientObjectChanged(((Integer)mevt.getArgs()[0]).intValue());
            }
        }
    }

    /**
     * Dispatches an invocation response.
     */
    protected void handleInvocationResponse (int reqId, int methodId, Object[] args)
    {
        // look up the invocation marshaller registered for that response
        ListenerMarshaller listener = _listeners.remove(reqId);
        if (listener == null) {
            log.warning("Received invocation response for which we have no registered listener. " +
                        "It is possible that this listener was flushed because the response did " +
                        "not arrive within " + _listenerMaxAge + " milliseconds.",
                        "reqId", reqId, "methId", methodId, "args", args);
            return;
        }

//         log.info("Dispatching invocation response", "listener", listener,
//                  "methId", methodId, "args", args);

        // dispatch the response
        try {
            listener.dispatchResponse(methodId, args);
        } catch (Throwable t) {
            log.warning("Invocation response listener choked", "listener", listener,
                        "methId", methodId, "args", args, t);
        }

        // flush expired listeners periodically
        long now = System.currentTimeMillis();
        if (now - _lastFlushTime > LISTENER_FLUSH_INTERVAL) {
            _lastFlushTime = now;
            flushListeners(now);
        }
    }

    /**
     * Dispatches an invocation notification.
     */
    protected void handleInvocationNotification (int receiverId, int methodId, Object[] args)
    {
        // look up the decoder registered for this receiver
        InvocationDecoder decoder = _receivers.get(receiverId);
        if (decoder == null) {
            log.warning("Received notification for which we have no registered receiver",
                        "recvId", receiverId, "methodId", methodId, "args", args);
            return;
        }

//         log.info("Dispatching invocation notification", "receiver", decoder.receiver,
//                  "methodId", methodId, "args", args);

        try {
            decoder.dispatchNotification(methodId, args);
        } catch (Throwable t) {
            log.warning("Invocation notification receiver choked", "receiver", decoder.receiver,
                        "methId", methodId, "args", args, t);
        }
    }

    /**
     * Called when the server has informed us that our previous client object is going the way of
     * the Dodo because we're changing screen names. We subscribe to the new object and report to
     * the client once we've got our hands on it.
     */
    protected void handleClientObjectChanged (int newCloid)
    {
        // subscribe to the new client object
        _omgr.subscribeToObject(newCloid, new Subscriber<ClientObject>() {
            public void objectAvailable (ClientObject clobj) {
                // grab a reference to our old receiver registrations
                DSet<Registration> receivers = _clobj.receivers;

                // replace the client object
                _clobj = clobj;

                // add ourselves as an event listener
                _clobj.addListener(InvocationDirector.this);

                // reregister our receivers
                _clobj.startTransaction();
                try {
                    _clobj.setReceivers(new DSet<Registration>());
                    for (Registration reg : receivers) {
                        _clobj.addToReceivers(reg);
                    }
                } finally {
                    _clobj.commitTransaction();
                }

                // and report the switcheroo back to the client
                _client.clientObjectDidChange(_clobj);
            }

            public void requestFailed (int oid, ObjectAccessException cause) {
                log.warning("Aiya! Unable to subscribe to changed client object", "cloid", oid,
                            "cause", cause);
            }
        });
    }

    /**
     * Flushes listener mappings that are older than {@link #_listenerMaxAge} milliseconds. An
     * alternative to flushing listeners that did not explicitly receive a response within our
     * expiry time period is to have the server's proxy listener send a message to the client when
     * it is finalized. We then know that no server entity will subsequently use that proxy
     * listener to send a response to the client. This involves more network traffic and complexity
     * than seems necessary and if a user of the system does respond after their listener has been
     * flushed, an informative warning will be logged. (Famous last words.)
     */
    protected void flushListeners (long now)
    {
        if (_listeners.size() > 0) {
            long then = now - _listenerMaxAge;
            Iterator<ListenerMarshaller> iter = _listeners.values().iterator();
            while (iter.hasNext()) {
                ListenerMarshaller lm = iter.next();
                if (then > lm.mapStamp) {
//                     Log.info("Flushing marshaller " + lm + ".");
                    iter.remove();
                }
            }
        }
    }

    /**
     * Used to generate monotonically increasing invocation request ids.
     */
    protected synchronized short nextRequestId ()
    {
        return _requestId++;
    }

    /**
     * Used to generate monotonically increasing invocation receiver ids.
     */
    protected synchronized short nextReceiverId ()
    {
        return _receiverId++;
    }

    /** The distributed object manager with which we interact. */
    protected DObjectManager _omgr;

    /** The client for whom we're working. */
    protected Client _client;

    /** Our client object; invocation responses and notifications are received on this object. */
    protected ClientObject _clobj;

    /** Used to generate monotonically increasing request ids. */
    protected short _requestId;

    /** Used to generate monotonically increasing receiver ids. */
    protected short _receiverId;

    /** Used to keep track of invocation service listeners which will receive responses from
     * invocation service requests. */
    protected HashIntMap<ListenerMarshaller> _listeners = new HashIntMap<ListenerMarshaller>();

    /** Used to keep track of invocation notification receivers. */
    protected HashIntMap<InvocationDecoder> _receivers = new HashIntMap<InvocationDecoder>();

    /** All registered receivers are maintained in a list so that we can assign receiver ids to
     * them when we go online. */
    protected ArrayList<InvocationDecoder> _reclist = Lists.newArrayList();

    /** The last time we flushed our listeners. */
    protected long _lastFlushTime;

    /** The max age of listeners. */
    protected long _listenerMaxAge = 90 * 1000L;

    /** The minimum interval between listener flush attempts. */
    protected static final long LISTENER_FLUSH_INTERVAL = 15000L;
}
