//
// $Id: PieceDropper.java,v 1.2 2003/12/16 03:45:31 mdb Exp $

package com.threerings.puzzle.drop.util;

import java.util.ArrayList;
import java.util.List;

import com.samskivert.util.StringUtil;

import com.threerings.puzzle.Log;
import com.threerings.puzzle.drop.data.DropBoard;
import com.threerings.puzzle.drop.data.DropPieceCodes;

/**
 * Handles dropping pieces in a board.
 */
public class PieceDropper
    implements DropPieceCodes
{
    /**
     * A class to hold information detailing the pieces to be dropped
     * in a particular column.
     */
    public static class PieceDropInfo
    {
	/** The starting row of the bottom piece being dropped. */
	public int row;

	/** The column number. */
	public int col;

	/** The distance to drop the pieces. */
	public int dist;

	/** The pieces to be dropped. */
	public int[] pieces;

        /**
         * Constructs a piece drop info object.
         */
	public PieceDropInfo (int col, int row, int dist)
	{
	    this.col = col;
	    this.row = row;
	    this.dist = dist;
	}

        /** Returns a string representation of this instance. */
	public String toString ()
	{
            return StringUtil.fieldsToString(this);
	}
    }

    /**
     * Constructs a piece dropper that uses the supplied piece drop logic
     * to specialise itself for a particular puzzle.
     */
    public PieceDropper (PieceDropLogic logic)
    {
        _logic = logic;
    }

    /**
     * Destructively modifies the supplied board to contain pieces dropped
     * in the first of potentially multiple drop positions and returns a
     * list of {@link PieceDropInfo} objects detailing all column segments
     * to be dropped.  Note that a single list is used internally to store
     * the drop info objects, and so callers that care to do anything
     * long-term with the drop info should create their own copy of the
     * list or somesuch.
     *
     * @param DropPieceProvider if the board should always be filled
     * (as specified by the PieceDropLogic) this will provide
     * information on the newly filled in pieces.
     */
    public List dropPieces (DropBoard board, DropPieceProvider provider)
    {
        int bhei = board.getHeight(), bwid = board.getWidth();
        _drops.clear();
	for (int yy = bhei - 1; yy >= 0; yy--) {
	    for (int xx = 0; xx < bwid; xx++) {
		// find all drops in this column
		getColumnDrops(board, _drops, xx, yy);
	    }
	}

        if (_logic.boardAlwaysFilled()) {
            addFillingDrops(board, provider, _drops);
        }

	return _drops;
    }

    /**
     * Analyzes but does not modify the supplied board and returns a list
     * of {@link PieceDropInfo} objects detailing all column segments to
     * be dropped.  Note that a single list is used internally to store
     * the drop info objects, and so callers that care to do anything
     * long-term with the drop info should create their own copy of the
     * list or somesuch.
     *
     * @param DropPieceProvider if the board should always be filled
     * (as specified by the PieceDropLogic) this will provide
     * information on the newly filled in pieces.
     */
    public List getDroppedPieces (DropBoard board, DropPieceProvider provider)
    {
        // grab a snapshot of the board within which we're dropping
        // pieces to avoid modifying the source board directly while we're
        // figuring out what to drop where
        if (_board == null) {
            _board = (DropBoard)board.clone();
        } else {
            board.copyInto(_board);
        }
        return dropPieces(_board, provider);
    }

    /**
     * Populates the <code>drops</code> list with {@link PieceDropInfo}
     * objects detailing the column segments to be dropped per empty space
     * below.  Destructively modifies the given board to reflect the final
     * positions of the column segments once dropped.
     */
    protected void getColumnDrops (DropBoard board, List drops, int x, int y)
    {
	// skip empty or fixed pieces
	int piece = board.getPiece(x, y);
        if (!_logic.isDroppablePiece(piece)) {
	    return;
	}

	if (_logic.isConstrainedPiece(piece)) {
            // find out where this constrained block starts and ends
            int start = _logic.getConstrainedEdge(board, x, y, LEFT);
            int end = _logic.getConstrainedEdge(board, x, y, RIGHT);
            int bwid = board.getWidth();
            if (start < 0 || end >= bwid) {
                Log.warning("Board reported bogus constrained edge " +
                            "[x=" + x + ", y=" + y +
                            ", start=" + start + ", end=" + end + "].");
                board.dump();
                start = Math.max(start, 0);
                end = Math.min(end, bwid);
            }

            // get the smallest drop distance across all of the block columns
            int dist = board.getHeight() - 1;
            for (int xpos = start; xpos <= end; xpos++) {
                dist = Math.min(dist, board.getDropDistance(xpos, y));
            }
	    if (dist == 0) {
		return;
	    }

	    // scoot along the bottom edge of the block dropping each column
            for (int xpos = start; xpos <= end; xpos++) {
		addDropInfo(board, drops, true, xpos, y, dist);
	    }

	} else {
	    // get the distance to drop the pieces
	    int dist = board.getDropDistance(x, y);
	    if (dist == 0) {
		return;
	    }

	    // add the column segment to the list of drops
	    addDropInfo(board, drops, false, x, y, dist);
	}
    }

    /**
     * Adds a {@link PieceDropInfo} object to the drop list detailing
     * dropping of the column segment at the specified location.
     *
     * @param board the working board.
     * @param allowConst whether to allow dropping constrained pieces in
     * the specified column segment.
     * @param x the column x-coordinate.
     * @param y the column segment bottom y-coordinate.
     * @param dist the distance to drop the pieces.
     */
    protected void addDropInfo (DropBoard board, List drops,
                                boolean allowConst, int x, int y, int dist)
    {
        // sanity check our column
        if (x < 0 || x >= board.getWidth()) {
            Log.warning("Requested to add bogus drop info [board=" + board +
                        ", drops=" + StringUtil.toString(drops) +
                        ", allowConst=" + allowConst +
                        ", x=" + x + ", y=" + y + ", dist=" + dist + "].");
            Thread.dumpStack();
        }

	// traverse up the column looking for an empty or block piece
	// that will terminate this column segment
	int height = getDropHeight(board, allowConst, x, y, dist);

	// create the piece drop info object
	PieceDropInfo pdi = new PieceDropInfo(x, y, dist);

	// copy in the relevant pieces
	pdi.pieces = new int[height];
	int idx = 0;
	for (int yy = y; yy > (y - height); yy--) {
	    pdi.pieces[idx++] = board.getPiece(x, yy);
	}

	// update the working copy of the board with the eventual
	// piece locations
	dropPieces(board, pdi);

	// add the column segment to the pot
	drops.add(pdi);
    }

    /**
     * If we want to keep the board filled at all times,
     * this method will be called to create drop objects for the
     * newly-filled in pieces.
     */
    protected void addFillingDrops (
        DropBoard board, DropPieceProvider provider, List drops)
    {
        boolean out = false;
        // drop in new pieces for empty spaces at the top
        for (int xx=0, bwid=board.getWidth(); xx < bwid; xx++) {
            // get the distance to drop the pieces
            int dist = board.getDropDistance(xx, -1);
            if (dist != 0) {
                PieceDropInfo pdi = new PieceDropInfo(xx, -1, dist);
                pdi.pieces = new int[dist];
                for (int ii=0; ii < dist; ii++) {
                    try {
                        pdi.pieces[ii] = provider.getNextPiece();
                    } catch (DropPieceProvider.OutOfPiecesException oop) {
                        // well, how far did we get?
                        int[] something = new int[ii];
                        System.arraycopy(pdi.pieces, 0, something, 0, ii);
                        pdi.pieces = something;
                        out = true;
                        break;
                    }
                }

                dropPieces(board, pdi);
                drops.add(pdi);

                // if we're outta pieces, bail.
                if (out) {
                    return;
                }
            }
        }
    }

    /**
     * Returns the height of the piece segment to be dropped at the
     * given coordinates.
     */
    protected int getDropHeight (
        DropBoard board, boolean allowConst, int x, int y, int dist)
    {
	int height = 0;
	for (int yy = y; yy >= 0; yy--) {
	    int curpiece = board.getPiece(x, yy);
            if (!_logic.isClimbablePiece(allowConst, curpiece, true)) {
		return height;
	    }

	    height++;

            if (!_logic.isClimbablePiece(allowConst, curpiece, false)) {
                return height;
            }
	}

	return height;
    }

    /**
     * Updates the given board to reflect the eventual destination of the
     * given pieces.
     */
    protected void dropPieces (DropBoard board, PieceDropInfo pdi)
    {
	// clear out the original piece positions
        if (!board.setSegment(
                VERTICAL, pdi.col, pdi.row, pdi.pieces.length, PIECE_NONE)) {
            Log.warning("Bogosity encountered when clearing pieces for drop " +
                        "[bwid=" + board.getWidth() +
                        ", bhei=" + board.getHeight() + ", pdi=" + pdi + "].");
        }
        // place the pieces in their destination positions
	applyPieces(board, pdi);
    }

    /**
     * Applies the pieces in the given piece drop info object to the given
     * board at their eventual destination positions.
     */
    protected void applyPieces (DropBoard board, PieceDropInfo pdi)
    {
	int start = pdi.row, end = (pdi.row - pdi.pieces.length);
	int idx = 0;
        boolean error = false;
	for (int yy = (start + pdi.dist); yy > (end + pdi.dist); yy--) {
	    error = !board.setPiece(pdi.col, yy, pdi.pieces[idx++]) || error;
	}
        if (error) {
            Log.warning("Bogosity encountered while applying dropped " +
                        "pieces to board [bwid=" + board.getWidth() +
                        ", bhei=" + board.getHeight() + ", pdi=" + pdi + "].");
        }
    }

    /** The piece drop logic used to allow puzzle-specific piece dropping
     * hooks. */
    protected PieceDropLogic _logic;

    /** The list of piece drop info objects detailing the piece drops
     * resulting from the last call to {@link #getDroppedPieces}. */
    protected ArrayList _drops = new ArrayList();

    /** The board on which pieces are being dropped. */
    protected DropBoard _board;
}
