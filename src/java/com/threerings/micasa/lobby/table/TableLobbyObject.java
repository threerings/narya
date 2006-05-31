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

package com.threerings.micasa.lobby.table;

import com.threerings.presents.dobj.DSet;
import com.threerings.parlor.data.Table;
import com.threerings.micasa.lobby.LobbyObject;

public class TableLobbyObject
    extends LobbyObject
    implements com.threerings.parlor.data.TableLobbyObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>tableSet</code> field. */
    public static final String TABLE_SET = "tableSet";
    // AUTO-GENERATED: FIELDS END

    /** A set containing all of the tables being managed by this lobby. */
    public DSet tableSet = new DSet();

    // documentation inherited
    public DSet getTables ()
    {
        return tableSet;
    }

    // documentation inherited from interface
    public void addToTables (Table table)
    {
        addToTableSet(table);
    }

    // documentation inherited from interface
    public void updateTables (Table table)
    {
        updateTableSet(table);
    }

    // documentation inherited from interface
    public void removeFromTables (Comparable key)
    {
        removeFromTableSet(key);
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the
     * <code>tableSet</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToTableSet (DSet.Entry elem)
    {
        requestEntryAdd(TABLE_SET, tableSet, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>tableSet</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromTableSet (Comparable key)
    {
        requestEntryRemove(TABLE_SET, tableSet, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>tableSet</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateTableSet (DSet.Entry elem)
    {
        requestEntryUpdate(TABLE_SET, tableSet, elem);
    }

    /**
     * Requests that the <code>tableSet</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setTableSet (DSet value)
    {
        requestAttributeChange(TABLE_SET, value, this.tableSet);
        this.tableSet = (value == null) ? null : value.typedClone();
    }
    // AUTO-GENERATED: METHODS END
}
