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

package com.threerings.puzzle.drop.client;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;

import com.samskivert.util.ObserverList;
import com.threerings.util.DirectionUtil;

import com.threerings.media.image.Mirage;
import com.threerings.media.sprite.Sprite;

import com.threerings.puzzle.Log;

/**
 * The drop sprite is a sprite that displays one or more pieces falling
 * toward the bottom of the board.
 */
public class DropSprite extends Sprite
{
    /**
     * Constructs a drop sprite and starts it dropping.
     *
     * @param view the board view upon which this sprite will be displayed.
     * @param col the column of the sprite.
     * @param row the row of the bottom-most piece.
     * @param pieces the pieces displayed by the sprite.
     * @param dist the distance the sprite is to drop in rows.
     */
    public DropSprite (
        DropBoardView view, int col, int row, int[] pieces, int dist)
    {
        this(view, col, row, pieces, dist, -1);
    }

    /**
     * Constructs a drop sprite and starts it dropping.
     *
     * @param view the board view upon which this sprite will be displayed.
     * @param col the column of the sprite.
     * @param row the row of the bottom-most piece.
     * @param pieces the pieces displayed by the sprite.
     * @param dist the distance the sprite is to drop in rows.
     * @param renderOrder the render order.
     */
    public DropSprite (
        DropBoardView view, int col, int row, int[] pieces, int dist,
        int renderOrder)
    {
        _view = view;
        _col = col;
        _row = row;
	_pieces = pieces;
	_dist = (dist == 0) ? 1 : dist;
        _orient = NORTH;
        _unit = _view.getPieceHeight();
        setRenderOrder(renderOrder);
    }

    // documentation inherited
    protected void init ()
    {
        super.init();

        // size the bounds to fit our pieces
        updateBounds();
	// set up the piece location
	setBoardLocation(_row, _col);
	// calculate vertical render offset based on the number of pieces
	setRowOffset(-(_pieces.length - 1));
    }

    /**
     * Returns the remaining number of columns to drop.
     */
    public int getDistance ()
    {
	return _dist;
    }

    /**
     * Returns the column the piece is located in.
     */
    public int getColumn ()
    {
	return _col;
    }

    /**
     * Returns the row the piece is located in.
     */
    public int getRow ()
    {
	return _row;
    }

    /**
     * Returns the pieces the sprite is displaying.
     */
    public int[] getPieces ()
    {
	return _pieces;
    }

    /**
     * Returns the velocity of this sprite.
     */
    public float getVelocity ()
    {
	return _vel;
    }

    /**
     * Sets the row and column the piece is located in.
     */
    public void setBoardLocation (int row, int col)
    {
	_row = row;
	_col = col;
	updatePosition();
    }

    /**
     * Sets the column the piece is located in.
     */
    public void setColumn (int col)
    {
	_col = col;
	updatePosition();
    }

    /**
     * Set the row the piece is located in.
     */
    public void setRow (int row)
    {
	_row = row;
	updatePosition();
    }

    /**
     * Sets the column offset of the sprite image.
     */
    public void setColumnOffset (int count)
    {
	_offx = count;
        updateRenderOffset();
	updateRenderOrigin();
    }

    /**
     * Sets the row offset of the sprite image.
     */
    public void setRowOffset (int count)
    {
	_offy = count;
        updateRenderOffset();
	updateRenderOrigin();
    }

    /**
     * Sets the pieces the sprite is displaying.
     */
    public void setPieces (int[] pieces)
    {
        _pieces = pieces;
    }

    /**
     * Sets the velocity of this sprite.  The time at which the current
     * row was entered is modified so that the sprite position will remain
     * the same when calculated using the new velocity since the piece
     * sprite may have its velocity modified in the middle of a row
     * traversal.
     */
    public void setVelocity (float velocity)
    {
        // bail if we've already got the requested velocity
        if (_vel == velocity) {
            return;
        }

        if (_rowstamp > 0) {
            // get our current distance along the row
            long now = _view.getTimeStamp();
            float pctdone = getPercentDone(now);

            // revise the current row entry time to account for the new velocity
            float travpix = pctdone * _unit;
            long msecs = (long)(travpix / velocity);
            _rowstamp = now - msecs;
        }

	// update the velocity
	_vel = velocity;
    }

    /**
     * Starts the piece dropping toward the next row.
     */
    public void drop ()
    {
        // Log.info("Dropping piece [piece=" + this + "].");

        // drop one row by default
        if (_dist <= 0) {
            _dist = 1;
        }

	if (_stopstamp > 0) {
	    // we're dropping from a stand-still
            long delta = _view.getTimeStamp() - _stopstamp;
	    _rowstamp += delta;
            _stopstamp = 0;

	} else {
            // we're continuing a previous drop, so make use of any
            // previously existing time
            _rowstamp = _endstamp;
	}
    }

    /**
     * Returns true if this drop sprite is dropping, false if it has been
     * {@link #stop}ped or has not yet been {@link #drop}ped.
     */
    public boolean isDropping ()
    {
        return (_stopstamp == 0) && (_rowstamp != 0);
    }

    /**
     * Stops the piece from dropping.
     */
    public void stop ()
    {
        if (_stopstamp == 0) {
            _stopstamp = _view.getTimeStamp();
            // Log.info("Stopped piece [piece=" + this + "].");
        }
    }

    /**
     * Puts the drop sprite into (or takes it out of) bouncing
     * mode. Bouncing mode is used to put the sprite into limbo after it
     * lands but before we commit the landing, giving the user a last
     * moment change move or rotate the piece. While the sprite is
     * "bouncing" it will be rendered one pixel below it's at rest state.
     */
    public void setBouncing (boolean bouncing)
    {
        if (_bouncing = bouncing) {
            // if we've activated bouncing, shift the sprite slightly to
            // illustrate its new state
            shiftForBounce();

            // to prevent funny business in the event that we were a long
            // ways past the end of the row when we landed, we warp the
            // sprite back to the exact point of landing for the purposes
            // of the bounce and any subsequent antics
            _endstamp = _rowstamp = _view.getTimeStamp();

//             Log.info("Adjusted rowstap due to bounce " +
//                      "[time=" + _endstamp + "].");
        }
    }

    /**
     * Returns true if this sprite is bouncing.
     */
    public boolean isBouncing ()
    {
        return _bouncing;
    }

    /**
     * Updates the sprite's location to illustrate that it is currently in
     * the "bouncing" state.
     */
    protected void shiftForBounce ()
    {
        setLocation(_ox, _srcPos.y+1);
    }

    // documentation inherited
    public boolean inside (Shape shape)
    {
	return shape.contains(_bounds);
    }

    /**
     * Returns a value between <code>0.0</code> and <code>1.0</code>
     * representing how far the piece has moved toward the next row
     * as of the given time stamp.
     */
    public float getPercentDone (long timestamp)
    {
        // if we've never been ticked and so haven't yet initialized our
        // row start timestamp, just let the caller know that we've not
        // traversed our row at all
        if (_rowstamp == 0) {
            return 0.0f;
        }

	long msecs = Math.max(0, timestamp - _rowstamp);
	float travpix = msecs * _vel;
        float pctdone = (travpix / _unit);

//         Log.info("getPercentDone [timestamp=" + timestamp +
//                  ", rowstamp=" + _rowstamp + ", msecs=" + msecs +
//                  ", travpix=" + travpix + ", pctdone=" + pctdone + 
//                  ", vel=" + _vel + "].");

	return pctdone;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        // get the column and row increment based on the sprite's orientation
        int oidx = _orient/2;
        int incx = ORIENT_DX[oidx];
        int incy = ORIENT_DY[oidx];

        // determine offset from the start of each actual row and column
        int dx = _ox - _srcPos.x, dy = _oy - _srcPos.y;

        int pcol = _col, prow = _row;
        for (int ii = 0; ii < _pieces.length; ii++) {
            // ask the board for the render position of this piece
            _view.getPiecePosition(pcol, prow, _renderPos);
            // draw the piece image
            paintPieceImage(gfx, ii, pcol, prow, _orient,
                            _renderPos.x + dx, _renderPos.y + dy);
            // increment the target column and row
            pcol += incx;
            prow += incy;
        }
    }

    /**
     * Paints the specified piece with the supplied parameters.
     */
    protected void paintPieceImage (Graphics2D gfx, int pieceidx,
                                    int col, int row, int orient, int x, int y)
    {
        Mirage image = _view.getPieceImage(_pieces[pieceidx], col, row, orient);
        image.paint(gfx, x, y);
    }

    // documentation inherited
    public void tick (long timestamp)
    {
	super.tick(timestamp);

        // initialize our rowstamp if we haven't done so already
        if (_rowstamp == 0) {
            _rowstamp = timestamp;
        }

        // if we're bouncing or paused, do nothing here
        if (_bouncing || _stopstamp > 0) {
            return;
        }

        PieceMovedOp pmop = null;

	// figure out how far along the current board coordinate we should be
	float pctdone = getPercentDone(timestamp);
	if (pctdone >= 1.0f) {
	    // note that we've reached the next row
            advancePosition();

	    // update remaining drop distance
	    _dist--;

	    // calculate any remaining time to be used
	    long used = (long)(_unit / _vel);
	    _endstamp = _rowstamp + used;
            _rowstamp = _endstamp;

            // update our percent done because we've moved down a row
            pctdone -= 1.0;

            // inform observers that we've reached our destination
            pmop = new PieceMovedOp(this, timestamp, _col, _row);
	}

        // constrain the sprite's position to the destination row
        pctdone = Math.min(pctdone, 1.0f);

        // calculate the latest sprite position
        int nx = _srcPos.x + (int)((_destPos.x - _srcPos.x) * pctdone);
        int ny = _srcPos.y + (int)((_destPos.y - _srcPos.y) * pctdone);

//         Log.info("Drop sprite tick [dist=" + _dist + ", pctdone=" + pctdone +
//                  ", row=" + _row + ", col=" + _col +
//                  ", nx=" + nx + ", ny=" + ny + "].");

        // only update the sprite's location if it actually moved
        if (_ox != nx || _oy != ny) {
            setLocation(nx, ny);
        }

        // lastly notify our observers if we made it to the next row
        if (pmop != null) {
            _observers.apply(pmop);
        }
    }

    /**
     * Called when the sprite has finished traversing its current row to
     * advance its board coordinates to the next row.
     */
    protected void advancePosition ()
    {
        setRow(_row + 1);
        // Log.info("Moved to row " + _row);
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        if (_rowstamp > 0) {
            _rowstamp += timeDelta;
        }
    }

    // documentation inherited
    public void toString (StringBuilder buf)
    {
        super.toString(buf);
        buf.append(", orient=").append(DirectionUtil.toShortString(_orient));
	buf.append(", row=").append(_row);
	buf.append(", col=").append(_col);
	buf.append(", offx=").append(_offx);
	buf.append(", offy=").append(_offy);
	buf.append(", dist=").append(_dist);
    }

    /**
     * Updates internal pixel coordinates used when the piece is moving.
     */
    protected void updatePosition ()
    {
        _view.getPiecePosition(_col, _row, _srcPos);
        _view.getPiecePosition(_col, _row+1, _destPos);
	setLocation(_srcPos.x, _srcPos.y);
    }

    // documentation inherited
    public void setOrientation (int orient)
    {
        invalidate();
        super.setOrientation(orient);
        updateBounds();
        invalidate();
    }

    /**
     * Updates the bounds for this sprite based on the sprite display
     * dimensions in the view.
     */
    protected void updateBounds ()
    {
        Dimension size = _view.getPieceSegmentSize(
            _col, _row, _orient, _pieces.length);
        _bounds.width = size.width;
        _bounds.height = size.height;
    }

    /**
     * Adjusts our render origin such that our location is not in the
     * upper left of the sprite's rendered image but is in fact offset by
     * some number of rows and columns.
     */
    protected void updateRenderOffset ()
    {
        _oxoff = -(_view.getPieceWidth() * _offx);
        _oyoff = -(_view.getPieceHeight() * _offy);
    }

    /** Used to dispatch {@link DropSpriteObserver#pieceMoved}. */
    protected static class PieceMovedOp implements ObserverList.ObserverOp
    {
        public PieceMovedOp (DropSprite sprite, long when, int col, int row)
        {
            _sprite = sprite;
            _when = when;
            _col = col;
            _row = row;
        }

        public boolean apply (Object observer)
        {
            if (observer instanceof DropSpriteObserver) {
                ((DropSpriteObserver)observer).pieceMoved(
                    _sprite, _when, _col, _row);
            }
            return true;
        }

        protected DropSprite _sprite;
        protected long _when;
        protected int _col, _row;
    }

    /** The default piece velocity. */
    protected static final float DEFAULT_VELOCITY = 30f/1000f;

    /** The time at which we started the current row. */
    protected long _rowstamp;

    /** The time at which we reached the end of the previous row. */
    protected long _endstamp;

    /** The time at which we were stopped en route to our next row. */
    protected long _stopstamp;

    /** The board view upon which this sprite is displayed. */
    protected DropBoardView _view;

    /** The unit distance the sprite moves to reach the next row. */
    protected int _unit;

    /** The screen coordinates of the top-left of the row currently
     * occupied by the sprite. */
    protected Point _srcPos = new Point();

    /** The screen coordinates of the top-left of the row toward which the
     * sprite is falling. */
    protected Point _destPos = new Point();

    /** The piece render position; used as working data when determining
     * where to render each piece in the sprite. */
    protected Point _renderPos = new Point();

    /** The number of rows remaining to drop. */
    protected int _dist;

    /** The piece velocity. */
    protected float _vel = DEFAULT_VELOCITY;

    /** The offsets in columns or rows at which the piece is rendered. */
    protected int _offx, _offy;

    /** The current piece location in the board. */
    protected int _row, _col;

    /** The pieces this sprite is displaying. */
    protected int[] _pieces;

    /** Indicates that the drop sprite is bouncing; see {@link
     * #setBouncing}. */
    protected boolean _bouncing;

    // used to compute the column and row increment while rendering the
    // sprite's pieces based on its orientation
    //                                          W  N  E  S
    protected static final int[] ORIENT_DX = { -1, 0, 1, 0 };
    protected static final int[] ORIENT_DY = { 0, -1, 0, 1 };
}
