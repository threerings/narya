//
// $Id: TableLobbyObject.java,v 1.2 2003/02/26 17:54:56 mdb Exp $

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
