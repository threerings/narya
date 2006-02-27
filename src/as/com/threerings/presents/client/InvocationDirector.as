package com.threerings.presents.client {

import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.EventListener;
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
        _clobj = null;

        // TODO: lots more
    }

    /**
     * Registers an invocation notification receiver by way of its
     * notification event decoder.
     */
    public function registerReceiver (decoder :InvocationDecoder) :void
    {
        _reclist.push(decoder);

        // if we're already online, assign a recevier id now
        if (_clobj != null) {
            assignReceiverId(decoder);
        }
    }

    // documentation inherited from interface EventListener
    public function eventReceived (event :DEvent) :void
    {
        // TODO
    }

    /**
     * Assigns a receiver id to this decoder and publishes it in the
     * {@link ClientObject#receivers} field.
     */
    internal function assignReceiverId (decoder :InvocationDecoder) :void
    {
        // TODO
    }

    /**
     * Called when we log on; generates mappings for all receivers
     * registered prior to logon.
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
     * Called by the ClientSubscriber helper class when the client object
     * has been returned by the server.
     */
    public function gotClientObject (clobj :ClientObject) :void
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
    public function gotClientObjectFailed (oid :int, cause :ObjectAccessError)
            :void
    {
        Log.warning("Invocation director unable to subscribe to " +
            "client object [cloid=" + oid + ", cause=" + cause + "]!");
        _client.getClientObjectFailed(cause);
    }

    internal var _omgr :DObjectManager;

    internal var _client :Client;

    internal var _clobj :ClientObject;

    /** All registered receivers are maintained in a list so that we can
     * assign receiver ids to them when we go online. */
    internal var _reclist :Array = new Array();
}
}

import com.threerings.presents.client.InvocationDirector;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessError;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.Log;

class ClientSubscriber implements Subscriber
{
    public function ClientSubscriber (invdir :InvocationDirector)
    {
        _invdir = invdir;
    }

    public function objectAvailable (obj :DObject) :void
    {
        _invdir.gotClientObject(obj as ClientObject);
    }

    public function requestFailed (oid :int, cause :ObjectAccessError) :void
    {
        _invdir.gotClientObjectFailed(oid, cause);
    }

    protected var _invdir :InvocationDirector;
}
