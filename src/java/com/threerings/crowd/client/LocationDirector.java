//
// $Id: LocationDirector.java,v 1.6 2001/08/04 03:06:39 mdb Exp $

package com.threerings.cocktail.party.client;

import java.util.ArrayList;
import java.util.List;

import com.threerings.cocktail.cher.client.*;
import com.threerings.cocktail.cher.dobj.*;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.data.BodyObject;
import com.threerings.cocktail.party.data.PlaceObject;
import com.threerings.cocktail.party.util.PartyContext;

/**
 * The location manager provides a means by which entities on the client
 * can request to move from place to place and can be notified if other
 * entities have caused the client to move to a new place. It also
 * provides a mechanism for ratifying a request to move to a new place
 * before actually issuing the request.
 */
public class LocationManager
    implements ClientObserver, Subscriber
{
    public LocationManager (PartyContext ctx)
    {
        // keep this around for later
        _ctx = ctx;

        // register ourselves as a client observer
        ctx.getClient().addObserver(this);
    }

    /**
     * Adds a location observer to the list. This observer will
     * subsequently be notified of potential, effected and failed location
     * changes.
     */
    public void addLocationObserver (LocationObserver observer)
    {
        _observers.add(observer);
    }

    /**
     * Removes a location observer from the list.
     */
    public void removeLocationObserver (LocationObserver observer)
    {
        _observers.remove(observer);
    }

    /**
     * Requests that this client be moved to the specified place. A
     * request will be made and when the response is received, the
     * location observers will be notified of success or failure.
     */
    public void moveTo (int placeId)
    {
        // first check to see if our observers are happy with this move
        // request
        for (int i = 0; i < _observers.size(); i++) {
            LocationObserver obs = (LocationObserver)_observers.get(i);
            if (!obs.locationMayChange(placeId)) {
                Log.info("Location change vetoed by observer " +
                         "[pid=" + placeId + ", obs=" + obs + "].");
                return;
            }
        }

        // complain if we're over-writing a pending request
        if (_pendingPlaceId != -1) {
            Log.warning("We appear to have a moveTo request outstanding " +
                        "[ppid=" + _pendingPlaceId +
                        ", npid=" + placeId + "].");
            // but we're going to fall through and do it anyway because
            // refusing to switch rooms at this point will inevitably
            // result in some strange bug causing a move request to be
            // dropped by the server and the client that did it to be
            // totally hosed because they can no longer move to new
            // locations because they still have an outstanding request
        }

        // make a note of our pending place id
        _pendingPlaceId = placeId;

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
    public void handleMoveSucceeded (int invid)
    {
        DObjectManager omgr = _ctx.getDObjectManager();

        // unsubscribe from our old place object
        if (_place != null) {
            omgr.unsubscribeFromObject(_place.getOid(), this);
            _place = null;
        }

        // make a note that we're now mostly in the new location
        _previousPlaceId = _placeId;
        _placeId = _pendingPlaceId;
        _pendingPlaceId = -1;

        // subscribe to our new place object to complete the move
        omgr.subscribeToObject(_placeId, this);
    }

    /**
     * Called in response to a failed <code>moveTo</code> request.
     */
    public void handleMoveFailed (int invid, String reason)
    {
        // clear out our pending request oid
        int placeId = _pendingPlaceId;
        _pendingPlaceId = -1;

        // let our observers know that something has gone horribly awry
        notifyFailure(placeId, reason);
    }

    public void objectAvailable (DObject object)
    {
        // yay, we have our new place object
        _place = (PlaceObject)object;

        // let our observers know that all is well on the western front
        for (int i = 0; i < _observers.size(); i++) {
            LocationObserver obs = (LocationObserver)_observers.get(i);
            obs.locationDidChange(_place);
        }
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        // aiya! we were unable to fetch our new place object; something
        // is badly wrong
        Log.warning("Aiya! Unable to fetch place object for new location " +
                    "[plid=" + oid + ", reason=" + cause + "].");

        // clear out our half initialized place info
        int placeId = _placeId;
        _placeId = -1;

        // let the kids know shit be fucked
        notifyFailure(placeId, "m.unable_to_fetch_place_object");

        // if we were previously somewhere, try going back there
        if (_previousPlaceId != -1) {
            moveTo(_previousPlaceId);
        }
    }

    public boolean handleEvent (DEvent event, DObject target)
    {
        // nothing to do here, but remain subscribed
        return true;
    }

    protected void notifyFailure (int placeId, String reason)
    {
        for (int i = 0; i < _observers.size(); i++) {
            LocationObserver obs = (LocationObserver)_observers.get(i);
            obs.locationChangeFailed(placeId, reason);
        }
    }

    /** The context through which we access needed services. */
    protected PartyContext _ctx;

    /** Our location observer list. */
    protected List _observers = new ArrayList();

    /** The oid of the place we currently occupy. */
    protected int _placeId = -1;

    /** The place object that we currently occupy. */
    protected PlaceObject _place;

    /**
     * The oid of the place for which we have an outstanding moveTo
     * request, or -1 if we have no outstanding request.
     */
    protected int _pendingPlaceId = -1;

    /** The oid of the place we previously occupied. */
    protected int _previousPlaceId = -1;
}
