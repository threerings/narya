//
// $Id: DropControllerDelegate.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.client;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import java.util.List;

import com.samskivert.util.IntListUtil;
import com.samskivert.util.StringUtil;
import com.threerings.util.DirectionUtil;

import com.threerings.media.FrameParticipant;
import com.threerings.media.animation.Animation;
import com.threerings.media.animation.AnimationAdapter;
import com.threerings.media.sprite.Sprite;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.puzzle.util.PuzzleContext;

import com.threerings.puzzle.Log;
import com.threerings.puzzle.client.PuzzleController;
import com.threerings.puzzle.client.PuzzleControllerDelegate;
import com.threerings.puzzle.client.PuzzlePanel;
import com.threerings.puzzle.data.Board;
import com.threerings.puzzle.data.BoardSummary;

import com.threerings.puzzle.drop.data.DropBoard;
import com.threerings.puzzle.drop.data.DropCodes;
import com.threerings.puzzle.drop.data.DropConfig;
import com.threerings.puzzle.drop.data.DropLogic;
import com.threerings.puzzle.drop.data.DropPieceCodes;
import com.threerings.puzzle.drop.util.DropBoardUtil;
import com.threerings.puzzle.drop.util.DropPieceProvider;
import com.threerings.puzzle.drop.util.PieceDropLogic;
import com.threerings.puzzle.drop.util.PieceDropper.PieceDropInfo;
import com.threerings.puzzle.drop.util.PieceDropper;

/**
 * Games that wish to make use of the drop puzzle services will need to
 * create an extension of this delegate class, customizing it for their
 * particular game and then adding it via {@link
 * PuzzleController#addDelegate}.
 *
 * <p> It handles logical actions for a puzzle game that generally
 * consists of a two-dimensional board containing pieces, with new pieces
 * either falling into the board as a "drop block", or rising into the
 * bottom of the board in new piece rows.
 *
 * <p> Derived classes must implement {@link #getPieceVelocity} and {@link
 * #evolveBoard}.
 *
 * <p> Block-dropping puzzles will likely want to override {@link
 * #createNextBlock}, {@link #blockDidLand}, and {@link
 * #getPieceDropLogic}.
 *
 * <p> Board-rising puzzles will likely want to override {@link
 * #getRiseVelocity}, {@link #getRiseDistance}, {@link
 * #getPieceDropLogic}, and {@link #boardDidRise}.
 */
public abstract class DropControllerDelegate extends PuzzleControllerDelegate
    implements DropCodes, DropPieceCodes, FrameParticipant
{
    /** The action command for moving the block to the left. */
    public static final String MOVE_BLOCK_LEFT = "move_block_left";

    /** The action command for moving the block to the right. */
    public static final String MOVE_BLOCK_RIGHT = "move_block_right";

    /** The action command for rotating the block counter-clockwise. */
    public static final String ROTATE_BLOCK_CCW = "rotate_block_ccw";

    /** The action command for rotating the block clockwise. */
    public static final String ROTATE_BLOCK_CW = "rotate_block_cw";

    /** The action command for starting to dropping the block. */
    public static final String START_DROP_BLOCK = "start_drop_block";

    /** The action command for ending dropping the block. */
    public static final String END_DROP_BLOCK = "end_drop_block";

    /** The action command for raising the next rising row. */
    public static final String RAISE_ROW = "raise_row";

    /**
     * Creates a delegate with the specified drop game logic and
     * controller.
     */
    public DropControllerDelegate (PuzzleController ctrl, DropLogic logic)
    {
        super(ctrl);

        // keep this for later
        _ctrl = ctrl;

        // obtain the drop logic parameters
        DropLogic dlogic = (DropLogic)logic;
        _usedrop = dlogic.useBlockDropping();
        _userise = dlogic.useBoardRising();

        if (_userise) {
            // prepare for board rising
            _risevel = getRiseVelocity();
            _risedist = getRiseDistance();
        }
    }

    // documentation inherited
    public void init (CrowdContext ctx, PlaceConfig config)
    {
        super.init(ctx, config);

        // save things off
        PuzzlePanel panel = (PuzzlePanel)_ctrl.getPlaceView();
        _ctx = (PuzzleContext)ctx;
        _dview = (DropBoardView)panel.getBoardView();
        _dpanel = (DropPanel)panel;
        _dboard = (DropBoard)_ctrl.getBoard();

        // obtain the board dimensions
        DropConfig dconfig = (DropConfig)config;
        _bwid = dconfig.getBoardWidth();
        _bhei = dconfig.getBoardHeight();

        // create the piece dropper if appropriate
        PieceDropLogic pdl = getPieceDropLogic();
        if (pdl != null) {
            _dropper = new PieceDropper(pdl);
        }
    }

    /**
     * Get the DropPieceProvider for this puzzle. This is currently
     * only needed if you are using the alwaysfilled property of dropboards.
     */
    protected DropPieceProvider getDropPieceProvider ()
    {
        return null;
    }

    /**
     * Returns the speed with which the next board row should rise into
     * place, in pixels per millisecond.
     */
    protected float getRiseVelocity ()
    {
        return DEFAULT_RISE_VELOCITY;
    }

    /**
     * Returns the distance in pixels that each board row will traverse
     * when rising into place.
    */
    protected int getRiseDistance ()
    {
        return DEFAULT_RISE_DISTANCE;
    }

    /**
     * Starts up the action; tries evolving the board to get things going.
     */
    protected void startAction ()
    {
        super.startAction();

//         Log.info("Starting drop action");

        // save off the player index
        _pidx = _ctrl.getPlayerIndex();

        // add ourselves as a frame participant
        _ctx.getFrameManager().registerFrameParticipant(this);

//        if (_userise) {
//            // make sure the board has its next row of pieces
//            advanceRisingPieces();
//            // we'll set up the risestamp on the first rise tick
//        }

        // if we've a drop block left over from our previous action, set
        // it on its merry way once more
        if (_blocksprite != null) {
            long delta = _dview.getTimeStamp() - _blockStamp;
            Log.info("Restarting drop sprite [delta=" + delta + "].");
            _blocksprite.fastForward(delta);
            _blockStamp = 0L;
            _dview.addSprite(_blocksprite);

            // if we cleared the action while the drop sprite was
            // bouncing, we need to land the block to get things going
            // again
            if (_blocksprite.isBouncing()) {
                Log.info("Ended on a bounce, landing the block and " +
                         "starting things up.");
                checkBlockLanded("bounced", true, true);
            }
        }

        // evolve the board to kick-start the game into action
        tryEvolveBoard();
    }

    // documentation inherited
    protected boolean canClearAction ()
    {
//         Log.info("Drop can clear " + _evolving);
        return !_evolving && super.canClearAction();
    }

    /**
     * Clears out all of the action in the board; removes any drop block
     * sprites, any pieces rising in the board, and resets the animation
     * timestamps.
     */
    protected void clearAction ()
    {
        super.clearAction();

//         Log.info("Clearing drop action.");

        // do away with the bounce interval
        _bounceStamp = 0;
        _bounceRow = Integer.MIN_VALUE;

        // kill any active drop block
        if (_blocksprite != null) {
            _dview.removeSprite(_blocksprite);
            _blockStamp = _dview.getTimeStamp();
        }

        // reset intermediate rising timestamps
        _rpstamp = 0;
        _zipstamp = 0;
        _fastDrop = false;

        // remove ourselves as a frame participant
        _ctx.getFrameManager().removeFrameParticipant(this);
    }

    // documentation inherited
    public void gameDidEnd ()
    {
        super.gameDidEnd();

        // clear out the drop block sprite
        _blocksprite = null;

        // reset ourselves back to pre-game conditions
        _risestamp = 0;
//        _dview.setRisingPieces(null);
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
        // handle any block-related movement actions
        if (handleBlockAction(action)) {
            return true;
        }

        String cmd = action.getActionCommand();
        if (cmd.equals(START_DROP_BLOCK)) {
            handleDropBlock(true);

        } else if (cmd.equals(END_DROP_BLOCK)) {
            handleDropBlock(false);

	} else {
            return super.handleAction(action);
 	}

        return true;
    }

    // documentation inherited
    public void setBoard (Board board)
    {
        super.setBoard(board);

        // update the casted board reference
        _dboard = (DropBoard)board;
    }

    // documentation inherited
    protected boolean handleBlockAction (ActionEvent action)
    {
        String cmd = action.getActionCommand();
        boolean handled = false;

        if (cmd.equals(MOVE_BLOCK_LEFT)) {
            handleMoveBlock(LEFT);
            handled = true;

        } else if (cmd.equals(MOVE_BLOCK_RIGHT)) {
            handleMoveBlock(RIGHT);
            handled = true;

        } else if (cmd.equals(ROTATE_BLOCK_CCW)) {
            handleRotateBlock(CCW);
            handled = true;

        } else if (cmd.equals(ROTATE_BLOCK_CW)) {
            handleRotateBlock(CW);
            handled = true;
        }

        if (handled && _blocksprite != null) {
            // land the block if it's been placed on something solid as a
            // result of one of the above actions
            String source = "fiddled [cmd=" + cmd + "]";
            if (checkBlockLanded(source, false, false)) {
                startBounceTimer(source);
            }
	}

        return handled;
    }

    /**
     * Handles block moved events.
     */
    protected void handleMoveBlock (int dir)
    {
        if (_blocksprite == null) {
            return;
        }

        // gather information regarding the attempted move
        Rectangle bb = _blocksprite.getBoardBounds();
        int row = _blocksprite.getRow(), col = _blocksprite.getColumn();
        int dx = (dir == LEFT) ? -1 : 1;

        // if the sprite has made it to the bottom of the board then we
        // don't want to allow it to "virtually" fall any further because
        // of the bounce interval
         float pctdone = (row >= (_bhei - 1)) ? 0 :
             _blocksprite.getPercentDone(_dview.getTimeStamp());

        // get the drop block position resulting from the move
        Point pos = _dboard.getForgivingMove(
            bb.x, bb.y, bb.width, bb.height, dx, 0, pctdone);
        if (pos != null) {
            int frow = row + (pos.y - bb.y);
            int fcol = col + (pos.x - bb.x);
            // Log.info("Valid move [row=" + frow + ", col=" + col + "].");
            _blocksprite.setBoardLocation(frow, fcol);
	}
    }

    /**
     * Handles block rotation events.
     */
    protected void handleRotateBlock (int dir)
    {
        if (_blocksprite == null) {
            return;
        }

        // gather information regarding the attempted rotation
        int[] rows = _blocksprite.getRows();
        int[] cols = _blocksprite.getColumns();
        // if the sprite has made it to the bottom of the board then we
        // don't want to allow it to "virtually" fall any further because
        // of the bounce interval
        float pctdone = (rows[0] >= (_bhei - 1)) ? 0 :
            _blocksprite.getPercentDone(_dview.getTimeStamp());

        // get the drop block position resulting from the rotation
        int[] info = _dboard.getForgivingRotation(
            rows, cols, _blocksprite.getOrientation(), dir,
            getRotationType(), pctdone);
        if (info != null) {
//             Log.info("Found valid rotation " +
//                      "[orient=" + DirectionUtil.toShortString(info[0]) +
//                      ", col=" + info[1] + ", row=" + info[2] +
//                      ", blocksprite=" + _blocksprite + "].");

	    // update the piece image
	    _dview.rotateDropBlock(_blocksprite, info[0]);

            // place the block in its newly rotated location
            _blocksprite.setBoardLocation(info[2], info[1]);

            // let derived classes do what they will
            blockDidRotate(dir);
        }
    }

    /**
     * Called when the drop block has rotated in the specified direction
     * to allow derived classes to engage in any game-specific antics.
     */
    protected void blockDidRotate (int dir)
    {
    }

    /**
     * Returns the rotation type used by this drop game. Either {@link
     * DropBoard#RADIAL_ROTATION} or {@link DropBoard#INPLACE_ROTATION}.
     */
    protected int getRotationType ()
    {
        return DropBoard.RADIAL_ROTATION;
    }

    /**
     * Handles drop block events.
     */
    protected void handleDropBlock (boolean fast)
    {
        _fastDrop = fast;

        // only allow changing the piece velocity if we're not bouncing
        if (_blocksprite != null && _bounceStamp == 0) {
            // Log.info("Updating drop block velocity [fast=" + fast + "].");
            _blocksprite.setVelocity(getPieceVelocity(fast));
        }
    }

    /**
     * Returns the drop sprite velocity to assign to a new drop sprite.
     */
    protected abstract float getPieceVelocity (boolean fast);

    /**
     * Handles creation and dropping of the next dropping block.
     */
    protected void dropNextBlock ()
    {
        if (_blocksprite != null || _ctrl.isWaiting() || !_ctrl.hasAction()) {
            Log.info("Not dropping block [bs=" + (_blocksprite != null) +
                     ", waiting=" + _ctrl.isWaiting() +
                     ", action=" + _ctrl.hasAction() + "].");
            return;
        }

        // determine whether or not the game should be ended because we
        // can't drop the next block
        if (checkDropEndsGame()) {
            return;
        }

        if (!_ctrl.isGameOver() && _puzobj.isActivePlayer(_pidx)) {
            // create the next block
            _blocksprite = createNextBlock();
            if (_blocksprite != null) {
                // reset the drop block fast-drop state
                _fastDrop = false;

                // configure and add the drop block sprite
                _blocksprite.setVelocity(getPieceVelocity(_fastDrop));
                _blocksprite.addSpriteObserver(_dropMovedHandler);
                _dview.addSprite(_blocksprite);

                // update the next block display
                _dpanel.setNextBlock(peekNextPieces());

                // make sure the block has somewhere to go
                if (checkBlockLanded("next-block", false, true)) {
                    startBounceTimer("next-block");
                }
            }

            // clear out our last bounce row
            _bounceRow = Integer.MIN_VALUE;
        }
    }

    /**
     * Called by {@link #dropNextBlock} to determine whether the game
     * should be ended rather than dropping the next block because the
     * board is filled and a new block cannot enter. If true is returned,
     * the drop controller assumes that the derived class will have ended
     * or reset the game as appropriate and will simply abandon its
     * attempt to drop the next block.
     */
    protected boolean checkDropEndsGame ()
    {
        return false;
    }

    /**
     * Called only for block-dropping puzzles when it's time to create the
     * next drop block.  Returns the drop block sprite if it was
     * successfully created, or <code>null</code> if it was not.
     */
    protected DropBlockSprite createNextBlock ()
    {
        // nothing for now
        return null;
    }

    /**
     * Take a peek at the next pieces.
     */
    protected int[] peekNextPieces ()
    {
        return null;
    }

    /**
     * Called when a drop sprite posts a piece moved event.
     */
    protected void handleDropSpriteMoved (
        DropSprite sprite, long when, int col, int row)
    {
        if (sprite instanceof DropBlockSprite) {
            if (checkBlockLanded("piece-moved", false, true)) {
                startBounceTimer("piece-moved");
            }
            // keep dropping the drop block
            sprite.drop();

        } else {
            if (sprite.getDistance() > 0) {
                sprite.drop();

            } else {
                // remove the sprite
                _dview.removeSprite(sprite);

                // apply the pieces to the board
                applyDropSprite(sprite, col, row);

                // perform any new destruction and falling
                tryEvolveBoard();
            }
        }
    }

    /**
     * Applies the pieces in the given sprite to the specified column and
     * row in the board.  Called when a drop sprite has finished
     * traversing its entire distance.
     */
    protected void applyDropSprite (DropSprite sprite, int col, int row)
    {
        // set the pieces in the board
        int[] pieces = sprite.getPieces();
        _dboard.setSegment(VERTICAL, col, row, pieces);
        // dirty the updated board pieces
        _dview.dirtySegment(VERTICAL, col, row, pieces.length);
    }

    /**
     * Calls {@link #tryEvolveBoard(boolean)} with debugging deactivated.
     */
    protected void tryEvolveBoard ()
    {
        tryEvolveBoard(false);
    }

    /**
     * Attempts to evolve the board. This involves first calling {@link
     * #canEvolveBoard} and only calling {@link #evolveBoard} if the
     * former returned true. If the board is fully stabilized, {@link
     * #boardDidStabilize} will be called to reinstate the puzzle action.
     */
    protected void tryEvolveBoard (boolean debug)
    {
        // if we can't evolve the board because things are going on, we
        // bail out immediately
        if (!canEvolveBoard()) {
            if (debug) {
                Log.info("Can't evolve board " +
                         "[acount=" + _dview.getActionCount() + "].");
            }
            return;
        }

        // if we do not evolve the board in any way, let the derived class
        // know that the board stabilized so that they can drop in a new
        // piece if they like or take whatever other action is appropriate
        _evolving = evolveBoard();
        if (debug) {
            Log.info("Evolved board [evolving=" + _evolving + "].");
        }

        // if we're no longer evolving and the action has not ended, go
        // ahead and let our derived class know that the board has
        // stabilized so that it can drop in the next piece or somesuch
        if (!_evolving) {
            if (_ctrl.hasAction()) {
                // this will trigger further puzzle activity
                if (debug) {
                    Log.info("Board did stabilize");
                }
                boardDidStabilize();

            } else {
                if (debug) {
                    Log.info("Maybe clearing action.");
                }
                // this will ensure that if we have been postponing action
                // due to board evolution, that it will now be cleared
                maybeClearAction();
            }
        }
    }

    /**
     * Called to determine whether it is safe to evolve the board. The
     * default implementation does not allow board evolution if there are
     * sprites or animations active on the board.
     */
    protected boolean canEvolveBoard ()
    {
        return (_dview.getActionCount() == 0);
    }

    /**
     * Evolves the board to an unchanging state. If the board is in a
     * state where pieces should react with one another to cause changes
     * to the board state (such as piece dropping via {@link #dropPieces},
     * piece destruction, and/or piece joining), this is where that
     * process should be effected.
     *
     * <p> When no further evolution is possible and the board has
     * stabilized this method should return false to indicate that such
     * action should be taken. That will result in a follow-up call to
     * {@link #boardDidStabilize} (assuming that the action was not
     * cleared prior to the final stabilization of the board).
     */
    protected abstract boolean evolveBoard ();

    /**
     * Called when the board has been fully evolved and is once again
     * stable. The default implementation updates the player's local board
     * summary and drops the next block into the board, but derived
     * classes may wish to perform custom actions if they don't use drop
     * blocks or have other requirements.
     */
    protected void boardDidStabilize ()
    {
        updateSelfSummary();
        dropNextBlock();
    }

    /**
     * Updates the player's own local board summary to reflect the local
     * copy of the player's board which is likely to be more up to date
     * than the server-side board from which all other player board
     * summaries originate.
     */
    protected void updateSelfSummary ()
    {
        if (_puzobj.summaries != null) {
            BoardSummary bsum = _puzobj.summaries[_pidx];
            bsum.setBoard(_dboard);
            bsum.summarize();
            _dpanel.setSummary(_pidx, bsum);
        }
    }

    /**
     * Called when an animation finishes doing its business.  Derived
     * classes may wish to override this method but should be sure to call
     * <code>super.animationDidFinish()</code>.
     */
    protected void animationDidFinish (Animation anim)
    {
        tryEvolveBoard();
    }

    /**
     * Checks whether the drop block can continue dropping and lands its
     * pieces if not.  Returns whether at least one piece of the block has
     * landed; note that the other piece may need subsequent dropping.
     *
     * @param commit if true, the block landing is committed, if false, it
     * is only checked, not committed.
     * @param atTop whether the block sprite is to be treated as being at
     * the top of its current row.
     */
    protected boolean checkBlockLanded (
        String source, boolean commit, boolean atTop)
    {
        if (_blocksprite == null) {
            return true;
        }

        // check to see that both pieces can continue dropping
        int[] rows = _blocksprite.getRows();
        int[] cols = _blocksprite.getColumns();

        // TODO: we may need to limit pctdone here to account for landing
        // on the bottom of the board.
        float pctdone = (atTop) ? 0.0f :
            _blocksprite.getPercentDone(_dview.getTimeStamp());

//         Log.info("Checking landed [source=" + source +
//                  ", bounceRow=" + _bounceRow +
//                  ", rows=" + StringUtil.toString(rows) +
//                  ", cols=" + StringUtil.toString(cols) +
//                  ", orient=" + DirectionUtil.toShortString(
//                      _blocksprite.getOrientation()) +
//                  ", commit=" + commit + ", pctdone=" + pctdone + "].");

        if (_dboard.isValidDrop(rows, cols, pctdone)) {
            return false;
        }

        // if we're committing the landing, remove the sprite and update
        // the board and all that
        if (commit) {
            // give sub-classes a chance to do any pre-landing business
            blockWillLand();

            // stamp the pieces into the board
            int[] pieces = _blocksprite.getPieces();
            boolean error = false;
            for (int ii = 0; ii < pieces.length; ii++) {
                if (rows[ii] >= 0) {
                    int col = cols[ii], row = rows[ii];

                    // sanity-check the block to make sure it's located in
                    // a valid position, and that we aren't somehow
                    // overwriting an existing piece
                    if (col < 0 || col >= _bwid || row >= _bhei) {
                        Log.warning("Placing drop block piece outside board " +
                                    "bounds!? [x=" + col + ", y=" + row +
                                    ", pidx=" + ii +
                                    ", blocksprite=" + _blocksprite + "].");
                        error = true;

                    } else {
                        int cpiece = _dboard.getPiece(col, row);
                        if (cpiece != PIECE_NONE) {
                            Log.warning("Placing drop block piece onto " +
                                        "occupied board position!? [x=" + col +
                                        ", y=" + row + ", pidx=" + ii +
                                        ", blocksprite=" + _blocksprite + "].");
                            error = true;
                        }
                    }

                    if (!error) {
                        // stuff the piece into the board
                        _dboard.setPiece(col, row, pieces[ii]);
                        _dview.dirtyPiece(col, row);
                    }
                }

                if (DEBUG_PUZZLE && error) {
                    _dboard.dump();
                    Log.warning("Bailing out in a flaming pyre of glory.");
                    System.exit(0);
                }
            }

            // remove the drop block sprite
            _dview.removeSprite(_blocksprite);
            _blocksprite = null;

            // give sub-classes a chance to do any post-landing business
            blockDidLand();
        }

        return true;
    }

    /**
     * Called only for block-dropping puzzles when the drop block is about
     * to land on something.  Derived classes may wish to override this
     * method to perform game-specific actions such as queueing up a
     * "block placed" progress event.
     */
    protected void blockWillLand ()
    {
        // nothing for now
    }

    /**
     * Called only for block-dropping puzzles when the drop block lands on
     * something.  Derived classes may wish to override this method to
     * perform any game-specific actions.
     */
    protected void blockDidLand ()
    {
        // nothing for now
    }

    /**
     * Called when a block lands. We give the user a smidgen of time to
     * continue to fiddle with the block before we actually land it. If
     * the block is still landed when the bounce timer expires, we commit
     * the landing, otherwise we let the block keep falling.
     */
    protected void startBounceTimer (String source)
    {
        int bounceRow = IntListUtil.getMaxValue(_blocksprite.getRows());
//         Log.info("startBounceTimer [source=" + source +
//                  ", bounceStamp=" + _bounceStamp +
//                  ", time=" + _dview.getTimeStamp() +
//                  ", bounceRow=" + _bounceRow +
//                  ", nbounceRow=" + bounceRow + "].");

        // forcibly land the block if we bounce twice at the same row
        if (_bounceStamp == 0 && _bounceRow == bounceRow) {
            if (checkBlockLanded("double-bounced", true, true)) {
                tryEvolveBoard();
            }
            return;
        }

        // if the bounce "timer" is already started, the user probably did
        // something like rotate the piece while it was bouncing (which is
        // why we give them the bounce interval), so we don't reset
        if (_bounceStamp == 0) {
            // slow the piece down so that it doesn't fly past the
            // coordinates at which it's potentially landing; we have to
            // do this before we tell the sprite that it's bouncing
            // because changing the velocity fiddles with the rowstamp and
            // we're going to reset the rowstamp when we tell the sprite
            // that it's bouncing
            _blocksprite.setVelocity(getPieceVelocity(false));

            // set up our bounce interval (it depends on the current piece
            // velocity and so must be set at the time we bounce)
            _bounceInterval = (int)
                ((_dview.getPieceHeight() * BOUNCE_FRACTION) /
                 getPieceVelocity(false));
//             Log.info("bounceInterval=" + _bounceInterval +
//                      ", phei=" + _dview.getPieceHeight() +
//                      ", vel=" + getPieceVelocity(false));

            // make a note of the time we started bouncing
            _bounceStamp = _dview.getTimeStamp();

            // and the row at which we're bouncing
            _bounceRow = bounceRow;

            // put the block sprite into bouncing mode
            _blocksprite.setBouncing(true);
        }
    }

    /**
     * Called when the bounce timer expires. Herein we either commit the
     * landing of a block if it is still landed or let it keep falling if
     * it is no longer landed.
    */
    protected void bounceTimerExpired ()
    {
//         Log.info("bounceTimerExpired [bounceStamp=" + _bounceStamp +
//                  ", time=" + _dview.getTimeStamp() +
//                  ", bounceRow=" + _bounceRow + "].");

        // make sure we weren't cancelled for some reason
        if (_bounceStamp != 0) {
            if (checkBlockLanded("bounced", true, true)) {
                tryEvolveBoard();

            } else if (_blocksprite != null) {
                // take the block sprite out of bouncing mode
                _blocksprite.setBouncing(false);
            }
            _bounceStamp = 0;
        }
    }

    /**
     * Drops any pieces that need dropping and returns whether any pieces
     * were dropped.  Derived classes that would like to drop their pieces
     * should include a call to this method in their {@link #evolveBoard}
     * implementation, and must also override {@link #getPieceDropLogic}
     * to provide their game-specific piece dropper implementation.
     */
    protected boolean dropPieces ()
    {
	// get a list of the piece columns to be dropped
	List drops = _dropper.getDroppedPieces(_dboard, getDropPieceProvider());
        int size = drops.size();
	if (size == 0) {
	    return false;
	}

        // drop each column
        for (int ii = 0; ii < size; ii++) {
	    PieceDropInfo pdi = (PieceDropInfo)drops.get(ii);
	    // Log.info("Dropping column segment [pdi=" + pdi + "].");

	    // clear the dropping pieces from the board
            _dboard.setSegment(
                VERTICAL, pdi.col, pdi.row, pdi.pieces.length, PIECE_NONE);

	    // create a piece sprite animating the pieces falling
	    DropSprite sprite = _dview.createPieces(
                pdi.col, pdi.row, pdi.pieces, pdi.dist);
            sprite.setVelocity(1.5f * getPieceVelocity(true));
            sprite.addSpriteObserver(_dropMovedHandler);
            _dview.addActionSprite(sprite);
	}

	return true;
    }

    /**
     * Returns the piece dropper used to drop any pieces that need
     * dropping in the board.  Derived classes that intend to make use of
     * {@link #dropPieces} must implement this method and return a
     * reference to their game-specific piece dropper implementation.
     */
    protected PieceDropLogic getPieceDropLogic ()
    {
        return null;
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
//        if (_userise && (tickStamp >= _risesent + RISE_INTERVAL)) {
//            _risesent += RISE_INTERVAL;
//            raiseBoard(tickStamp);
//        }

        // check the bounce timer
        if ((_bounceStamp != 0) &&
            ((tickStamp - _bounceStamp) >= _bounceInterval)) {
            bounceTimerExpired();
        }
    }

    // documentation inherited
    public Component getComponent ()
    {
        return null;
    }

    // documentation inherited
    public boolean needsPaint ()
    {
        return false;
    }

    // documentation inherited
    public void didResume (long delta)
    {
        super.didResume(delta);

        // fast-forward the board rising timestamps
        _risestamp += delta;
        if (_zipstamp != 0) {
            _zipstamp += delta;
        }

        Log.info("Drop puzzle resuming, attempting to evolve board.");

        // we're un-paused, so we should try evolving the board to start
        // things up again
        tryEvolveBoard(true);
    }

    /**
     * Sets whether the board rising is paused.
     */
    public void setRisingPaused (boolean paused)
    {
        if (paused && _rpstamp == 0) {
            // pause the board
            _rpstamp = _dview.getTimeStamp();

        } else if (!paused && _rpstamp != 0) {
            // un-pause the board
            long delta = _dview.getTimeStamp() - _rpstamp;
            _risestamp += delta;
            if (_zipstamp != 0) {
                _zipstamp += delta;
            }
            _rpstamp = 0;
        }
    }

    /**
     * Causes the board to zip quickly to the next row.
     */
    public void zipToNextRow ()
    {
        // don't overwrite an existing zip
        if (_zipstamp == 0) {
            // if we're paused, inherit the pause time, otherwise use the
            // current time
            if (_rpstamp != 0) {
                _zipstamp = _rpstamp;
            } else {
                _zipstamp = _dview.getTimeStamp();
            }
        }
    }

    /**
     * Called periodically on the frame tick.  Raises the board row based
     * on the time since the current row traversal began.
     */
    /*
    protected void raiseBoard (long tickStamp)
    {
        // don't raise if rising is paused or the action is cleared
        if (_rpstamp != 0 || !_ctrl.hasAction()) {
            return;
        }

        // initialize the rise stamp the first time we're risen
        if (_risestamp == 0) {
            _risestamp = tickStamp;
            _risesent = _risestamp;
        }

        // determine how far we've risen
        long msecs = tickStamp - _risestamp;
        float travpix = msecs * _risevel;

        // account for any zipping effect
        long zipsecs = 0;
        if (_zipstamp > 0) {
            zipsecs = tickStamp - _zipstamp;
            // make sure we don't zip past the top
            float zippix = (zipsecs * _risevel * 15);
            if (travpix < _risedist) {
                travpix += zippix;
                travpix = Math.min(travpix, _risedist);
            }
        }

        float pctdone = travpix / _risedist;

        boolean rose = false;
        if (pctdone >= 1.0f) {
            rose = true;
            if (_zipstamp > 0) {
                // clear out any zip stamp
                _zipstamp = 0;
                _risestamp = tickStamp;
                pctdone = 1f;

            } else {
                long used = (long)(_risedist / _risevel);
                _risestamp += used;
            }

            // give sub-classes a chance to do their thing
            boardWillRise();
        }

        // update the board display
        int ypos = ((int)(_risedist * pctdone)) % _risedist;
        _dview.setRiseOffset(ypos);

        if (rose) {
            // check to see if this means doom and defeat (even though the
            // game might be over, we still want to advance the piece
            // packet one last time and do the last rise so that the
            // server can tell that we kicked the proverbial bucket)
            boolean canRise = checkCanRise();

            // apply the rising row pieces to the board
            int[] pieces = _dview.getRisingPieces();
            _dboard.applyRisingPieces(pieces);

            // set up the next row of rising pieces
            _dview.setRisingPieces(null);
            advanceRisingPieces();

            // give sub-classes a chance to do their thing
            boardDidRise();

            if (canRise) {
                // evolve the board
                tryEvolveBoard();

            } else {
                Log.debug("Sticking fork in it [risers=" +
                          StringUtil.toString(pieces) + ".");

                // let the controller know that we're done for
                _ctrl.resetGame();
            }
        }

//         Log.info("Board rise [msecs=" + msecs + ", roff=" + ypos +
//                  ", pctdone=" + pctdone + ", zipsecs=" + zipsecs + "].");
    }
    */

    /**
     * Called to determine whether or not rising a new row into the board
     * is legal. The default implementation will return false if the top
     * row of the board contains any pieces.
     */
    protected boolean checkCanRise ()
    {
        return !_dboard.rowContainsPieces(0, PIECE_NONE);
    }

    /**
     * Called only for board-rising puzzles before effecting the rising of
     * the board by one row.  Derived classes may wish to override this
     * method to add any desired behaviour, but should be sure to call
     * <code>super.boardWillRise()</code>.
     */
    protected void boardWillRise ()
    {
        // nothing for now
    }

    /**
     * Called only for board-rising puzzles when the board has finished
     * rising one row.  Derived classes may wish to override this method
     * to add any desired behaviour, but should be sure to call
     * <code>super.boardDidRise()</code>.
     */
    protected void boardDidRise ()
    {
        // nothing for now
    }

    /** The yohoho context. */
    protected PuzzleContext _ctx;

    /** Our puzzle controller. */
    protected PuzzleController _ctrl;

    /** The drop panel. */
    protected DropPanel _dpanel;

    /** The drop board view. */
    protected DropBoardView _dview;

    /** The drop board. */
    protected DropBoard _dboard;

    /** Whether or not we are in the middle of board evolution. */
    protected boolean _evolving;

    /** Whether the game is using drop block functionality. */
    protected boolean _usedrop;

    /** Whether the game is using board rising functionality. */
    protected boolean _userise;

    /** The board dimensions in pieces. */
    protected int _bwid, _bhei;

    /** Our player index in the game. */
    protected int _pidx;

    /** The distance the board row travels in pixels. */
    protected int _risedist;

    /** The speed with which the board rises in pixels per millisecond. */
    protected float _risevel;

    /** The drop block sprite associated with the landing block, if any. */
    protected DropBlockSprite _blocksprite;

    /** The piece dropper used to drop pieces in the board if the puzzle
     * chooses to make use of piece dropping functionality. */
    protected PieceDropper _dropper;

    /** The time at which the board rise was paused. */
    protected long _rpstamp;

    /** The time at which the last board rise began. */
    protected long _risestamp;

    /** The time at which we last fired off a board rising event. */
    protected long _risesent;

    /** The time at which we were requested to start zipping. */
    protected long _zipstamp;

    /** The duration of the bounce interval. */
    protected int _bounceInterval;

    /** The time at which we last started bouncing, or 0. */
    protected long _bounceStamp;

    /** The row at which we last bounced, or {@link Integer#MIN_VALUE}. */
    protected int _bounceRow;

    /** The timestamp used to keep track of when the drop block was
     * removed so that we can fast-forward it when restored. */
    protected long _blockStamp;

    /** Whether the drop blocks are currently dropping quickly. */
    protected boolean _fastDrop;

    /** Used to evolve the board following the completion of animations. */
    protected AnimationAdapter _evolveObserver = new AnimationAdapter() {
        public void animationCompleted (Animation anim, long when) {
            animationDidFinish(anim);
        }
    };

    /** Used to listen to drop sprites and react to their move events. */
    protected DropSpriteObserver _dropMovedHandler = new DropSpriteObserver() {
        public void pieceMoved (
            DropSprite sprite, long when, int col, int row) {
            handleDropSpriteMoved(sprite, when, col, row);
        }
    };

    /** The default board row rising velocity. */
    protected static final float DEFAULT_RISE_VELOCITY = 100f / 1000f;

    /** The default board row rising distance. */
    protected static final int DEFAULT_RISE_DISTANCE = 20;

    /** The delay in milliseconds between board rising intervals. */
    protected static final long RISE_INTERVAL = 50L;

    /** Defines the distance of a piece that we allow to bounce before we
     * land it. */
    protected static final float BOUNCE_FRACTION = 0.125f;
}
