//
// $Id: ZoneManager.java,v 1.2 2001/12/13 05:49:50 mdb Exp $

package com.threerings.whirled.zone.server;

import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * A zone is a collection of scenes organized into a connected group. A
 * user can wander around within a zone, moving from scene to scene via
 * the standard mechanisms. To move between zones, they must use a special
 * mechanism (like at the dock, they can move from an island zone into
 * their ship zone; or they can move from an island zone into their house
 * zone). A zone provides scene summary information that can be used to
 * display a map of the zone to the client.
 */
public interface ZoneManager
{
    /**
     * Used to notify requesters when an asynchronous zone load has
     * completed (successfully or not).
     */
    public static interface ResolutionListener
    {
        /**
         * Called when a zone was successfully resolved.
         */
        public void zoneWasResolved (ZoneSummary summary);

        /**
         * Called when a zone failed to resolve.
         */
        public void zoneFailedToResolve (int zoneId, Exception reason);
    }

    /**
     * Resolves and delivers the scene summary information for the
     * requested zone. Zone resolution is an asynchronous process, which
     * necessitates this callback-style interface.
     *
     * @param zoneId the qualified zone id of the zone to resolve.
     * @param listener the listener that should be notified when the zone
     * is successfully resolved or is known to have failed to resolve.
     */
    public void resolveZone (int zoneId, ResolutionListener listener);
}
