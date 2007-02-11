package com.threerings.presents.client {

public class ClientAdapter
    implements ClientObserver
{
    public function ClientAdapter (
            clientWillLogon :Function = null,
            clientDidLogon :Function = null,
            clientObjectDidChange :Function = null,
            clientDidLogoff :Function = null,
            clientFailedToLogon :Function = null,
            clientConnectionFailed :Function = null,
            clientWillLogoff :Function = null,
            clientDidClear :Function = null)
    {
        _clientWillLogon = clientWillLogon;
        _clientDidLogon = clientDidLogon;
        _clientObjectDidChange = clientObjectDidChange;
        _clientDidLogoff = clientDidLogoff;
        _clientFailedToLogon = clientFailedToLogon;
        _clientConnectionFailed = clientConnectionFailed;
        _clientWillLogoff = clientWillLogoff;
        _clientDidClear = clientDidClear;
    }

    // from ClientObserver
    public function clientWillLogon (event :ClientEvent) :void
    {
        if (_clientWillLogon != null) {
            _clientWillLogon(event);
        }
    }

    // from ClientObserver
    public function clientDidLogon (event :ClientEvent) :void
    {
        if (_clientDidLogon != null) {
            _clientDidLogon(event);
        }
    }

    // from ClientObserver
    public function clientObjectDidChange (event :ClientEvent) :void
    {
        if (_clientObjectDidChange != null) {
            _clientObjectDidChange(event);
        }
    }

    // from ClientObserver
    public function clientDidLogoff (event :ClientEvent) :void
    {
        if (_clientDidLogoff != null) {
            _clientDidLogoff(event);
        }
    }

    // from ClientObserver
    public function clientFailedToLogon (event :ClientEvent) :void
    {
        if (_clientFailedToLogon != null) {
            _clientFailedToLogon(event);
        }
    }

    // from ClientObserver
    public function clientConnectionFailed (event :ClientEvent) :void
    {
        if (_clientConnectionFailed != null) {
            _clientConnectionFailed(event);
        }
    }

    // from ClientObserver
    public function clientWillLogoff (event :ClientEvent) :void
    {
        if (_clientWillLogoff != null) {
            _clientWillLogoff(event);
        }
    }

    // from ClientObserver
    public function clientDidClear (event :ClientEvent) :void
    {
        if (_clientDidClear != null) {
            _clientDidClear(event);
        }
    }

    protected var _clientWillLogon :Function;
    protected var _clientDidLogon :Function;
    protected var _clientObjectDidChange :Function;
    protected var _clientDidLogoff :Function;
    protected var _clientFailedToLogon :Function;
    protected var _clientConnectionFailed :Function;
    protected var _clientWillLogoff :Function;
    protected var _clientDidClear :Function;
}
}
