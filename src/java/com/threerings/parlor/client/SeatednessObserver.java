//
// $Id: SeatednessObserver.java,v 1.1 2001/10/23 20:23:29 mdb Exp $

package com.threerings.parlor.client;

/**
 * Entites that wish to hear about when we sit down at a table or stand up
 * from a table can implement this interface and register themselves with
 * the {@link TableManager}.
 */
public interface SeatednessObserver
{
    /**
     * Called when this client sits down at or stands up from a table.
     *
     * @param isSeated true if the client is now seated at a table, false
     * if they are now no longer seated at a table.
     */
    public void seatednessDidChange (boolean isSeated);
}
