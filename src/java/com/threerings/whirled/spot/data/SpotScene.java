//
// $Id$
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

package com.threerings.whirled.spot.data;

import java.util.Iterator;

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
    public Portal getPortal (int portalId);

    /**
     * Returns the number of portals in this scene.
     */
    public int getPortalCount ();

    /**
     * Returns an iterator over the portals in this scene.
     */
    public Iterator getPortals ();

    /**
     * Returns the portal id that should be assigned to the next portal
     * added to this scene.
     */
    public short getNextPortalId ();

    /**
     * Returns the portal that represents the default entrance to this
     * scene. If a body enters the scene at logon time rather than
     * entering from some other scene, this is the portal at which they
     * would appear.
     */
    public Portal getDefaultEntrance ();

    /**
     * Adds a portal to this scene, immediately making the requisite
     * modifications to the underlying scene model. The portal id should
     * have already been assigned using the value obtained from {@link
     * #getNextPortalId}.
     */
    public void addPortal (Portal portal);

    /**
     * Removes the specified portal from the scene.
     */
    public void removePortal (Portal portal);

    /**
     * Sets the default entrance in this scene, immediately making the
     * requisite modifications to the underlying scene model.
     */
    public void setDefaultEntrance (Portal portal);
}
