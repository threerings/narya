//
// $Id: OccupantObserver.java,v 1.5 2004/08/27 02:12:33 mdb Exp $
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

import com.threerings.crowd.data.OccupantInfo;

/**
 * An entity that is interested in hearing about bodies that enter and
 * leave a location (as well as disconnect and reconnect) can implement
 * this interface and register itself with the {@link OccupantDirector}.
 */
public interface OccupantObserver
{
    /**
     * Called when a body enters the place.
     */
    public void occupantEntered (OccupantInfo info);

    /**
     * Called when a body leaves the place.
     */
    public void occupantLeft (OccupantInfo info);

    /**
     * Called when an occupant is updated.
     *
     * @param oldinfo the occupant info prior to the update.
     * @param newinfo the newly update info record.
     */
    public void occupantUpdated (OccupantInfo oldinfo, OccupantInfo newinfo);
}
