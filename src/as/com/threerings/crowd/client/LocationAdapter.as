package com.threerings.crowd.client {

import com.threerings.crowd.data.PlaceObject;

public class LocationAdapter
    implements LocationObserver
{
    public function LocationAdapter (
            mayChange :Function = null, didChange :Function = null,
            changeFailed :Function = null)
    {
        _mayChange = mayChange;
        _didChange = didChange;
        _changeFailed = changeFailed;
    }

    // documentation inherited from interface LocationObserver
    public function locationMayChange (placeId :int) :Boolean
    {
        return (_mayChange == null) || _mayChange(placeId);
    }

    // documentation inherited from interface LocationObserver
    public function locationDidChange (place :PlaceObject) :void
    {
        if (_didChange != null) {
            _didChange(place);
        }
    }

    // documentation inherited from interface LocationObserver
    public function locationChangeFailed (placeId :int, reason :String) :void
    {
        if (_changeFailed != null) {
            _changeFailed(placeId, reason);
        }
    }

    protected var _mayChange :Function;
    protected var _didChange :Function;
    protected var _changeFailed :Function;
}
}
