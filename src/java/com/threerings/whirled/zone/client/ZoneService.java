//
// $Id: ZoneService.java,v 1.2 2001/12/17 00:56:19 mdb Exp $

package com.threerings.whirled.zone.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationDirector;

import com.threerings.whirled.zone.Log;

/**
 * The zone service class provides the client interface to the zone
 * related invocation services (e.g. moving between zones).
 */
public class ZoneService implements ZoneCodes
{
    /**
     * Requests that that this client's body be moved to the specified
     * scene in the specified zone.
     *
     * @param zoneId the zone id to which we want to move.
     * @param sceneId the scene id to which we want to move.
     * @param sceneVers the version number of the scene object that we
     * have in our local repository.
     */
    public static void moveTo (Client client, int zoneId, int sceneId,
                               int sceneVers, ZoneDirector rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] {
            new Integer(zoneId), new Integer(sceneId), new Integer(sceneVers) };
        invdir.invoke(MODULE_NAME, MOVE_TO_REQUEST, args, rsptarget);
        Log.info("Sent moveTo request [zone=" + zoneId +
                 ", scene=" + sceneId + ", version=" + sceneVers + "].");
    }
}
