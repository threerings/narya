//
// $Id: ZoneObserver.java,v 1.2 2001/12/17 04:11:40 mdb Exp $

package com.threerings.whirled.zone.client;

import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * The zone observer interface makes it possible for entities to be
 * notified when the client moves to a new zone.
 */
public interface ZoneObserver
{
    /**
     * Called when we begin the process of switching to a new zone. This
     * will be followed by a call to {@link #zoneDidChange} to indicate
     * that the change was successful or {@link #zoneChangeFailed} if the
     * change fails.
     *
     * @param zoneId the zone id of the zone to which we are changing.
     */
    public void zoneWillChange (int zoneId);

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
