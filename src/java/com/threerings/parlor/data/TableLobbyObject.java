//
// $Id: TableLobbyObject.java,v 1.3 2004/08/27 02:20:13 mdb Exp $
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

package com.threerings.parlor.data;

import com.threerings.presents.dobj.DSet;

/**
 * This interface must be implemented by the place object used by a lobby
 * that wishes to make use of the table services.
 */
public interface TableLobbyObject
{
    /**
     * Returns a reference to the distributed set instance that will be
     * holding the tables.
     */
    public DSet getTables ();

    /**
     * Adds the supplied table instance to the tables set (using the
     * appropriate distributed object mechanisms).
     */
    public void addToTables (Table table);

    /**
     * Updates the value of the specified table instance in the tables
     * distributed set (using the appropriate distributed object
     * mechanisms).
     */
    public void updateTables (Table table);

    /**
     * Removes the table instance that matches the specified key from the
     * tables set (using the appropriate distributed object mechanisms).
     */
    public void removeFromTables (Comparable key);
}
