//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.puzzle.drop.server;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.puzzle.Log;
import com.threerings.puzzle.data.Board;
import com.threerings.puzzle.data.PuzzleCodes;
import com.threerings.puzzle.server.PuzzleManager;
import com.threerings.puzzle.server.PuzzleManagerDelegate;

import com.threerings.puzzle.drop.data.DropBoard;
import com.threerings.puzzle.drop.data.DropCodes;
import com.threerings.puzzle.drop.data.DropConfig;
import com.threerings.puzzle.drop.data.DropLogic;
import com.threerings.puzzle.drop.util.PieceDropLogic;
import com.threerings.puzzle.drop.util.PieceDropper;

/**
 * Provides the necessary support for a puzzle game that involves a
 * two-dimensional board containing pieces, with new pieces either falling
 * into the board as a "drop block", or rising into the bottom of the
 * board in new piece rows, groups of blocks can be "broken" and garbage
 * can be sent to other players' boards as a result. This is implemented
 * as a delegate so that the natural hierarchy need not be twisted to
 * differentiate between puzzles that use piece dropping and those that
 * don't. Because we have need to structure our hierarchy around things
 * like whether a puzzle is a duty puzzle, this becomes necessary.
 *
 * <p> A puzzle game using these services will then need to extend this
 * delegate, implementing the necessary methods to customize it for the
 * particulars of their game and then register it with their game manager
 * via {@link PuzzleManager#addDelegate}.
 *
 * <p> It also keeps track of, for each player, board level information,
 * and player game status.  Miscellaneous utility routines are provided
 * for checking things like whether the game is over, whether a player is
 * still active in the game, and so forth.
 *
 * <p> Derived classes are likely to want to override {@link
 * #getPieceDropLogic}.
 */
public abstract class DropManagerDelegate extends PuzzleManagerDelegate
    implements PuzzleCodes, DropCodes
{
    /**
     * Provides the delegate with a reference to the manager for which it
     * is delegating as well as the logic object that it uses to determine
     * how to manage the drop puzzle.
     */
    public DropManagerDelegate (PuzzleManager puzmgr, DropLogic logic)
    {
        super(puzmgr);

        // configure the game-specific settings
        _usedrop = logic.useBlockDropping();
        _userise = logic.useBoardRising();
        if (_usedrop && _userise) {
            Log.warning("Can't use dropping blocks and board rising "+
                        "functionality simultaneously in a drop puzzle game! " +
                        "Falling back to straight dropping.");
            _userise = false;
        }
    }

    // documentation inherited
    public void didInit (PlaceConfig config)
    {
        _dconfig = (DropConfig)config;

        // save things off
        _bwid = _dconfig.getBoardWidth();
        _bhei = _dconfig.getBoardHeight();

        super.didInit(config);
    }

    // documentation inherited
    public void didStartup (PlaceObject plobj)
    {
        super.didStartup(plobj);

        // initialize the drop board array
        _dboards = new DropBoard[_puzmgr.getPlayerCount()];

        // create the piece dropper if appropriate
        PieceDropLogic pdl = getPieceDropLogic();
        if (pdl != null) {
            _dropper = new PieceDropper(pdl);
        }
    }

    // documentation inherited
    public void gameWillStart ()
    {
        super.gameWillStart();

        // get casted references to all player drop boards
        Board[] board = _puzmgr.getBoards();
        for (int ii = 0; ii < _puzmgr.getPlayerCount(); ii++) {
            _dboards[ii] = (DropBoard)board[ii];
        }
    }

    /**
     * Drops any pieces that need dropping on the given player's board and
     * returns whether any pieces were dropped.
     */
    protected boolean dropPieces (DropBoard board)
    {
	return (_dropper.dropPieces(board, null) > 0);
    }

    /**
     * Returns the piece drop logic used to drop any pieces that need
     * dropping in the board.
     */
    protected PieceDropLogic getPieceDropLogic ()
    {
        return null;
    }

    /**
     * This method should be called by derived classes whenever the player
     * successfully places a drop block.
     */
    protected void placedBlock (int pidx)
    {
    }

    /** The drop game board for each player. */
    protected DropBoard[] _dboards;

    /** The drop game config object. */
    protected DropConfig _dconfig;

    /** Whether the game is using drop block functionality. */
    protected boolean _usedrop;

    /** Whether the game is using board rising functionality. */
    protected boolean _userise;

    /** The board dimensions in pieces. */
    protected int _bwid, _bhei;

    /** The piece dropper used to drop pieces in the board if the puzzle
     * chooses to make use of piece dropping functionality. */
    protected PieceDropper _dropper;

    /** Used to limit the maximum number of board update loops permitted
     * before assuming something's gone horribly awry and aborting. */
    protected static final int MAX_UPDATE_LOOPS = 100;
}
