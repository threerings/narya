//
// $Id: ZoneObserver.java,v 1.1 2001/12/17 04:00:27 mdb Exp $

package com.threerings.whirled.zone.client;

import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * The zone observer interface makes it possible for entities to be
 * notified when the client moves to a new zone.
 */
public interface ZoneObserver
{
    /**
     * Called when we have switched to a new zone.
     *
     * @param summary the summary information for the new zone or null if
     * we have switched to no zone.
     */
    public void zoneDidChange (ZoneSummary summary);

    /**
     * This is called on all zone observers when a zone change request is
     * rejected by the server or fails for some other reason.
     *
     * @param reason the reason code that explains why the zone change
     * request was rejected or otherwise failed.
     */
    public void zoneChangeFailed (String reason);
}
