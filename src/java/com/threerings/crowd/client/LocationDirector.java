//
// $Id: LocationDirector.java,v 1.3 2001/07/23 21:14:27 mdb Exp $

package com.threerings.cocktail.party.client;

import com.threerings.cocktail.cher.client.*;
import com.threerings.cocktail.cher.dobj.*;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.data.BodyObject;
import com.threerings.cocktail.party.util.PartyContext;

/**
 * The location manager provides a means by which entities on the client
 * can request to move from place to place and can be notified if other
 * entities have caused the client to move to a new place. It also
 * provides a mechanism for ratifying a request to move to a new place
 * before actually issuing the request.
 */
public class LocationManager
    implements ClientObserver
{
    public LocationManager (PartyContext ctx)
    {
        // keep this around for later
        _ctx = ctx;

        // register ourselves as a client observer
        ctx.getClient().addObserver(this);
    }

    /**
     * Requests that this client be moved to the specified place. A
     * request will be made and when the response is received, the
     * location observers will be notified of success or failure.
     */
    public void moveTo (int placeId)
    {
        // issue a moveTo request
        LocationService.moveTo(_ctx.getClient(), placeId, this);
    }

    public void clientDidLogon (Client client)
    {
        // get a copy of our body object
        Subscriber sub = new Subscriber() {
            public void objectAvailable (DObject object)
            {
                gotBodyObject((BodyObject)object);
            }

            public void requestFailed (int oid, ObjectAccessException cause)
            {
                Log.warning("Location manager unable to fetch body " +
                            "object; all has gone horribly wrong" +
                            "[cause=" + cause + "].");
            }

            public boolean handleEvent (DEvent event, DObject target)
            {
                return false;
            }
        };
        int cloid = client.getClientOid();
        client.getDObjectManager().subscribeToObject(cloid, sub);
    }

    protected void gotBodyObject (BodyObject clobj)
    {
        // check to see if we are already in a location, in which case
        // we'll want to be going there straight away
    }

    public void clientFailedToLogon (Client client, Exception cause)
    {
        // we're fair weather observers. we do nothing until logon
        // succeeds
    }

    public void clientConnectionFailed (Client client, Exception cause)
    {
        // nothing doing
    }

    public boolean clientWillLogoff (Client client)
    {
        // we have no objections
        return true;
    }

    public void clientDidLogoff (Client client)
    {
        // nothing doing
    }

    /**
     * Called in response to a successful <code>moveTo</code> request.
     */
    public void handleMoveSucceeded ()
    {
        Log.info("Move succeeded.");
    }

    /**
     * Called in response to a failed <code>moveTo</code> request.
     */
    public void handleMoveFailed (String reason)
    {
        Log.info("Move failed [reason=" + reason + "].");
    }

    protected PartyContext _ctx;
}
