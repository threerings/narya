//
// $Id: PuzzleControllerDelegate.java,v 1.2 2004/02/25 14:48:44 mdb Exp $

package com.threerings.puzzle.client;

import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.game.GameControllerDelegate;

import com.threerings.puzzle.data.Board;
import com.threerings.puzzle.data.PuzzleCodes;
import com.threerings.puzzle.data.PuzzleGameCodes;
import com.threerings.puzzle.data.PuzzleObject;

/**
 * A base class for puzzle controller delegates. Provides access to some
 * delegated puzzle controller methods ({@link #startAction}, {@link
 * #clearAction}, etc.) and provides a casted reference to the puzzle
 * object.
 */
public class PuzzleControllerDelegate extends GameControllerDelegate
    implements PuzzleCodes, PuzzleGameCodes
{
    /**
     * Constructs a puzzle controller delegate.
     */
    public PuzzleControllerDelegate (PuzzleController ctrl)
    {
        super(ctrl);

        // keep around a casted reference to our controller
        _ctrl = ctrl;
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // get a casted reference to our game object
        _puzobj = (PuzzleObject)plobj;
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);

        _puzobj = null;
    }

    /**
     * Called when a player is knocked out of the game.
     */
    public void playerKnockedOut (int pidx)
    {
    }

    /**
     * Called when the user toggles chatting mode.
     */
    public void setChatting (boolean chatting)
    {
    }

    /**
     * Can we start chatting at the instant that this method is called?
     */
    protected boolean canStartChatting ()
    {
        return true;
    }

    /**
     * Derived classes should override this method and do whatever is
     * necessary to start up the action for their puzzle. This could be
     * called when the user is already in the "room" and the game starts,
     * or immediately upon entering the room if the game is already
     * started (for example if they disconnected and reconnected to a game
     * already in progress).
     */
    protected void startAction ()
    {
    }

    /**
     * Delegates that wish to postpone action clearing can override this
     * method to return false until such time as the action can be
     * cleared. They must, however, call {@link #maybeClearAction} when
     * conditions become such that they would once again allow action to
     * be cleared.
     */
    protected boolean canClearAction ()
    {
        return true;
    }

    /**
     * Calls {@link PuzzleController#maybeClearAction}, preserving its
     * protected access but making the method available to all
     * PuzzleControllerDelegate derivations.
     */
    protected void maybeClearAction ()
    {
        _ctrl.maybeClearAction();
    }

    /**
     * Puzzles should override this method and clear out any action on the
     * board and generally clean up anything that was going on because the
     * game was in play. This is called when the game has ended or when it
     * is going to reset and when the client leaves the game "room".
     * Anything that is cleared out here should be recreated in {@link
     * #startAction}.
     */
    protected void clearAction ()
    {
    }

    /**
     * Called when the puzzle controller sets up a new board for the
     * player.
     *
     * @param board the newly initialized and ready-to-go board.
     */
    public void setBoard (Board board)
    {
    }

    /**
     * Called when the game is suspended while we wait for a new piece
     * packet from the server.
     */
    public void didSuspend ()
    {
    }

    /**
     * Called when the game is resumed once a new piece packet arrives
     * from the server while we were suspended awaiting its arrival.
     *
     * @param delta the time elapsed in milliseconds while we were
     * suspended.
     */
    public void didResume (long delta)
    {
    }

    /**
     * Called when the puzzle difficulty level is changed.
     */
    public void difficultyChanged (int level)
    {
    }

    /** Our puzzle controller. */
    protected PuzzleController _ctrl;

    /** The puzzle distributed object. */
    protected PuzzleObject _puzobj;
}
