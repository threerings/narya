//
// $Id: TableObserver.java,v 1.3 2004/08/27 02:20:12 mdb Exp $
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

package com.threerings.parlor.client;

import com.threerings.parlor.data.Table;

/**
 * The {@link TableDirector} converts distributed object events into
 * higher level callbacks to implementers of this interface, which are
 * expected to render these events sensically in a user interface.
 */
public interface TableObserver
{
    /**
     * Called when a new table is created.
     */
    public void tableAdded (Table table);

    /**
     * Called when something has changed about a table (occupant list
     * updated, state changed from matchmaking to in-play, etc.).
     */
    public void tableUpdated (Table table);

    /**
     * Called when a table goes away.
     */
    public void tableRemoved (int tableId);
}
