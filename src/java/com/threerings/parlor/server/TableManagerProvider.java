//
// $Id: TableManagerProvider.java,v 1.1 2001/10/23 23:47:02 mdb Exp $

package com.threerings.parlor.server;

/**
 * A place manager that wishes to provide table matchmaking services in
 * its place needs to create a table manager and make it available by
 * implementing this interface. The table invocation services and the
 * table manager will take care of the rest.
 */
public interface TableManagerProvider
{
    /**
     * Returns a reference to the table manager that is responsible for
     * table management in this lobby.
     */
    public TableManager getTableManager ();
}
