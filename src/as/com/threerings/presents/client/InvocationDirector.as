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

package com.threerings.presents.client {

import flash.errors.IllegalOperationError;
import flash.utils.getTimer; // function import

import com.threerings.util.HashMap;
import com.threerings.util.Log;
import com.threerings.util.Wrapped;

import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

import com.threerings.presents.dobj.CompoundEvent;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.EventListener;
import com.threerings.presents.dobj.InvocationNotificationEvent;
import com.threerings.presents.dobj.InvocationRequestEvent;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.ObjectAccessError;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.dobj.SubscriberAdapter;

import com.threerings.presents.data.ClientObject;

public class InvocationDirector
    implements EventListener
{
    private static const log :Log = Log.getLog(InvocationDirector);

    public function init (omgr :DObjectManager, cloid :int, client :Client) :void
    {
        if (_clobj != null) {
            log.warning("Zoiks, client object around during invmgr init!");
            cleanup();
        }

        _omgr = omgr;
        _client = client;

        _omgr.subscribeToObject(cloid, new SubscriberAdapter(
            this.gotClientObject, this.gotClientObjectFailed));
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
     * Registers an invocation notification receiver by way of its notification event decoder.
     */
    public function registerReceiver (decoder :InvocationDecoder) :void
    {
        _reclist.push(decoder);

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
        for (var ii :int = _reclist.length - 1; ii >= 0; ii--) {
            var decoder :InvocationDecoder = (_reclist[ii] as InvocationDecoder);
            if (decoder.getReceiverCode() === receiverCode) {
                _reclist.splice(ii, 1);
            }
        }

        // if we're logged on, clear out any receiver id mapping
        if (_clobj != null) { 
            var rreg :InvocationReceiver_Registration =
                (_clobj.receivers.get(receiverCode) as InvocationReceiver_Registration);
            if (rreg == null) {
                log.warning("Receiver unregistered for which we have no id to code mapping " +
                            "[code=" + receiverCode + "].");
            } else {
                var odecoder :Object = _receivers.remove(rreg.receiverId);
//                 log.info("Cleared receiver " + StringUtil.shortClassName(odecoder) +
//                          " " + rreg + ".");
            }
            _clobj.removeFromReceivers(receiverCode);
        }
    }

    /**
     * Assigns a receiver id to this decoder and publishes it in the {@link ClientObject#receivers}
     * field.
     */
    internal function assignReceiverId (decoder :InvocationDecoder) :void
    {
        var reg :InvocationReceiver_Registration =
            new InvocationReceiver_Registration(decoder.getReceiverCode(), nextReceiverId());
        _clobj.addToReceivers(reg);
        _receivers.put(reg.receiverId, decoder);
    }

    /**
     * Called when we log on; generates mappings for all receivers registered prior to logon.
     */
    internal function assignReceiverIds () :void
    {
        _clobj.startTransaction();
        try {
            for each (var decoder :InvocationDecoder in _reclist) {
                assignReceiverId(decoder);
            }
        } finally {
            _clobj.commitTransaction();
        }
    }

    /**
     * Starts a transaction that allows multiple invocation service requests to be batched into a
     * single message and sent to the server all at once.
     *
     * <p> When the transaction is complete, the caller must call {@link #commitTransaction} to
     * cause the requests to be sent. Failure to do so will render the entire invocation services
     * non-functional.
     */
    public function startTransaction () :void
    {
        // just increment our transaction nesting count, sendRequest will handle everything else
        _tcount++;
    }

    /**
     * Commits a transaction started with {@link #startTransaction}.
     */
    public function commitTransaction () :void
    {
        if (_tcount <= 0) {
            throw new IllegalOperationError("Cannot commit: not involved in a transaction");
        }
        if (--_tcount == 0) {
            for each (var event :CompoundEvent in _tevents) {
                event.commit(_omgr);
            }
            _tevents = [];
        }
    }

    /**
     * Requests that the specified invocation request be packaged up and sent to the supplied
     * invocation oid.
     */
    public function sendRequest (invOid :int, invCode :int, methodId :int, args :Array) :void
    {
        // configure any invocation listener marshallers among the args
        for each (var arg :Object in args) {
            if (arg is InvocationMarshaller_ListenerMarshaller) {
                var lm :InvocationMarshaller_ListenerMarshaller =
                    (arg as InvocationMarshaller_ListenerMarshaller);
                lm.callerOid = _clobj.getOid();
                lm.requestId = nextRequestId();
                lm.mapStamp = getTimer();

                // create a mapping for this marshaller so that we can properly dispatch responses
                // sent to it
                _listeners.put(lm.requestId, lm);
            }
        }

        // create an invocation request event and dispatch it
        var req :InvocationRequestEvent =
            new InvocationRequestEvent(invOid, invCode, methodId, args);
        if (_tcount > 0) {
            var event :CompoundEvent = _tevents[invOid];
            if (event == null) {
                _tevents[invOid] = (event = new CompoundEvent(invOid));
            }
            event.postEvent(req);
        } else {
            _omgr.postEvent(req);
        }
    }

    // documentation inherited from interface EventListener
    public function eventReceived (event :DEvent) :void
    {
        if (event is InvocationResponseEvent) {
            var ire :InvocationResponseEvent = (event as InvocationResponseEvent);
            handleInvocationResponse(ire.getRequestId(), ire.getMethodId(), ire.getArgs());

        } else if (event is InvocationNotificationEvent) {
            var ine :InvocationNotificationEvent = (event as InvocationNotificationEvent);
            handleInvocationNotification(ine.getReceiverId(), ine.getMethodId(), ine.getArgs());

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
    protected function handleInvocationResponse (reqId :int, methodId :int, args :Array) :void
    {
        var listener :InvocationMarshaller_ListenerMarshaller = 
            (_listeners.remove(reqId) as InvocationMarshaller_ListenerMarshaller);
        if (listener == null) {
            log.warning("Received invocation response for which we have no registered listener " +
                        "[reqId=" + reqId + ", methId=" + methodId + ", args=" + args + "]. " +
                        "It is possble that this listener was flushed because the response did " +
                        "not arrive within " + LISTENER_MAX_AGE + " milliseconds.");
            return;
        }

        unwrapArgs(args);

//         log.info("Dispatching invocation response [listener=" + listener +
//                  ", methId=" + methodId + ", args=" + StringUtil.toString(args) + "].");

        // dispatch the response
        try {
            listener.dispatchResponse(methodId, args);
        } catch (e :Error) {
            log.warning("Invocation response listener choked [listener=" + listener +
                        ", methId=" + methodId + ", args=" + args + "].");
            log.logStackTrace(e);
        }

        // flush expired listeners periodically
        var now :Number = getTimer();
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
        var decoder :InvocationDecoder = (_receivers.get(receiverId) as InvocationDecoder);
        if (decoder == null) {
            log.warning("Received notification for which we have no registered receiver " +
                        "[recvId=" + receiverId + ", methodId=" + methodId +
                        ", args=" + args + "].");
            return;
        }

        unwrapArgs(args);

//         log.info("Dispatching invocation notification [receiver=" + decoder.receiver +
//                  ", methodId=" + methodId + ", args=" + StringUtil.toString(args) + "].");

        try {
            decoder.dispatchNotification(methodId, args);
        } catch (e :Error) {
            log.warning("Invocation notification receiver choked [receiver=" + decoder.receiver +
                        ", methId=" + methodId + ", args=" + args + "].");
            log.logStackTrace(e);
        }
    }

    /**
     * Flushes listener mappings that are older than {@link #LISTENER_MAX_AGE} milliseconds. An
     * alternative to flushing listeners that did not explicitly receive a response within our
     * expiry time period is to have the server's proxy listener send a message to the client when
     * it is finalized. We then know that no server entity will subsequently use that proxy
     * listener to send a response to the client. This involves more network traffic and complexity
     * than seems necessary and if a user of the system does respond after their listener has been
     * flushed, an informative warning will be logged. (Famous last words.)
     */
    protected function flushListeners (now :Number) :void
    {
        var then :Number = now - LISTENER_MAX_AGE;
        for each (var reqId :int in _listeners.keys()) {
            var lm :InvocationMarshaller_ListenerMarshaller =
                (_listeners.get(reqId) as InvocationMarshaller_ListenerMarshaller);
            if (lm.mapStamp < then) {
                _listeners.remove(reqId);
            }
        }
    }

    /**
     * Called when the server has informed us that our previous client object is going the way of
     * the Dodo because we're changing screen names. We subscribe to the new object and report to
     * the client once we've got our hands on it.
     */
    protected function handleClientObjectChanged (newCloid :int) :void
    {
        // TODO: or fuck it?
    }

    /**
     * Unwrap any arguments that have arrived from the server in wrapped types.
     */
    protected function unwrapArgs (args :Array) :void
    {
        if (args != null) {
            for (var ii :int = 0; ii < args.length; ii++) {
                if (args[ii] is Wrapped) {
                    args[ii] = (args[ii] as Wrapped).unwrap();
                }
            }
        }
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
     * Called by the ClientObject SubscriberAdapter when the client object has been returned by the
     * server.
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
     * Called by the ClientObject SubscriberAdapter when it fails.
     */
    internal function gotClientObjectFailed (oid :int, cause :ObjectAccessError) :void
    {
        log.warning("Invocation director unable to subscribe to client object [cloid=" + oid +
                    ", cause=" + cause + "]!");
        _client.getClientObjectFailed(cause);
    }

    /** The distributed object manager with which we interact. */
    internal var _omgr :DObjectManager;

    /** The client for whom we're working. */
    internal var _client :Client;

    /** Our client object; invocation responses and notifications are received on this object. */
    internal var _clobj :ClientObject;

    /** Used to generate monotonically increasing request ids. */
    protected var _requestId :int;

    /** Used to generate monotonically increasing receiver ids. */
    protected var _receiverId :int;

    /** Used to keep track of invocation service listeners which will receive responses from
     * invocation service requests. */
    protected var _listeners :HashMap = new HashMap();

    /** Used to keep track of invocation notification receivers. */
    protected var _receivers :HashMap = new HashMap();

    /** A count of how deeply nested we are in transaction land. */
    protected var _tcount :int;

    /** An event used to accumulate service request events when in a transaction. */
    protected var _tevents :Array = [];

    /** All registered receivers are maintained in a list so that we can assign receiver ids to
     * them when we go online. */
    internal var _reclist :Array = [];

    /** The last time we flushed our listeners. */
    protected var _lastFlushTime :Number;

    /** The minimum interval between listener flush attempts. */
    protected const LISTENER_FLUSH_INTERVAL :int = 15000;

    /** Listener mappings older than 90 seconds are reaped. */
    protected const LISTENER_MAX_AGE :int = 90 * 1000;
}
}
