//
// $Id: LocationAdapter.java,v 1.3 2004/08/27 02:12:32 mdb Exp $
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

package com.threerings.crowd.client;

import com.threerings.crowd.data.PlaceObject;

/**
 * The location adapter makes life easier for a class that really only
 * cares about one or two of the location observer callbacks and doesn't
 * want to provide empty implementations of the others. One can either
 * extend location adapter, or create an anonymous instance that overrides
 * the desired callback(s). Note that the location adapter defaults to
 * ratifying any location change.
 *
 * @see LocationObserver
 */
public class LocationAdapter implements LocationObserver
{
    // documentation inherited
    public boolean locationMayChange (int placeId)
    {
        return true;
    }

    // documentation inherited
    public void locationDidChange (PlaceObject place)
    {
    }

    // documentation inherited
    public void locationChangeFailed (int placeId, String reason)
    {
    }
}
