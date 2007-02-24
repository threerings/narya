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
