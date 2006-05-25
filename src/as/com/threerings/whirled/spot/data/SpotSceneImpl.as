//
// $Id: SpotSceneImpl.java 3451 2005-03-31 19:40:55Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.whirled.spot.data {

import com.threerings.util.ArrayIterator;
import com.threerings.util.Iterator;
import com.threerings.util.HashMap;

/**
 * An implementation of the {@link SpotScene} interface.
 */
public class SpotSceneImpl
    implements SpotScene
{
    /**
     * Creates an instance that will obtain data from the supplied spot
     * scene model.
     */
    public function SpotSceneImpl (model :SpotSceneModel = null)
    {
        if (model != null) {
            _smodel = model;
            readPortals();

        } else {
            _smodel = new SpotSceneModel();
        }
    }

    protected function readPortals () :void
    {
        _portals.clear();
        for each (var port :Portal in _smodel.portals) {
            _portals.put(port.portalId, port);
        }
    }

    // documentation inherited from interface
    public function getPortal (portalId :int) :Portal
    {
        return (_portals.get(portalId) as Portal);
    }

    // documentation inherited from interface
    public function getPortalCount () :int
    {
        return _portals.size();
    }

    // documentation inherited from interface
    public function getPortals () :Iterator
    {
        return new ArrayIterator(_portals.values());
    }

    // documentation inherited from interface
    public function getNextPortalId () :int
    {
        // compute a new portal id for our friend the portal
        for (var ii :int = 1; ii < MAX_PORTAL_ID; ii++) {
            if (!_portals.containsKey(ii)) {
                return ii;
            }
        }
        return -1;
    }

    // documentation inherited from interface
    public function getDefaultEntrance () :Portal
    {
        return getPortal(_smodel.defaultEntranceId);
    }

    // documentation inherited from interface
    public function addPortal (portal :Portal) :void
    {
        if (portal.portalId <= 0) {
            Log.getLog(this).warning("Refusing to add zero-id portal " +
                "[scene=" + this + ", portal=" + portal + "].");
            return;
        }

        // add it to our model
        _smodel.addPortal(portal);

        // and slap it into our table
        _portals.put(portal.portalId, portal);
    }

    // documentation inherited from interface
    public function removePortal (portal :Portal) :void
    {
        // remove the portal from our mapping
        _portals.remove(portal.portalId);

        // remove it from the model
        _smodel.removePortal(portal);
    }

    /**
     * Used when we're being parsed from an XML scene model.
     */
    public function setDefaultEntranceId (defaultEntranceId :int) :void
    {
        _smodel.defaultEntranceId = defaultEntranceId;
    }

    // documentation inherited from interface
    public function setDefaultEntrance (portal :Portal) :void
    {
        _smodel.defaultEntranceId = (portal == null) ? -1 : portal.portalId;
    }

    /**
     * This should be called if a scene update was received that caused
     * our underlying scene model to change.
     */
    public function updateReceived () :void
    {
        readPortals();
    }

    /** A casted reference to our scene model. */
    protected var _smodel :SpotSceneModel;

    /** A mapping from portal id to portal. */
    protected var _portals :HashMap = new HashMap();

    /** We don't allow more than ~32k portals in a scene. Things would
     * slow down *way* before we got there. */
    protected static const MAX_PORTAL_ID :int = int(Math.pow(2, 15) - 1);
}
}
