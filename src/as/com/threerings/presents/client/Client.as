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

import flash.display.Stage;

import flash.events.EventDispatcher;
import flash.events.TimerEvent;

import flash.utils.Timer;

import com.threerings.util.MethodQueue;
import com.threerings.util.ObserverList;

import com.threerings.presents.client.InvocationService_ConfirmListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;

import com.threerings.presents.dobj.DObjectManager;

import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.PingRequest;
import com.threerings.presents.net.PongResponse;

public class Client extends EventDispatcher
{
    /** The default port on which the server listens for client connections. */
    public static const DEFAULT_SERVER_PORTS :Array = [ 47624 ];

    private static const log :Log = Log.getLog(Client);

    public function Client (creds :Credentials, stage :Stage)
    {
        _creds = creds;
        _stage = stage;
        MethodQueue.setStage(stage);
    }

    /**
     * Registers the supplied observer with this client. While registered the observer will receive
     * notifications of state changes within the client. The function will refuse to register an
     * already registered observer.
     *
     * @see ClientObserver
     * @see SessionObserver
     */
    public function addClientObserver (observer :SessionObserver) :void
    {
        addEventListener(ClientEvent.CLIENT_WILL_LOGON, observer.clientWillLogon);
        addEventListener(ClientEvent.CLIENT_DID_LOGON, observer.clientDidLogon);
        addEventListener(ClientEvent.CLIENT_OBJECT_CHANGED, observer.clientObjectDidChange);
        addEventListener(ClientEvent.CLIENT_DID_LOGOFF, observer.clientDidLogoff);
        if (observer is ClientObserver) {
            var cliObs :ClientObserver = (observer as ClientObserver);
            addEventListener(ClientEvent.CLIENT_FAILED_TO_LOGON, cliObs.clientFailedToLogon);
            addEventListener(ClientEvent.CLIENT_CONNECTION_FAILED, cliObs.clientConnectionFailed);
            addEventListener(ClientEvent.CLIENT_WILL_LOGOFF, cliObs.clientWillLogoff);
            addEventListener(ClientEvent.CLIENT_DID_CLEAR, cliObs.clientDidClear);
        }
    }

    /**
     * Unregisters the supplied observer. Upon return of this function, the observer will no longer
     * receive notifications of state changes within the client.
     */
    public function removeClientObserver (observer :SessionObserver) :void
    {
        removeEventListener(ClientEvent.CLIENT_WILL_LOGON, observer.clientWillLogon);
        removeEventListener(ClientEvent.CLIENT_DID_LOGON, observer.clientDidLogon);
        removeEventListener(ClientEvent.CLIENT_OBJECT_CHANGED, observer.clientObjectDidChange);
        removeEventListener(ClientEvent.CLIENT_DID_LOGOFF, observer.clientDidLogoff);
        if (observer is ClientObserver) {
            var cliObs :ClientObserver = (observer as ClientObserver);
            removeEventListener(ClientEvent.CLIENT_FAILED_TO_LOGON, cliObs.clientFailedToLogon);
            removeEventListener(
                ClientEvent.CLIENT_CONNECTION_FAILED, cliObs.clientConnectionFailed);
            removeEventListener(ClientEvent.CLIENT_WILL_LOGOFF, cliObs.clientWillLogoff);
            removeEventListener(ClientEvent.CLIENT_DID_CLEAR, cliObs.clientDidClear);
        }
    }

    public function setServer (hostname :String, ports :Array) :void
    {
        _hostname = hostname;
        _ports = ports;
    }

    public function callLater (fn :Function, args :Array = null) :void
    {
        MethodQueue.callLater(fn, args);
    }

    /**
     * @return the Stage object we're living in.
     */
    public function getStage () :Stage
    {
        return _stage;
    }

    public function getHostname () :String
    {
        return _hostname;
    }

    /**
     * Returns the ports on which this client is configured to connect.
     */
    public function getPorts () :Array
    {
        return _ports;
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

    public function setVersion (version :String) :void
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

    /**
     * Returns the set of bootstrap service groups needed by this client.
     */
    public function getBootGroups () :Array
    {
        return _bootGroups;
    }

    /**
     * Marks this client as interested in the specified bootstrap services group. Any services
     * registered as bootstrap services with the supplied group name will be included in this
     * clients bootstrap services set. This must be called before {@link #logon}.
     */
    public function addServiceGroup (group :String) :void
    {
        if (isLoggedOn()) {
            throw new Error("Services must be registered prior to logon().");
        }
        if (_bootGroups.indexOf(group) == -1) {
            _bootGroups.push(group);
        }
    }

    public function getService (clazz :Class) :InvocationService
    {
        if (_bstrap != null) {
            for each (var isvc :InvocationService in _bstrap.services.asArray()) {
                if (isvc is clazz) {
                    return isvc;
                }
            }
        }

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
     * Requests that this client connect and logon to the server with which it was previously
     * configured.
     *
     * @return false if we're already logged on.
     */
    public function logon () :Boolean
    {
        // if we have a communicator, we're already logged on
        if (_comm != null) {
            return false;
        }

        // let our observers know that we're logging on (this will give directors a chance to
        // register invocation service groups)
        notifyObservers(ClientEvent.CLIENT_WILL_LOGON);

        // we need to wait for the CLIENT_WILL_LOGON to have been dispatched before we actually
        // tell the communicator to logon, so we run this through the callLater pipeline
        _comm = new Communicator(this);
        callLater(function () :void {
            _comm.logon();
        });

        // it is safe, however, to start up our tick interval immediately
        if (_tickInterval == null) {
            _tickInterval = new Timer(5000);
            _tickInterval.addEventListener(TimerEvent.TIMER, tick);
            _tickInterval.start();
        }

        return true;
    }

    /**
     * Transitions a logged on client from its current server to the specified new server.
     * Currently this simply logs the client off of its current server (if it is logged on) and
     * logs it onto the new server, but in the future we may aim to do something fancier.
     *
     * <p> If we fail to connect to the new server, the client <em>will not</em> be automatically
     * reconnected to the old server. It will be in a logged off state. However, it will be
     * reconfigured with the hostname and ports of the old server so that the caller can notify the
     * user of the failure and then simply call {@link #logon} to attempt to reconnect to the old
     * server.
     *
     * @param observer an observer that will be notified when we have successfully logged onto the
     * other server, or if the move failed.
     */
    public function moveToServer (hostname :String, ports :Array,
                                  obs :InvocationService_ConfirmListener) :void
    {
        // the server switcher will take care of everything for us
        new ServerSwitcher(this, hostname, ports, obs).switchServers();
    }

    /**
     * Requests that the client log off of the server to which it is connected.
     *
     * @param abortable if true, the client will call clientWillDisconnect on allthe client
     * observers and abort the logoff process if any of them return false. If false,
     * clientWillDisconnect will not be called.
     *
     * @return true if the logoff succeeded, false if it failed due to a disagreeable observer.
     */
    public function logoff (abortable :Boolean) :Boolean
    {
        if (_comm == null) {
            log.warning("Ignoring request to log off: not logged on.");
            return true;
        }

        // if the request is abortable, let's run it past the observers.  if any of them call
        // preventDefault() then the logoff will be cancelled
        if (abortable && !notifyObservers(ClientEvent.CLIENT_WILL_LOGOFF)) {
            return false;
        }

        _tickInterval.stop();
        _tickInterval = null;

        _comm.logoff();
        return true;
    }

    public function gotBootstrap (data :BootstrapData, omgr :DObjectManager) :void
    {
        // log.debug("Got bootstrap " + data + ".");

        _bstrap = data;
        _omgr = omgr;
        _cloid = data.clientOid;

        _invdir.init(omgr, _cloid, this);

        // log.debug("TimeBaseService: " + requireService(TimeBaseService));
    }

    /**
     * Called every five seconds; ensures that we ping the server if we haven't communicated in a
     * long while.
     */
    protected function tick (event :TimerEvent) :void
    {
        if (_comm == null) {
            return;
        }

        var now :uint = flash.utils.getTimer();
        if (now - _comm.getLastWrite() > PingRequest.PING_INTERVAL) {
            _comm.postMessage(new PingRequest());
        }
    }

    /**
     * Called by the {@link Communicator} if it is experiencing trouble logging on but is still
     * trying fallback strategies.
     */
    internal function reportLogonTribulations (cause :LogonError) :void
    {
        notifyObservers(ClientEvent.CLIENT_FAILED_TO_LOGON, cause);
    }

    /**
     * Called by the invocation director when it successfully subscribes to the client object
     * immediately following logon.
     */
    public function gotClientObject (clobj :ClientObject) :void
    {
        _clobj = clobj;
        notifyObservers(ClientEvent.CLIENT_DID_LOGON);
    }

    /**
     * Called by the invocation director if it fails to subscribe to the client object after logon.
     */
    public function getClientObjectFailed (cause :Error) :void
    {
        notifyObservers(ClientEvent.CLIENT_FAILED_TO_LOGON, cause);
    }

    /**
     * Called by the invocation director when it discovers that the client object has changed.
     */
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

        // if this was due to a logon error, we can notify our listeners now that we're cleaned up:
        // they may want to retry logon on another port, or something
        if (logonError != null) {
            notifyObservers(ClientEvent.CLIENT_FAILED_TO_LOGON, logonError);
        } else {
            notifyObservers(ClientEvent.CLIENT_DID_CLEAR, null);
        }
    }

    /**
     * Called by the omgr when we receive a pong packet.
     */
    public function gotPong (pong :PongResponse) :void
    {
        // TODO: compute time delta?
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

    /** The stage upon which our client runs. */
    protected var _stage :Stage;

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
    protected var _ports :Array; /* of int */

    /** The entity that manages our network communications. */
    protected var _comm :Communicator;

    /** Our list of client observers. */
//    protected var _observers :ObserverList =
//        new ObserverList(ObserverList.SAFE_IN_ORDER_NOTIFY);

    /** The set of bootstrap service groups this client cares about. */
    protected var _bootGroups :Array = new Array(InvocationCodes.GLOBAL_GROUP);

    /** General startup information provided by the server. */
    protected var _bstrap :BootstrapData;

    /** Manages invocation services. */
    protected var _invdir :InvocationDirector = new InvocationDirector();

    /** Ticks. */
    protected var _tickInterval :Timer;

    // client observer constants
    /*
    internal static const CLIENT_DID_LOGON :int = 0;
    internal static const CLIENT_FAILED_TO_LOGON :int = 1;
    internal static const CLIENT_OBJECT_CHANGED :int = 2;
    internal static const CLIENT_CONNECTION_FAILED :int = 3;
    internal static const CLIENT_WILL_LOGOFF :int = 4;
    internal static const CLIENT_DID_LOGOFF :int = 5;
    */
}
}
