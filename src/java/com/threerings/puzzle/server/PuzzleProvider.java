//
// $Id: PuzzleProvider.java,v 1.3 2004/03/06 11:29:19 mdb Exp $

package com.threerings.puzzle.server;

import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService.InvocationListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.parlor.game.GameManager;

import com.threerings.puzzle.Log;
import com.threerings.puzzle.client.PuzzleService.EnterPuzzleListener;
import com.threerings.puzzle.data.PuzzleCodes;
import com.threerings.puzzle.data.PuzzleObject;
import com.threerings.puzzle.data.PuzzlerObject;
import com.threerings.puzzle.data.SolitairePuzzleConfig;

/**
 * Handles the server end of the puzzle services.
 */
public class PuzzleProvider
    implements InvocationProvider, PuzzleCodes
{
    /**
     * Constructs a puzzle provider instance.
     */
    public PuzzleProvider (RootDObjectManager omgr, PlaceRegistry plreg)
    {
        _omgr = omgr;
        _plreg = plreg;
    }

    /**
     * Processes a request from a client to start a puzzle.
     */
    public void startPuzzle (
        ClientObject caller, SolitairePuzzleConfig config,
        InvocationListener listener)
        throws InvocationException
    {
        BodyObject user = (BodyObject)caller;

        Log.debug("Processing start puzzle [caller=" + user.who() +
                  ", config=" + config + "].");

        try {
            // just this fellow will be playing
            config.players = new Name[] { user.username };

            // create the game manager and begin its initialization
            // process
            GameManager gmgr = (GameManager)_plreg.createPlace(config, null);

            // the game manager will take care of notifying the player
            // that the game has been created once it has been started up

        } catch (InstantiationException ie) {
            Log.warning("Error instantiating puzzle manager " +
                        "[for=" + caller.who() + ", config=" + config + "].");
            Log.logStackTrace(ie);
            throw new InvocationException(INTERNAL_ERROR);
        }
    }

    /**
     * Processes a request from a client to enter a puzzle.
     */
    public void enterPuzzle (
        ClientObject caller, int puzzleOid, EnterPuzzleListener listener)
        throws InvocationException
    {
        // do the entry and send the response
        listener.puzzleEntered(enterPuzzle((BodyObject)caller, puzzleOid));
    }

    /**
     * Processes a request from a client to leave their current puzzle.
     */
    public void leavePuzzle (ClientObject caller)
    {
        BodyObject user = (BodyObject)caller;
        int puzzleOid = ((PuzzlerObject)user).getPuzzleLoc();

        // make sure they're currently in a puzzle
        if (puzzleOid == -1) {
            Log.warning("Received leave puzzle request from user that " +
                        "isn't in a puzzle [user=" + user.who() + "].");
            return;
        }

        // make sure the puzzle in question actually exists
        PlaceManager pmgr = _plreg.getPlaceManager(puzzleOid);
        if (pmgr == null) {
            Log.info("Requested to leave a non-existent puzzle " +
                     "[user=" + user.who() + "].");
            return;
        }

        // depart their old puzzle
        departPuzzle(user);

        // set their puzzle location to -1 to indicate that they're no
        // longer in a puzzle
        ((PuzzlerObject)user).setPuzzleLoc(-1);
    }

    /**
     * Processes a request from a client to change the difficulty level of
     * their current puzzle.
     */
    public void changeDifficulty (ClientObject caller, int level)
    {
        BodyObject user = (BodyObject)caller;
        int puzzleOid = ((PuzzlerObject)user).getPuzzleLoc();

        // make sure they're currently in a puzzle
        if (puzzleOid == -1) {
            Log.warning("Received change difficulty request from user that " +
                        "isn't in a puzzle [user=" + user.who() + "].");
            return;
        }

        // make sure the puzzle in question actually exists
        PlaceManager pmgr = _plreg.getPlaceManager(puzzleOid);
        if (pmgr == null) {
            Log.info("Requested to change difficulty of a non-existent " +
                     "puzzle [user=" + user.who() + "].");
            return;
        }

        // change the puzzle difficulty level
        PuzzleObject puzobj = (PuzzleObject)pmgr.getPlaceObject();
        puzobj.setDifficulty(level);
    }

    /**
     * Moves the specified body from whatever puzzle they currently occupy
     * to the puzzle identified by the supplied oid.
     *
     * @return the config object for the new location.
     *
     * @exception InvocationException thrown if the entry was not
     * successful for some reason (which will be communicated as an error
     * code in the exception's message data).
     */
    public PlaceConfig enterPuzzle (BodyObject user, int puzzleOid)
        throws InvocationException
    {
        int bodoid = user.getOid();
        int puzzleLoc = ((PuzzlerObject)user).getPuzzleLoc();

        // make sure the place in question actually exists
        PlaceManager pmgr = _plreg.getPlaceManager(puzzleOid);
        if (pmgr == null) {
            Log.info("Requested to move to non-existent place " +
                     "[user=" + user.who() + ", puzzle=" + puzzleOid + "].");
            throw new InvocationException(NO_SUCH_PUZZLE);
        }

        // acquire a lock on the body object to ensure that rapid fire
        // enterPuzzle requests don't break things
        if (!user.acquireLock("enterPuzzleLock")) {
            // if we're still locked, a previous enterPuzzle request
            // hasn't been fully processed
            throw new InvocationException(ENTER_IN_PROGRESS);
        }

        // make sure they're not already in the puzzle they're entering
        if (puzzleLoc == puzzleOid) {
            throw new InvocationException(ALREADY_IN_PUZZLE);
        }

        // depart any previously occupied puzzle
        departPuzzle(user);

        // set the body's new puzzle location
        PlaceObject place = pmgr.getPlaceObject();
        ((PuzzlerObject)user).setPuzzleLoc(place.getOid());

        // prepare to update their new puzzle location
        place.startTransaction();
        try {
            // generate a new occupant info record (which will add it to
            // the target location)
            pmgr.buildOccupantInfo(user);

            // add the body object id to the place object's occupant list
            place.addToOccupants(bodoid);
        } finally {
            place.commitTransaction();
        }

        // and finally queue up a lock release event to release the lock
        // once all these events are processed
        user.releaseLock("enterPuzzleLock");

        return pmgr.getConfig();
    }

    /**
     * Removes the user's occupant information from the puzzle object that
     * they are now departing.
     */
    protected void departPuzzle (BodyObject user)
    {
        int puzzleOid = ((PuzzlerObject)user).getPuzzleLoc();
        if (puzzleOid == -1) {
            return;
        }
        int bodoid = user.getOid();

        // remove them from the occupant list of the previous puzzle
        try {
            PlaceObject pold = (PlaceObject)_omgr.getObject(puzzleOid);
            if (pold != null) {
                Integer key = new Integer(bodoid);
                // remove their occupant info (which is keyed on oid) and
                // remove them from the occupant list
                pold.startTransaction();
                try {
                    pold.removeFromOccupantInfo(key);
                    pold.removeFromOccupants(bodoid);
                } finally {
                    pold.commitTransaction();
                }

            } else {
                Log.info("Body's prior puzzle no longer around? " +
                         "[boid=" + bodoid +
                         ", puzoid=" + puzzleOid + "].");
            }

        } catch (ClassCastException cce) {
            Log.warning("Body claims to be in puzzle which references " +
                        "non-PlaceObject!? [boid=" + bodoid +
                        ", puzoid=" + puzzleOid  + "].");
        }
    }

    /** The distributed object manager with which we interoperate. */
    protected RootDObjectManager _omgr;

    /** The place registry with which we interoperate. */
    protected PlaceRegistry _plreg;
}
