//
// $Id: TableObserver.java,v 1.2 2001/10/26 01:40:22 mdb Exp $

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
