//
// $Id: SpotSceneObject.java 3300 2005-01-08 22:05:00Z ray $
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

import com.threerings.presents.dobj.DSet;
import com.threerings.whirled.data.SceneObject;

/**
 * Extends the {@link SceneObject} with information specific to spots.
 */
public class SpotSceneObject extends SceneObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>occupantLocs</code> field. */
    public static const OCCUPANT_LOCS :String = "occupantLocs";

    /** The field name of the <code>clusters</code> field. */
    public static const CLUSTERS :String = "clusters";
    // AUTO-GENERATED: FIELDS END

    /** A distributed set containing {@link SceneLocation} records for all
     * occupants of this scene. */
    public var occupantLocs :DSet = new DSet();

    /** Contains information on all {@link Cluster}s in this scene. */
    public var clusters :DSet = new DSet();

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the
     * <code>occupantLocs</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public function addToOccupantLocs (elem :DSet_Entry) :void
    {
        requestEntryAdd(OCCUPANT_LOCS, occupantLocs, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>occupantLocs</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public function removeFromOccupantLocs (key :Object) :void
    {
        requestEntryRemove(OCCUPANT_LOCS, occupantLocs, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>occupantLocs</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public function updateOccupantLocs (elem :DSet_Entry) :void
    {
        requestEntryUpdate(OCCUPANT_LOCS, occupantLocs, elem);
    }

    /**
     * Requests that the <code>occupantLocs</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public function setOccupantLocs (value :DSet) :void
    {
        requestAttributeChange(OCCUPANT_LOCS, value, this.occupantLocs);
        this.occupantLocs = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>clusters</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public function addToClusters (elem :DSet_Entry) :void
    {
        requestEntryAdd(CLUSTERS, clusters, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>clusters</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public function removeFromClusters (key :Object) :void
    {
        requestEntryRemove(CLUSTERS, clusters, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>clusters</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public function updateClusters (elem :DSet_Entry) :void
    {
        requestEntryUpdate(CLUSTERS, clusters, elem);
    }

    /**
     * Requests that the <code>clusters</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public function setClusters (value :DSet) :void
    {
        requestAttributeChange(CLUSTERS, value, this.clusters);
        this.clusters = value;
    }
    // AUTO-GENERATED: METHODS END
}
}
