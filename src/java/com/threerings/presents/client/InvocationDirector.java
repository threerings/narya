//
// $Id: InvocationDirector.java,v 1.22 2002/09/19 23:36:59 mdb Exp $

package com.threerings.presents.client;

import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.ResultListener;
import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.client.InvocationReceiver.Registration;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller.ListenerMarshaller;
import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.EventListener;
import com.threerings.presents.dobj.InvocationNotificationEvent;
import com.threerings.presents.dobj.InvocationRequestEvent;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;

/**
 * Handles the client side management of the invocation services.
 */
public class InvocationDirector
    implements EventListener
{
    /**
     * Initializes the invocation director. This is called when the client
     * establishes a connection with the server.
     *
     * @param omgr the distributed object manager via which the invocation
     * manager will send and receive events.
     * @param cloid the oid of the object on which invocation
     * notifications as well as invocation responses will be received.
     * @param client a reference to the client for whom we're doing our
     * business.
     */
    public void init (DObjectManager omgr, final int cloid, Client client)
    {
        // keep these for later
        _omgr = omgr;
        _client = client;

        // add ourselves as a subscriber to the client object
        _omgr.subscribeToObject(cloid, new Subscriber() {
            public void objectAvailable (DObject object) {
                // keep a handle on this bad boy
                _clobj = (ClientObject)object;

                // add ourselves as an event listener
                _clobj.addListener(InvocationDirector.this);

                // assign a mapping to already registered receivers
                assignReceiverIds();

                // let the client know that we're ready to go now that
                // we've got our subscription to the client object
                _client.gotClientObject(_clobj);
            }

            public void requestFailed (int oid, ObjectAccessException cause) {
                // aiya! we were unable to subscribe to the client object.
                // we're hosed, hosed, hosed
                Log.warning("Invocation director unable to subscribe to " +
                            "client object [cloid=" + cloid +
                            ", cause=" + cause + "]!");
                _client.getClientObjectFailed(cause);
            }
        });
    }

    /**
     * Clears out our session information. This is called when the client
     * ends its session with the server.
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
     * Registers an invocation notification receiver by way of its
     * notification event decoder.
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
        for (Iterator iter = _reclist.iterator(); iter.hasNext(); ) {
            InvocationDecoder decoder = (InvocationDecoder)iter.next();
            if (decoder.getReceiverCode().equals(receiverCode)) {
                iter.remove();
            }
        }

        // if we're logged on, clear out any receiver id mapping
        if (_clobj != null) {
            _clobj.removeFromReceivers(receiverCode);
        }
    }

    /**
     * Assigns a receiver id to this decoder and publishes it in the
     * {@link ClientObject#receivers} field.
     */
    protected void assignReceiverId (InvocationDecoder decoder)
    {
        Registration reg = new Registration(
            decoder.getReceiverCode(), nextReceiverId());
        // stick the mapping into the client object
        _clobj.addToReceivers(reg);
        // and map the receiver in our receivers table
        _receivers.put(reg.receiverId, decoder);
    }

    /**
     * Called when we log on; generates mappings for all receivers
     * registered prior to logon.
     */
    protected void assignReceiverIds ()
    {
        // pack all the set add events into a single transaction
        _clobj.startTransaction();
        try {
            for (Iterator iter = _reclist.iterator(); iter.hasNext(); ) {
                assignReceiverId((InvocationDecoder)iter.next());
            }
        } finally {
            _clobj.commitTransaction();
        }
    }

    /**
     * Requests that the specified invocation request be packaged up and
     * sent to the supplied invocation oid.
     */
    public void sendRequest (
        int invOid, int invCode, int methodId, Object[] args)
    {
        // configure any invocation listener marshallers among the
        // arguments
        int acount = args.length;
        for (int ii = 0; ii < acount; ii++) {
            Object arg = args[ii];
            if (arg instanceof ListenerMarshaller) {
                ListenerMarshaller lm = (ListenerMarshaller)arg;
                lm.callerOid = _clobj.getOid();
                lm.requestId = nextRequestId();
                // create a mapping for this marshaller so that we can
                // properly dispatch responses sent to it
                _listeners.put(lm.requestId, lm);
            }
        }

        // create an invocation request event
        InvocationRequestEvent event =
            new InvocationRequestEvent(invOid, invCode, methodId, args);

        // because invocation directors are used on the server, we set the
        // source oid here so that invocation requests are properly
        // attributed to the right client object when created by
        // server-side entities only sort of pretending to be a client
        event.setSourceOid(_clobj.getOid());

//         Log.info("Sending invocation request " + event + ".");

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
            handleInvocationResponse(
                ire.getRequestId(), ire.getMethodId(), ire.getArgs());

        } else if (event instanceof InvocationNotificationEvent) {
            InvocationNotificationEvent ine =
                (InvocationNotificationEvent)event;
            handleInvocationNotification(
                ine.getReceiverId(), ine.getMethodId(), ine.getArgs());

        } else if (event instanceof MessageEvent) {
            MessageEvent mevt = (MessageEvent)event;
            if (mevt.getName().equals(ClientObject.CLOBJ_CHANGED)) {
                handleClientObjectChanged(
                    ((Integer)mevt.getArgs()[0]).intValue());
            }
        }
    }

    /**
     * Dispatches an invocation response.
     */
    protected void handleInvocationResponse (
        int reqId, int methodId, Object[] args)
    {
        // look up the invocation marshaller registered for that response
        ListenerMarshaller listener = (ListenerMarshaller)
            _listeners.remove(reqId);
        if (listener == null) {
            Log.warning("Received invocation response for which we have " +
                        "no registered listener [reqId=" + reqId +
                        ", methId=" + methodId +
                        ", args=" + StringUtil.toString(args) + "].");
            return;
        }

//         Log.info("Dispatching invocation response " +
//                  "[listener=" + listener + ", methId=" + methodId +
//                  ", args=" + StringUtil.toString(args) + "].");

        // dispatch the response
        try {
            listener.dispatchResponse(methodId, args);
        } catch (Throwable t) {
            Log.warning("Invocation response listener choked " +
                        "[listener=" + listener + ", methId=" + methodId +
                        ", args=" + StringUtil.toString(args) + "].");
            Log.logStackTrace(t);
        }
    }

    /**
     * Dispatches an invocation notification.
     */
    protected void handleInvocationNotification (
        int receiverId, int methodId, Object[] args)
    {
        // look up the decoder registered for this receiver
        InvocationDecoder decoder = (InvocationDecoder)
            _receivers.get(receiverId);
        if (decoder == null) {
            Log.warning("Received notification for which we have no " +
                        "registered receiver [recvId=" + receiverId +
                        ", methodId=" + methodId +
                        ", args=" + StringUtil.toString(args) + "].");
            return;
        }

//         Log.info("Dispatching invocation notification " +
//                  "[receiver=" + decoder.receiver + ", methodId=" + methodId +
//                  ", args=" + StringUtil.toString(args) + "].");

        try {
            decoder.dispatchNotification(methodId, args);
        } catch (Throwable t) {
            Log.warning("Invocation notification receiver choked " +
                        "[receiver=" + decoder.receiver +
                        ", methId=" + methodId +
                        ", args=" + StringUtil.toString(args) + "].");
            Log.logStackTrace(t);
        }
    }

    /**
     * Called when the server has informed us that our previous client
     * object is going the way of the Dodo because we're changing screen
     * names. We subscribe to the new object and report to the client once
     * we've got our hands on it.
     */
    protected void handleClientObjectChanged (int newCloid)
    {
        // subscribe to the new client object
        _omgr.subscribeToObject(newCloid, new Subscriber() {
            public void objectAvailable (DObject object) {
                // grab a reference to our old receiver registrations
                DSet receivers = _clobj.receivers;

                // replace the client object
                _clobj = (ClientObject)object;

                // add ourselves as an event listener
                _clobj.addListener(InvocationDirector.this);

                // reregister our receivers
                try {
                    _clobj.startTransaction();
                    Iterator iter = receivers.entries();
                    while (iter.hasNext()) {
                        _clobj.addToReceivers((Registration)iter.next());
                    }
                } finally {
                    _clobj.commitTransaction();
                }

                // and report the switcheroo back to the client
                _client.clientObjectDidChange(_clobj);
            }

            public void requestFailed (int oid, ObjectAccessException cause) {
                Log.warning("Aiya! Unable to subscribe to changed " +
                            "client object [cloid=" + oid +
                            ", cause=" + cause + "].");
            }
        });
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

    /** Our client object; invocation responses and notifications are
     * received on this object. */
    protected ClientObject _clobj;

    /** Used to generate monotonically increasing request ids. */
    protected short _requestId;

    /** Used to generate monotonically increasing receiver ids. */
    protected short _receiverId;

    /** Used to keep track of invocation service listeners which will
     * receive responses from invocation service requests. */
    protected HashIntMap _listeners = new HashIntMap();

    /** Used to keep track of invocation notification receivers. */
    protected HashIntMap _receivers = new HashIntMap();

    /** All registered receivers are maintained in a list so that we can
     * assign receiver ids to them when we go online. */
    protected ArrayList _reclist = new ArrayList();
}
