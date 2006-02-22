package com.threerings.presents.client {

import flash.util.trace;

import flash.events.EventDispatcher;
import flash.events.TimerEvent;
import flash.util.Timer;

import com.threerings.presents.data.ClientObject;

import com.threerings.presents.dobj.DObjectManager;

import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.PingRequest;
import com.threerings.presents.net.PongResponse;

public class Client extends EventDispatcher
{
    /** The default port on which the server listens for client connections. */
    public static const DEFAULT_SERVER_PORT :int = 47624;

    public function Client (creds :Credentials)
    {
        _creds = creds;
    }

    public function setServer (hostname :String, port :int) :void
    {
        _hostname = hostname;
        _port = port;
    }

    public function getHostname () :String
    {
        return _hostname;
    }

    public function getPort () :int
    {
        return _port;
    }

    public function getCredentials () :Credentials
    {
        return _creds;
    }

    public function setCredentials (creds :Credentials) :void
    {
        _creds = creds;
    }

    public function getVersion () :String
    {
        return _version;
    }

    public function setVersion (version :String)
    {
        _version = version;
    }

    public function getAuthResponseData () :AuthResponseData
    {
        return _authData;
    }

    public function setAuthResponseData (data :AuthResponseData) :void
    {
        _authData = data;
    }

    public function getDObjectManager () :DObjectManager
    {
        return _omgr;
    }

    public function getClientOid () :int
    {
        return _cloid;
    }

    public function getClientObject () :ClientObject
    {
        return _clobj;
    }

    public function getInvocationDirector () :InvocationDirector
    {
        return _invdir;
    }

    public function getService (clazz :Class) :InvocationService
    {
        if (_bstrap == null) {
            return null;
        }

        // TODO
        return null;
    }

    public function requireService (clazz :Class) :InvocationService
    {
        var isvc :InvocationService = getService(clazz);
        if (isvc == null) {
            throw new Error(clazz + " isn't available. I can't bear to go on.");
        }
        return isvc;
    }

    public function getBootstrapData () :BootstrapData
    {
        return _bstrap;
    }

    public function isLoggedOn () :Boolean
    {
        return (_clobj != null);
    }

    /**
     * Requests that this client connect and logon to the server with
     * which it was previously configured.
     *
     * @return false if we're already logged on.
     */
    public function logon () :Boolean
    {
        // if we have a communicator, we're already logged on
        if (_comm != null) {
            return false;
        }

        _comm = new Communicator(this);
        _comm.logon();

        if (_tickInterval == null) {
            _tickInterval = new Timer(5000);
            _tickInterval.addEventListener(TimerEvent.TIMER, tick);
            _tickInterval.start();
        }
        return true;
    }

    /**
     * Requests that the client log off of the server to which it is
     * connected.
     *
     * @param abortable if true, the client will call clientWillDisconnect
     * on allthe client observers and abort the logoff process if any of them
     * return false. If false, clientWillDisconnect will not be called.
     *
     * @return true if the logoff succeeded, false if it failed due to a
     * disagreeable observer.
     */
    public function logoff (abortable :Boolean) :Boolean
    {
        if (_comm == null) {
            trace("Ignoring request to log off: not logged on.");
            return true;
        }

        // if the request is abortable, let's run it past the observers.
        // if any of them call preventDefault() then the logoff will be
        // cancelled
        if (abortable && !notifyObservers(ClientEvent.CLIENT_WILL_LOGOFF)) {
            return false;
        }

        _tickInterval.stop();
        _tickInterval = null;

        _comm.logoff();
        return true;
    }

    public function gotBootstrap (data :BootstrapData, omgr :DObjectManager)
            :void
    {
        trace("Got bootstrap " + data + ".");

        _bstrap = data;
        _omgr = omgr;
        _cloid = data.clientOid;

        _invdir.init(omgr, _cloid, this);
    }

    /**
     * Called every five seconds; ensures that we ping the server if we
     * haven't communicated in a long while.
     */
    protected function tick (event :TimerEvent) :void
    {
        if (_comm == null) {
            return;
        }

        var now :Number = new Date().getTime();
        if (now - _comm.getLastWrite() > PingRequest.PING_INTERVAL) {
            _comm.postMessage(new PingRequest());
        }
    }

    protected function gotClientObject (clobj :ClientObject) :void
    {
        _clobj = clobj;
        notifyObservers(ClientEvent.CLIENT_DID_LOGON);
    }

    protected function getClientObjectFailed (cause :Error) :void
    {
        notifyObservers(ClientEvent.CLIENT_FAILED_TO_LOGON, cause);
    }

    protected function clientObjectDidChange (clobj :ClientObject) :void
    {
        _clobj = clobj;
        _cloid = clobj.getOid();

        notifyObservers(ClientEvent.CLIENT_OBJECT_CHANGED);
    }

    internal function cleanup (logonError :Error) :void
    {
        // clear out our references
        _comm = null;
        _omgr = null;
        _clobj = null;
        _cloid = -1;

        // and let our invocation director know we're logged off
        _invdir.cleanup();

        // if this was due to a logon error, we can notify our listeners
        // now that we're cleaned up: they may want to retry logon on
        // another port, or something
        if (logonError != null) {
            notifyObservers(ClientEvent.CLIENT_FAILED_TO_LOGON, logonError);
        }
    }

    /**
     * Called by the omgr when we receive a pong packet.
     */
    public function gotPong (pong :PongResponse) :void
    {
        // TODO: compute time delta bowl-shit
    }

    /**
     * Convenience method to dispatch a client event to any listeners
     * and return the result of dispatchEvent.
     */
    public function notifyObservers (evtCode :String, cause :Error = null)
            :Boolean
    {
        return dispatchEvent(new ClientEvent(evtCode, this, cause));
    }

    /** The credentials we used to authenticate with the server. */
    protected var _creds :Credentials;

    /** The version string reported to the server at auth time. */
    protected var _version :String = "";

    /** The distributed object manager we're using during this session. */
    protected var _omgr :DObjectManager;

    /** The data associated with our authentication response. */
    protected var _authData :AuthResponseData;

    /** Our client distributed object id. */
    protected var _cloid :int = -1;

    /** Our client distributed object. */
    protected var _clobj :ClientObject;

    /** The game server host. */
    protected var _hostname :String;

    /** The port on which we connect to the game server. */
    protected var _port :int;

    /** The entity that manages our network communications. */
    protected var _comm :Communicator;

    /** General startup information provided by the server. */
    protected var _bstrap :BootstrapData;

    /** Manages invocation services. */
    protected var _invdir :InvocationDirector = new InvocationDirector();

    /** Ticks. */
    protected var _tickInterval :Timer;
}
}
