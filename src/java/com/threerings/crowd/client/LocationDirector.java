//
// $Id: LocationDirector.java,v 1.14 2001/10/24 03:10:30 mdb Exp $

package com.threerings.crowd.client;

import java.util.ArrayList;
import java.util.List;

import com.threerings.presents.client.*;
import com.threerings.presents.dobj.*;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.*;
import com.threerings.crowd.util.CrowdContext;

/**
 * The location director provides a means by which entities on the client
 * can request to move from place to place and can be notified if other
 * entities have caused the client to move to a new place. It also
 * provides a mechanism for ratifying a request to move to a new place
 * before actually issuing the request.
 */
public class LocationDirector
    implements ClientObserver, Subscriber
{
    public LocationDirector (CrowdContext ctx)
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
     * Returns the place object for the location we currently occupy or
     * null if we're not currently occupying any location.
     */
    public PlaceObject getPlaceObject ()
    {
        return _plobj;
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
        if (!mayMoveTo(placeId)) {
            return;
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

    /**
     * Requests to move to the room that we last occupied, if such a room
     * exists.
     *
     * @return true if we had a previous room and we requested to move to
     * it, false if we had no previous room.
     */
    public boolean moveBack ()
    {
        if (_previousPlaceId == -1) {
            return false;

        } else {
            moveTo(_previousPlaceId);
            return true;
        }
    }

    /**
     * This can be called by derived classes that need to coopt the moving
     * process to extend it in some way or other. In such situations, they
     * should call this method before moving to a new location to check to
     * be sure that all of the registered location observers are amenable
     * to a location change.
     *
     * @param placeId the place oid of our tentative new location.
     *
     * @return true if everyone is happy with the move, false if it was
     * vetoed by one of the location observers.
     */
    protected boolean mayMoveTo (int placeId)
    {
        for (int i = 0; i < _observers.size(); i++) {
            LocationObserver obs = (LocationObserver)_observers.get(i);
            if (!obs.locationMayChange(placeId)) {
                Log.info("Location change vetoed by observer " +
                         "[pid=" + placeId + ", obs=" + obs + "].");
                return false;
            }
        }

        return true;
    }

    /**
     * This can be called by derived classes that need to coopt the moving
     * process to extend it in some way or other. In such situations, they
     * will be responsible for receiving the successful move response and
     * they should let the location director know that the move has been
     * effected.
     *
     * @param placeId the place oid of our new location.
     * @param config the configuration information for the new place.
     */
    protected void didMoveTo (int placeId, PlaceConfig config)
    {
        DObjectManager omgr = _ctx.getDObjectManager();

        // do some cleaning up if we were previously in a place
        if (_plobj != null) {
            // let the old controller know that things are going away
            if (_controller != null) {
                try {
                    _controller.didLeavePlace(_plobj);
                } catch (Exception e) {
                    Log.warning("Place controller choked in " +
                                "didLeavePlace [plobj=" + _plobj + "].");
                    Log.logStackTrace(e);
                }
                _controller = null;
            }

            // unsubscribe from our old place object
            omgr.unsubscribeFromObject(_plobj.getOid(), this);
            _plobj = null;
        }

        // make a note that we're now mostly in the new location
        _previousPlaceId = _placeId;
        _placeId = placeId;

        try {
            // start up a new place controller to manage the new place
            Class cclass = config.getControllerClass();
            _controller = (PlaceController)cclass.newInstance();
            _controller.init(_ctx, config);

        } catch (Exception e) {
            Log.warning("Error creating or initializing place controller " +
                        "[config=" + config + "].");
            Log.logStackTrace(e);
        }

        // subscribe to our new place object to complete the move
        omgr.subscribeToObject(_placeId, this);
    }

    /**
     * This can be called by derived classes that need to coopt the moving
     * process to extend it in some way or other. If the coopted move
     * request fails, this failure can be propagated to the location
     * observers if appropriate.
     *
     * @param placeId the place oid to which we failed to move.
     * @param reason the reason code given for failure.
     */
    protected void failedToMoveTo (int placeId, String reason)
    {
        // let our observers know what's up
        notifyFailure(placeId, reason);
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
                Log.warning("Location director unable to fetch body " +
                            "object; all has gone horribly wrong" +
                            "[cause=" + cause + "].");
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
    public void handleMoveSucceeded (int invid, PlaceConfig config)
    {
        // handle the successful move
        didMoveTo(_pendingPlaceId, config);

        // and clear out the tracked pending oid
        _pendingPlaceId = -1;
    }

    /**
     * Called in response to a failed <code>moveTo</code> request.
     */
    public void handleMoveFailed (int invid, String reason)
    {
        // clear out our pending request oid
        int placeId = _pendingPlaceId;
        _pendingPlaceId = -1;

        Log.info("moveTo failed [pid=" + placeId +
                 ", reason=" + reason + "].");

        // let our observers know that something has gone horribly awry
        notifyFailure(placeId, reason);
    }

    public void objectAvailable (DObject object)
    {
        // yay, we have our new place object
        _plobj = (PlaceObject)object;

        // let the place controller know that we're ready to roll
        try {
            _controller.willEnterPlace(_plobj);
        } catch (Exception e) {
            Log.warning("Controller choked in willEnterPlace " +
                        "[place=" + _plobj + "].");
            Log.logStackTrace(e);
        }

        // let our observers know that all is well on the western front
        for (int i = 0; i < _observers.size(); i++) {
            LocationObserver obs = (LocationObserver)_observers.get(i);
            obs.locationDidChange(_plobj);
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

        // we need to sort out what to do about the half-initialized place
        // controller. presently we punt and hope that calling
        // didLeavePlace() without ever having called willEnterPlace()
        // does whatever's necessary

        // try to return to our previous location
        recoverFailedMove(placeId);
    }

    /**
     * If a <code>moveTo</code> request fails because we are unable to
     * fetch our new place object, we need to do something to recover. By
     * default that means attempting to return to the last location we
     * occupied, but derived classes may need to do things differently.
     *
     * @param placeId the place id that we tried to move to but that
     * failed.
     */
    protected void recoverFailedMove (int placeId)
    {
        // if we were previously somewhere (and that somewhere isn't where
        // we just tried to go), try going back to that happy place
        if (_previousPlaceId != -1 && _previousPlaceId != placeId) {
            moveTo(_previousPlaceId);
        }
    }

    protected void notifyFailure (int placeId, String reason)
    {
        for (int i = 0; i < _observers.size(); i++) {
            LocationObserver obs = (LocationObserver)_observers.get(i);
            obs.locationChangeFailed(placeId, reason);
        }
    }

    /** The context through which we access needed services. */
    protected CrowdContext _ctx;

    /** Our location observer list. */
    protected List _observers = new ArrayList();

    /** The oid of the place we currently occupy. */
    protected int _placeId = -1;

    /** The place object that we currently occupy. */
    protected PlaceObject _plobj;

    /** The place controller in effect for our current place. */
    protected PlaceController _controller;

    /**
     * The oid of the place for which we have an outstanding moveTo
     * request, or -1 if we have no outstanding request.
     */
    protected int _pendingPlaceId = -1;

    /** The oid of the place we previously occupied. */
    protected int _previousPlaceId = -1;
}
