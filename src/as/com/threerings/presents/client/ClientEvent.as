package com.threerings.presents.client {

import flash.events.Event;

public class ClientEvent extends Event
{
    public static const CLIENT_DID_LOGON :String = "clientDidLogon";
    public static const CLIENT_FAILED_TO_LOGON :String = "clientFailedLogon";
    public static const CLIENT_OBJECT_CHANGED :String = "clobjChanged";
    public static const CLIENT_CONNECTION_FAILED :String = "clientConnFailed";
    /** The logoff itself can be cancelled if a listener calls
     * preventDefault() on this event. */
    public static const CLIENT_WILL_LOGOFF :String = "clientWillLogoff";
    public static const CLIENT_DID_LOGOFF :String = "clientDidLogoff";
    public static const CLIENT_DID_CLEAR :String = "clientDidClear";

    public function ClientEvent (type :String, client :Client,
            cause :Error = null)
    {
        super(type, false, (type === CLIENT_WILL_LOGOFF));
        _client = client;
        _cause = cause;
    }

    public function getClient () :Client
    {
        return _client;
    }

    public function getCause () :Error
    {
        return _cause;
    }

    override public function clone () :Event
    {
        return new ClientEvent(type, _client, _cause);
    }

    /** The client that generated this client event. */
    protected var _client :Client;

    /** The error that caused this event, if applicable. */
    protected var _cause :Error;
}
}
