//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import com.samskivert.util.ObserverList;
import com.samskivert.util.ResultListener;
import com.samskivert.util.ObserverList.ObserverOp;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.util.SafeSubscriber;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.CrowdCodes;
import com.threerings.crowd.data.LocationCodes;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import static com.threerings.crowd.Log.log;

/**
 * The location director provides a means by which entities on the client can request to move from
 * place to place and can be notified if other entities have caused the client to move to a new
 * place. It also provides a mechanism for ratifying a request to move to a new place before
 * actually issuing the request.
 */
public class LocationDirector extends BasicDirector
    implements LocationCodes, LocationReceiver
{
    /**
     * Used to recover from a moveTo request that was accepted but resulted in a failed attempt to
     * fetch the place object to which we were moving.
     */
    public static interface FailureHandler
    {
        /**
         * Should instruct the client to move to the last known working location (as well as clean
         * up after the failed moveTo request).
         */
        void recoverFailedMove (int placeId);
    }

    /**
     * Constructs a location director which will configure itself for operation using the supplied
     * context.
     */
    public LocationDirector (CrowdContext ctx)
    {
        super(ctx);

        // keep this around for later
        _ctx = ctx;

        // register for location notifications
        _ctx.getClient().getInvocationDirector().registerReceiver(new LocationDecoder(this));
    }

    /**
     * Adds a location observer to the list. This observer will subsequently be notified of
     * potential, effected and failed location changes.
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
     * Returns the place object for the location we currently occupy or null if we're not currently
     * occupying any location.
     */
    public PlaceObject getPlaceObject ()
    {
        return _plobj;
    }

    /**
     * Returns true if there is a pending move request.
     */
    public boolean movePending ()
    {
        return (_pendingPlaceId > 0);
    }

    /**
     * Requests that this client be moved to the specified place. A request will be made and when
     * the response is received, the location observers will be notified of success or failure.
     *
     * @return true if the move to request was issued, false if it was rejected by a location
     * observer or because we have another request outstanding.
     */
    public boolean moveTo (int placeId)
    {
        // make sure the placeId is valid
        if (placeId < 0) {
            log.warning("Refusing moveTo(): invalid placeId " + placeId + ".");
            return false;
        }

        // first check to see if our observers are happy with this move request
        if (!mayMoveTo(placeId, null)) {
            return false;
        }

        // we need to call this both to mark that we're issuing a move request and to check to see
        // if the last issued request should be considered stale
        boolean refuse = checkRepeatMove();

        // complain if we're over-writing a pending request
        if (_pendingPlaceId != -1) {
            // if the pending request has been outstanding more than a minute, go ahead and let
            // this new one through in an attempt to recover from dropped moveTo requests
            if (refuse) {
                log.warning("Refusing moveTo; We have a request outstanding",
                            "ppid", _pendingPlaceId, "npid", placeId);
                return false;

            } else {
                log.warning("Overriding stale moveTo request", "ppid", _pendingPlaceId,
                            "npid", placeId);
            }
        }

        // make a note of our pending place id
        _pendingPlaceId = placeId;

        // issue a moveTo request
        log.info("Issuing moveTo(" + placeId + ").");
        _lservice.moveTo(placeId, new LocationService.MoveListener() {
            public void moveSucceeded (PlaceConfig config) {
                // handle the successful move
                didMoveTo(_pendingPlaceId, config);
                // and clear out the tracked pending oid
                _pendingPlaceId = -1;

                handlePendingForcedMove();
            }
            public void requestFailed (String reason) {
                // clear out our pending request oid
                int placeId = _pendingPlaceId;
                _pendingPlaceId = -1;
                log.info("moveTo failed", "pid", placeId, "reason", reason);
                // let our observers know that something has gone horribly awry
                handleFailure(placeId, reason);
                handlePendingForcedMove();
            }
        });
        return true;
    }

    /**
     * Requests to move to the room that we last occupied, if such a room exists.
     *
     * @return true if we had a previous room and we requested to move to it, false if we had no
     * previous room.
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
     * Issues a request to leave our current location.
     *
     * @return true if we were able to leave, false if we are in the middle of moving somewhere and
     * can't yet leave.
     */
    public boolean leavePlace ()
    {
        if (_pendingPlaceId != -1) {
            return false;
        }

        _lservice.leavePlace();
        didLeavePlace();

        // let our observers know that we're no longer in a location
        _observers.apply(_didChangeOp);

        return true;
    }

    /**
     * This can be called by cooperating directors that need to coopt the moving process to extend
     * it in some way or other. In such situations, they should call this method before moving to a
     * new location to check to be sure that all of the registered location observers are amenable
     * to a location change.
     *
     * @param placeId the place oid of our tentative new location.
     *
     * @return true if everyone is happy with the move, false if it was vetoed by one of the
     * location observers.
     */
    public boolean mayMoveTo (final int placeId, ResultListener<PlaceConfig> rl)
    {
        final boolean[] vetoed = new boolean[1];
        _observers.apply(new ObserverOp<LocationObserver>() {
            public boolean apply (LocationObserver obs) {
                vetoed[0] = (vetoed[0] || !obs.locationMayChange(placeId));
                return true;
            }
        });

        // if we're actually going somewhere, let the controller know that we might be leaving
        mayLeavePlace();

        // if we have a result listener, let it know if we failed or keep it for later if we're
        // still going
        if (rl != null) {
            if (vetoed[0]) {
                rl.requestFailed(new MoveVetoedException());
            } else {
                _moveListener = rl;
            }
        }
        // and return the result
        return !vetoed[0];
    }

    /**
     * Called to inform our controller that we may be leaving the current place.
     */
    protected void mayLeavePlace ()
    {
        if (_controller != null) {
            try {
                _controller.mayLeavePlace(_plobj);
            } catch (Exception e) {
                log.warning("Place controller choked in mayLeavePlace", "plobj", _plobj, e);
            }
        }
    }

    /**
     * This can be called by cooperating directors that need to coopt the moving process to extend
     * it in some way or other. In such situations, they will be responsible for receiving the
     * successful move response and they should let the location director know that the move has
     * been effected.
     *
     * @param placeId the place oid of our new location.
     * @param config the configuration information for the new place.
     */
    public void didMoveTo (int placeId, PlaceConfig config)
    {
        if (_moveListener != null) {
            _moveListener.requestCompleted(config);
            _moveListener = null;
        }

        // keep track of our previous place id
        _previousPlaceId = _placeId;

        // clear out our last request time
        _lastRequestTime = 0;

        // do some cleaning up in case we were previously in a place
        didLeavePlace();

        // make a note that we're now mostly in the new location
        _placeId = placeId;

        // start up a new place controller to manage the new place
        try {
            _controller = createController(config);
            if (_controller == null) {
                log.warning("Place config returned null controller", "config", config);
                return;
            }
            _controller.init(_ctx, config);

            // subscribe to our new place object to complete the move
            subscribeToPlace();

        } catch (Exception e) {
            log.warning("Failed to create place controller", "config", config, e);
            handleFailure(_placeId, LocationCodes.E_INTERNAL_ERROR);
        }
    }

    /**
     * Called when we're leaving our current location. Informs the location's controller that we're
     * departing, unsubscribes from the location's place object, and clears out our internal place
     * information.
     */
    public void didLeavePlace ()
    {
        // unsubscribe from our old place object
        if (_subber != null) {
            _subber.unsubscribe(_ctx.getDObjectManager());
            _subber = null;
        }

        // let the old controller know that things are going away
        if (_plobj != null && _controller != null) {
            try {
                _controller.didLeavePlace(_plobj);
            } catch (Exception e) {
                log.warning("Place controller choked in didLeavePlace", "plobj", _plobj, e);
            }
        }

        // and clear out other bits
        _plobj = null;
        _controller = null;
        _placeId = -1;
    }

    /**
     * This can be called by cooperating directors that need to coopt the moving process to extend
     * it in some way or other. If the coopted move request fails, this failure can be propagated
     * to the location observers if appropriate.
     *
     * @param placeId the place oid to which we failed to move.
     * @param reason the reason code given for failure.
     */
    public void failedToMoveTo (int placeId, String reason)
    {
        if (_moveListener != null) {
            _moveListener.requestFailed(new MoveFailedException(reason));
            _moveListener = null;
        }

        // clear out our last request time
        _lastRequestTime = 0;

        // let our observers know what's up
        handleFailure(placeId, reason);
    }

    /**
     * Called to test and set a time stamp that we use to determine if a pending moveTo request is
     * stale.
     */
    public boolean checkRepeatMove ()
    {
        long now = System.currentTimeMillis();
        if (now - _lastRequestTime < STALE_REQUEST_DURATION) {
            return true;

        } else {
            _lastRequestTime = now;
            return false;
        }
    }

    @Override
    public void clientDidLogon (Client client)
    {
        super.clientDidLogon(client);

        // subscribe to our body object
        Subscriber<BodyObject> sub = new Subscriber<BodyObject>() {
            public void objectAvailable (BodyObject object) {
                gotBodyObject(object);
            }
            public void requestFailed (int oid, ObjectAccessException cause) {
                log.warning("Location director unable to fetch body object; all has gone " +
                            "horribly wrong", "cause", cause);
            }
        };
        int cloid = client.getClientOid();
        client.getDObjectManager().subscribeToObject(cloid, sub);
    }

    @Override
    public void clientDidLogoff (Client client)
    {
        super.clientDidLogoff(client);

        // clear ourselves out and inform observers of our departure
        mayLeavePlace();
        didLeavePlace();

        // let our observers know that we're no longer in a location
        _observers.apply(_didChangeOp);

        // clear out everything else (it's possible that we were logged off in the middle of a
        // change location request)
        _pendingPlaceId = -1;
        _pendingForcedMoves.clear();
        _previousPlaceId = -1;
        _lastRequestTime = 0L;
        _lservice = null;
    }

    @Override
    protected void registerServices (Client client)
    {
        client.addServiceGroup(CrowdCodes.CROWD_GROUP);
    }

    @Override
    protected void fetchServices (Client client)
    {
        // obtain our service handle
        _lservice = client.requireService(LocationService.class);
    }

    /**
     * Subscribe to the PlaceObject identified by _placeId and
     * call {@link #gotPlaceObject} with the result.
     */
    protected void subscribeToPlace ()
    {
        _subber = new SafeSubscriber<PlaceObject>(_placeId, new Subscriber<PlaceObject>() {
            public void objectAvailable (PlaceObject object) {
                gotPlaceObject(object);
            }
            public void requestFailed (int oid, ObjectAccessException cause) {
                // aiya! we were unable to fetch our new place object; something is badly wrong
                log.warning("Aiya! Unable to fetch place object for new location", "plid", oid,
                            "reason", cause);
                // clear out our half initialized place info
                int placeId = _placeId;
                _placeId = -1;
                // let the kids know shit be fucked
                handleFailure(placeId, "m.unable_to_fetch_place_object");
            }
        });
        _subber.subscribe(_ctx.getDObjectManager());
    }

    protected void gotPlaceObject (PlaceObject object)
    {
        // yay, we have our new place object
        _plobj = object;

        // fill in our manager caller
        _plobj.initManagerCaller(_ctx.getClient().getDObjectManager());

        // let the place controller know that we're ready to roll
        if (_controller != null) {
            try {
                _controller.willEnterPlace(_plobj);
            } catch (Exception e) {
                log.warning("Controller choked in willEnterPlace", "place", _plobj, e);
            }
        }

        // let our observers know that all is well on the western front
        _observers.apply(_didChangeOp);
    }

    protected void gotBodyObject (BodyObject clobj)
    {
        // TODO? check to see if we are already in a location, in which case we'll want to be going
        // there straight away
    }

    // documentation inherited from interface
    public void forcedMove (final int placeId)
    {
        // if we're in the middle of a move, we can't abort it or we will screw everything up, so
        // just finish up what we're doing and assume that the repeated move request was the
        // spurious one as it would be in the case of lag causing rapid-fire repeat requests
        if (movePending()) {
            if (_pendingPlaceId == placeId) {
                log.info("Dropping forced move because we have a move pending",
                    "pendId", _pendingPlaceId, "reqId", placeId);
            } else {
                log.info("Delaying forced move because we have a move pending",
                    "pendId", _pendingPlaceId, "reqId", placeId);
                addPendingForcedMove(new Runnable() {
                    public void run () {
                        forcedMove(placeId);
                    }
                });
            }
            return;
        }

        log.info("Moving at request of server", "placeId", placeId);
        // clear out our old place information
        mayLeavePlace();
        didLeavePlace();
        // move to the new place
        moveTo(placeId);
    }

    /**
     * Sets the failure handler which will recover from place object fetching failures. In the
     * event that we are unable to fetch our place object after making a successful moveTo request,
     * we attempt to rectify the failure by moving back to the last known working location. Because
     * entites that cooperate with the location director may need to become involved in this
     * failure recovery, we provide this interface whereby they can interject themseves into the
     * failure recovery process and do their own failure recovery.
     */
    public void setFailureHandler (FailureHandler handler)
    {
        if (_failureHandler != null) {
            log.warning("Requested to set failure handler, but we've already got one. The " +
                        "conflicting entities will likely need to perform more sophisticated " +
                        "coordination to deal with failures.",
                        "old", _failureHandler, "new", handler);

        } else {
            _failureHandler = handler;
        }
    }

    protected void handleFailure (final int placeId, final String reason)
    {
        _observers.apply(new ObserverOp<LocationObserver>() {
            public boolean apply (LocationObserver obs) {
                obs.locationChangeFailed(placeId, reason);
                return true;
            }
        });

        // try to return to our previous location
        if (_failureHandler != null) {
            _failureHandler.recoverFailedMove(placeId);

        } else if (_placeId <= 0) {
            // if we were previously somewhere (and that somewhere isn't where we just tried to
            // go), try going back to that happy place
            if (_previousPlaceId != -1 && _previousPlaceId != placeId) {
                moveTo(_previousPlaceId);
            }

        } // else we're currently somewhere, so just stay there
    }

    /**
     * Called to create our place controller using the supplied place configuration. This lives in
     * a separate method so that derived instances can do funny class loader business if necessary
     * to load the place controller using a sandboxed class loader.
     */
    protected PlaceController createController (PlaceConfig config)
    {
        return config.createController();
    }

    public void addPendingForcedMove (Runnable move)
    {
        _pendingForcedMoves.add(move);
    }

    protected void handlePendingForcedMove ()
    {
        if (!_pendingForcedMoves.isEmpty()) {
            _ctx.getClient().getRunQueue().postRunnable(_pendingForcedMoves.remove(0));
        }
    }

    /** The context through which we access needed services. */
    protected CrowdContext _ctx;

    /** Provides access to location services. */
    protected LocationService _lservice;

    /** Our location observer list. */
    protected ObserverList<LocationObserver> _observers = ObserverList.newSafeInOrder();

    /** Used to subscribe to our place object. */
    protected SafeSubscriber<PlaceObject> _subber;

    /** The oid of the place we currently occupy. */
    protected int _placeId = -1;

    /** The place object that we currently occupy. */
    protected PlaceObject _plobj;

    /** The place controller in effect for our current place. */
    protected PlaceController _controller;

    /** The oid of the place for which we have an outstanding moveTo request, or -1 if we have no
     * outstanding request. */
    protected int _pendingPlaceId = -1;

    /** The oid of the place we previously occupied. */
    protected int _previousPlaceId = -1;

    /** The last time we requested a move to. */
    protected long _lastRequestTime;

    /** The entity that deals when we fail to subscribe to a place object. */
    protected FailureHandler _failureHandler;

    /** A listener that wants to know if we succeeded or how we failed to move.  */
    protected ResultListener<PlaceConfig> _moveListener;

    /** Forced move actions we should take once we complete the move we're in the middle of. */
    protected ArrayList<Runnable> _pendingForcedMoves = Lists.newArrayList();

    /** The operation used to inform observers that the location changed. */
    protected ObserverOp<LocationObserver> _didChangeOp = new ObserverOp<LocationObserver>() {
        public boolean apply (LocationObserver obs) {
            obs.locationDidChange(_plobj);
            return true;
        }
    };

    /** We require that a moveTo request be outstanding for one minute before it is declared to be
     * stale. */
    protected static final long STALE_REQUEST_DURATION = 60L * 1000L;
}
