package com.threerings.presents.client {

import mx.collections.IViewCursor;
import mx.collections.ArrayCollection;

import com.threerings.util.SimpleMap;

import com.threerings.presents.data.ListenerMarshaller;

import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.EventListener;
import com.threerings.presents.dobj.InvocationNotificationEvent;
import com.threerings.presents.dobj.InvocationRequestEvent;
import com.threerings.presents.dobj.ObjectAccessError;
import com.threerings.presents.dobj.Subscriber;

import com.threerings.presents.data.ClientObject;

import com.threerings.presents.Log;

public class InvocationDirector
    implements EventListener
{
    public function init (omgr :DObjectManager, cloid :int, client :Client)
            :void
    {
        if (_clobj != null) {
            Log.warning("Zoiks, client object around during invmgr init!");
            cleanup();
        }

        _omgr = omgr;
        _client = client;

        _omgr.subscribeToObject(cloid, new ClientSubscriber(this));
    }

    public function cleanup () :void
    {
        // wipe our client object, receiver mappings and listener mappings
        _clobj = null;

        // also reset our counters
        _requestId = 0;
        _receiverId = 0;
    }

    /**
     * Registers an invocation notification receiver by way of its
     * notification event decoder.
     */
    public function registerReceiver (decoder :InvocationDecoder) :void
    {
        _reclist.addItem(decoder);

        // if we're already online, assign a recevier id now
        if (_clobj != null) {
            assignReceiverId(decoder);
        }
    }

    /**
     * Removes a receiver registration.
     */
    public function unregisterReceiver (receiverCode :String) :void
    {
        // remove the receiver from the list
        for (var iter :IViewCursor = _reclist.getCursor(); iter.moveNext(); ) {
            var decoder :InvocationDecoder =
                (iter.current as InvocationDecoder);
            if (decoder.getReceiverCode() === receiverCode) {
                iter.remove();
            }
        }

        // if we're logged on, clear out any receiver id mapping
        if (_clobj != null) { 
            var rreg :InvocationRegistration =
                (_clobj.receivers.get(receiverCode) as InvocationRegistration);
            if (rreg == null) {
                Log.warning("Receiver unregistered for which we have no " +
                            "id to code mapping [code=" + receiverCode + "].");
            } else {
                var odecoder :Object = _receivers.remove(rreg.receiverId);
//                 Log.info("Cleared receiver " +
//                          StringUtil.shortClassName(odecoder) +
//                          " " + rreg + ".");
            }
            _clobj.removeFromReceivers(receiverCode);
        }
    }

    /**
     * Assigns a receiver id to this decoder and publishes it in the
     * {@link ClientObject#receivers} field.
     */
    internal function assignReceiverId (decoder :InvocationDecoder) :void
    {
        var reg :InvocationRegistration = new InvocationRegistration(
            decoder.getReceiverCode(), nextReceiverId());
        _clobj.addToReceivers(reg);
        _receivers.put(reg.receiverId, decoder);
    }

    /**
     * Called when we log on; generates mappings for all receivers
     * registered prior to logon.
     */
    internal function assignReceiverIds () :void
    {
        _clobj.startTransaction();
        try {
            for (var itr :IViewCursor = _reclist.getCursor(); itr.moveNext(); ) {
                var decoder :InvocationDecoder =
                    (itr.current as InvocationDecoder);
                assignReceiverId(decoder);
            }
        } finally {
            _clobj.commitTransaction();
        }
    }

    /**
     * Requests that the specified invocation request be packaged up and
     * sent to the supplied invocation oid.
     */
    public function sendRequest (
            invOid :int, invCode :int, methodId :int, args :Array) :void
    {
        // configure any invocation listener marshallers among the args
        for each (var arg :Object in args) {
            if (arg is ListenerMarshaller) {
                var lm :ListenerMarshaller = (arg as ListenerMarshaller);
                lm.callerOid = _clobj.getOid();
                lm.requestId = nextRequestId();
                lm.mapStamp = new Date().getTime();

                // create a mapping for this marshaller so that we can
                // properly dispatch responses sent to it
                _listeners.put(lm.requestId, lm);
            }
        }

        // create an invocation request event
        var event :InvocationRequestEvent =
            new InvocationRequestEvent(invOid, invCode, methodId, args);

        // because invocation directors are used on the server, we set the
        // source oid here so that invocation requests are properly
        // attributed to the right client object when created by
        // server-side entities only sort of pretending to be a client
        event.setSourceOid(_clobj.getOid());

        // now, dispatch the event
        _omgr.postEvent(event);
    }

    // documentation inherited from interface EventListener
    public function eventReceived (event :DEvent) :void
    {
        if (event is InvocationResponseEvent) {
            var ire :InvocationResponseEvent =
                (event as InvocationResponseEvent);
            handleInvocationResponse(ire.getRequestId(), ire.getMethodId(),
                ire.getArgs());

        } else if (event is InvocationNotificationEvent) {
            var ine :InvocationNotificationEvent =
                (event as InvocationNotificationEvent);
            handleInvocationNotification(ine.getReceiverId(), ine.getMethodId(),
                ine.getArgs());

        } else if (event is MessageEvent) {
            var me :MessageEvent = (event as MessageEvent);
            if (me.getName() === ClientObject.CLOBJ_CHANGED) {
                handleClientObjectChanged(me.getArgs()[0]);
            }
        }
    }

    /**
     * Dispatches an invocation response.
     */
    protected function handleInvocationResponse 
            (reqId :int, methodId :int, args :Array) :void
    {
        var listener :ListenerMarshaller = 
            (_listeners.remove(reqId) as ListenerMarshaller);
        if (listener == null) {
            Log.warning("Received invocation response for which we have " +
                        "no registered listener [reqId=" + reqId +
                        ", methId=" + methodId +
                        ", args=" + args + "]. " +
                        "It is possble that this listener was flushed " +
                        "because the response did not arrive within " +
                        LISTENER_MAX_AGE + " milliseconds.");
            return;
        }

//         Log.info("Dispatching invocation response " +
//                  "[listener=" + listener + ", methId=" + methodId +
//                  ", args=" + StringUtil.toString(args) + "].");

        // dispatch the response
        try {
            listener.dispatchResponse(methodId, args);
        } catch (e :Error) {
            Log.warning("Invocation response listener choked " +
                        "[listener=" + listener + ", methId=" + methodId +
                        ", args=" + args + "].");
            Log.logStackTrace(e);
        }

        // flush expired listeners periodically
        var now :Number = new Date().getTime();
        if (now - _lastFlushTime > LISTENER_FLUSH_INTERVAL) {
            _lastFlushTime = now;
            flushListeners(now);
        }
    }

    /**
     * Dispatches an invocation notification.
     */
    protected function handleInvocationNotification (
        receiverId :int, methodId :int, args :Array) :void
    {
        // look up the decoder registered for this receiver
        var decoder :InvocationDecoder =
            (_receivers.get(receiverId) as InvocationDecoder);
        if (decoder == null) {
            Log.warning("Received notification for which we have no " +
                        "registered receiver [recvId=" + receiverId +
                        ", methodId=" + methodId +
                        ", args=" + args + "].");
            return;
        }

//         Log.info("Dispatching invocation notification " +
//                  "[receiver=" + decoder.receiver + ", methodId=" + methodId +
//                  ", args=" + StringUtil.toString(args) + "].");

        try {
            decoder.dispatchNotification(methodId, args);
        } catch (e :Error) {
            Log.warning("Invocation notification receiver choked " +
                        "[receiver=" + decoder.receiver +
                        ", methId=" + methodId +
                        ", args=" + args + "].");
            Log.logStackTrace(e);
        }
    }

    /**
     * Flushes listener mappings that are older than {@link
     * #LISTENER_MAX_AGE} milliseconds. An alternative to flushing
     * listeners that did not explicitly receive a response within our
     * expiry time period is to have the server's proxy listener send a
     * message to the client when it is finalized. We then know that no
     * server entity will subsequently use that proxy listener to send a
     * response to the client. This involves more network traffic and
     * complexity than seems necessary and if a user of the system does
     * respond after their listener has been flushed, an informative
     * warning will be logged. (Famous last words.)
     */
    protected function flushListeners (now :Number) :void
    {
        var then :Number = now - LISTENER_MAX_AGE;
        for (var skey :String in _listeners.keys()) {
            var lm :ListenerMarshaller =
                (_listeners.get(skey) as ListenerMarshaller);
            if (lm.mapStamp < then) {
                _listeners.remove(skey);
            }
        }
    }

    /**
     * Called when the server has informed us that our previous client
     * object is going the way of the Dodo because we're changing screen
     * names. We subscribe to the new object and report to the client once
     * we've got our hands on it.
     */
    protected function handleClientObjectChanged (newCloid :int) :void
    {
        // TODO: or fuck it?
    }

    /**
     * Used to generate monotonically increasing invocation request ids.
     */
    protected function nextRequestId () :int
    {
        return _requestId++;
    }

    /**
     * Used to generate monotonically increasing invocation receiver ids.
     */
    protected function nextReceiverId () :int
    {
        return _receiverId++;
    }

    /**
     * Called by the ClientSubscriber helper class when the client object
     * has been returned by the server.
     */
    internal function gotClientObject (clobj :ClientObject) :void
    {
        clobj.addListener(this);
        clobj.setReceivers(new DSet());
        _clobj = clobj;
        assignReceiverIds();

        _client.gotClientObject(clobj);
    }

    /**
     * Called by the ClientSubscriber helper class when it fails.
     */
    internal function gotClientObjectFailed (
            oid :int, cause :ObjectAccessError) :void
    {
        Log.warning("Invocation director unable to subscribe to " +
            "client object [cloid=" + oid + ", cause=" + cause + "]!");
        _client.getClientObjectFailed(cause);
    }

    /** The distributed object manager with which we interact. */
    internal var _omgr :DObjectManager;

    /** The client for whom we're working. */
    internal var _client :Client;

    /** Our client object; invocation responses and notifications are
     * received on this object. */
    internal var _clobj :ClientObject;

    /** Used to generate monotonically increasing request ids. */
    protected var _requestId :int;

    /** Used to generate monotonically increasing receiver ids. */
    protected var _receiverId :int;

    /** Used to keep track of invocation service listeners which will
     * receive responses from invocation service requests. */
    protected var _listeners :SimpleMap = new SimpleMap();

    /** Used to keep track of invocation notification receivers. */
    protected var _receivers :SimpleMap = new SimpleMap();

    /** All registered receivers are maintained in a list so that we can
     * assign receiver ids to them when we go online. */
    internal var _reclist :ArrayCollection = new ArrayCollection();

    /** The last time we flushed our listeners. */
    protected var _lastFlushTime :Number;

    /** The minimum interval between listener flush attempts. */
    protected const LISTENER_FLUSH_INTERVAL :int = 15000;

    /** Listener mappings older than 90 seconds are reaped. */
    protected const LISTENER_MAX_AGE :int = 90 * 1000;
}
}
