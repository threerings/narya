//
// $Id: PlaceConfig.java,v 1.8 2004/08/27 02:12:33 mdb Exp $
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

package com.threerings.crowd.data;

import com.samskivert.util.StringUtil;

import com.threerings.io.TrackedStreamableObject;

/**
 * The place config class encapsulates the configuration information for a
 * particular type of place. The hierarchy of place config objects mimics
 * the hierarchy of place managers and controllers. Both the place manager
 * and place controller are provided with the place config object when the
 * place is created.
 *
 * <p> The place config object is also the mechanism used to instantiate
 * the appropriate place manager and controller. Every place must have an
 * associated place config derived class that overrides {@link
 * #getControllerClass} and {@link #getManagerClassName}, returning the
 * appropriate place controller and manager class for that place.
 */
public abstract class PlaceConfig extends TrackedStreamableObject
{
    /**
     * Returns the class that should be used to create a controller for
     * this place. The controller class must derive from {@link
     * PlaceController}.
     */
    public abstract Class getControllerClass ();

    /**
     * Returns the name of the class that should be used to create a
     * manager for this place. The manager class must derive from {@link
     * com.threerings.crowd.server.PlaceManager}. <em>Note:</em> this
     * method differs from {@link #getControllerClass} because we want to
     * avoid compile time linkage of the place config object (which is
     * used on the client) to server code. This allows a code optimizer
     * (DashO Pro, for example) to remove the server code from the client,
     * knowing that it is never used.
     */
    public abstract String getManagerClassName ();

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("type=").append(StringUtil.shortClassName(this));
        buf.append(", ");
        super.toString(buf);
    }
}
