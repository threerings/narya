//
// $Id: OccupantAdapter.java,v 1.2 2004/08/27 02:12:33 mdb Exp $
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
 * The occupant adapter makes life easier for occupant observer classes
 * that only care about one or two of the occupant observer
 * callbacks. They can either extend occupant adapter or create an
 * anonymous class that extends it and overrides just the callbacks they
 * care about.
 */
public class OccupantAdapter implements OccupantObserver
{
    // documentation inherited from interface
    public void occupantEntered (OccupantInfo info)
    {
    }

    // documentation inherited from interface
    public void occupantLeft (OccupantInfo info)
    {
    }

    // documentation inherited from interface
    public void occupantUpdated (OccupantInfo oinfo, OccupantInfo info)
    {
    }
}
