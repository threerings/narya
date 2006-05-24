//
// $Id: SpotScene.java 3451 2005-03-31 19:40:55Z mdb $
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

import com.threerings.util.Iterator;

/**
 * Makes available the spot scene information that the server needs to do
 * its business.
 */
public interface SpotScene
{
    /**
     * Returns a {@link Portal} object for the portal with the specified
     * id or null if no portal exists with that id.
     */
    function getPortal (portalId :int) :Portal;

    /**
     * Returns the number of portals in this scene.
     */
    function getPortalCount () :int;

    /**
     * Returns an iterator over the portals in this scene.
     */
    function getPortals () :Iterator;

    /**
     * Returns the portal id that should be assigned to the next portal
     * added to this scene.
     */
    function getNextPortalId () :int;

    /**
     * Returns the portal that represents the default entrance to this
     * scene. If a body enters the scene at logon time rather than
     * entering from some other scene, this is the portal at which they
     * would appear.
     */
    function getDefaultEntrance () :Portal;

    /**
     * Adds a portal to this scene, immediately making the requisite
     * modifications to the underlying scene model. The portal id should
     * have already been assigned using the value obtained from {@link
     * #getNextPortalId}.
     */
    function addPortal (portal :Portal) :void;

    /**
     * Removes the specified portal from the scene.
     */
    function removePortal (portal :Portal) :void;

    /**
     * Sets the default entrance in this scene, immediately making the
     * requisite modifications to the underlying scene model.
     */
    function setDefaultEntrance (portal :Portal) :void;
}
}
