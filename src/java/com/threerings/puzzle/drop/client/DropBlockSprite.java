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

import java.awt.Rectangle;

/**
 * The drop block sprite represents a block of multiple pieces that can be
 * rotated to any of the four cardinal compass directions.  As such, it
 * may span multiple columns or rows depending on its orientation.  The
 * block has a "central" piece around which it rotates, with the other
 * pieces referred to as "external" pieces.
 */
public class DropBlockSprite extends DropSprite
{
    /**
     * Constructs a drop block sprite and starts it dropping.
     *
     * @param view the board view upon which this sprite will be displayed.
     * @param col the column of the central piece.
     * @param row the row of the central piece.
     * @param orient the orientation of the sprite.
     * @param pieces the pieces displayed by the sprite.
     */
    public DropBlockSprite (
        DropBoardView view, int col, int row, int orient, int[] pieces)
    {
	super(view, col, row, pieces, 0);

        _orient = orient;
    }

    /**
     * Constructs a drop block sprite and starts it dropping.
     *
     * @param view the board view upon which this sprite will be displayed.
     * @param col the column of the central piece.
     * @param row the row of the central piece.
     * @param orient the orientation of the sprite.
     * @param pieces the pieces displayed by the sprite.
     * @param renderOrder the rendering order of the sprite.
     */
    public DropBlockSprite (
        DropBoardView view, int col, int row, int orient, int[] pieces,
        int renderOrder)
    {
	super(view, col, row, pieces, 0, renderOrder);

        _orient = orient;
    }

    // documentation inherited
    protected void init ()
    {
        super.init();

	setOrientation(_orient);
    }

    /**
     * Returns an array of the row numbers containing the block pieces.
     * The first index is the row of the central piece.  The array is
     * cached and re-used internally and so the caller should make their
     * own copy if they care to modify it.
     */
    public int[] getRows ()
    {
        return _rows;
    }

    /**
     * Returns an array of the column numbers containing the block pieces.
     * The first index is the column of the central piece.  The array is
     * cached and re-used internally and so the caller should make their
     * own copy if they care to modify it.
     */
    public int[] getColumns ()
    {
        return _cols;
    }

    /**
     * Returns the bounds of the block in board piece coordinates.  The
     * bounds rectangle is cached and re-used internally and so the caller
     * should make their own copy if they care to modify it.
     */
    public Rectangle getBoardBounds ()
    {
        return _dbounds;
    }

    /**
     * Returns the row the external piece is located in.
     */
    public int getExternalRow ()
    {
        return _erow;
    }

    /**
     * Returns the column the external piece is located in.
     */
    public int getExternalColumn ()
    {
        return _ecol;
    }

    // documentation inherited
    public void setColumn (int col)
    {
        super.setColumn(col);
        updateDropInfo();
    }

    // documentation inherited
    public void setRow (int row)
    {
        super.setRow(row);
        updateDropInfo();
    }

    // documentation inherited
    public void setBoardLocation (int row, int col)
    {
        super.setBoardLocation(row, col);
        updateDropInfo();
    }

    /**
     * Updates the sprite image offset to reflect the direction in which
     * the external piece is hanging.
     */
    public void setOrientation (int orient)
    {
	super.setOrientation(orient);

	int edx = 0, edy = 0;
	if (orient == NORTH) {
	    edy = -1;
	} else if (orient == WEST) {
	    edx = -1;
	}

        // update the sprite image offset
	setRowOffset(edy);
	setColumnOffset(edx);

        // update the external piece position and drop block bounds
        updateDropInfo();
    }

    // documentation inherited
    public void toString (StringBuilder buf)
    {
	super.toString(buf);
	buf.append(", erow=").append(_erow);
	buf.append(", ecol=").append(_ecol);
    }

    /**
     * Can this sprite pop-up a row on a forgiving rotation?
     */
    public boolean canPopup ()
    {
        return (_popups > 0);
    }

    /**
     * Called if we pop up to decrement the remaining popups we have.
     */
    public void didPopup ()
    {
        _popups--;
    }

    /**
     * Re-calculates the external piece position and bounds of the drop
     * block.
     */
    protected void updateDropInfo ()
    {
        // update the external piece location
        _erow = calculateExternalRow();
        _ecol = calculateExternalColumn();

        // update the piece row and column arrays
        _rows[0] = _row;
        _rows[1] = _erow;
        _cols[0] = _col;
        _cols[1] = _ecol;
        
        // calculate the drop block board bounds
        int maxrow = Math.max(_row, _erow);
        int mincol = Math.min(_col, _ecol);

        int bpwid, bphei;
        if (_orient == NORTH || _orient == SOUTH) {
            bpwid = 1;
            bphei = 2;
        } else {
            bpwid = 2;
            bphei = 1;
        }

        // create the bounds rectangle if necessary
        _dbounds.setBounds(mincol, maxrow, bpwid, bphei);
    }

    /**
     * Returns the row the external piece is located in based on the
     * current central piece location and sprite orientation.
     */
    protected int calculateExternalRow ()
    {
	if (_orient == NORTH) {
	    return (_row - 1);
	} else if (_orient == SOUTH) {
	    return (_row + 1);
	} else {
            return _row;
        }
    }

    /**
     * Returns the column the external piece is located in based on the
     * current central piece location and sprite orientation.
     */
    protected int calculateExternalColumn ()
    {
	if (_orient == WEST) {
	    return (_col - 1);
	} else if (_orient == EAST) {
	    return (_col + 1);
	} else {
            return _col;
        }
    }

    /** How many times this sprite can be popped-up a row in a forgiving
     * rotation. */
    protected byte _popups = 2;

    /** The drop block bounds in board coordinates. */
    protected Rectangle _dbounds = new Rectangle();

    /** The drop block piece rows. */
    protected int[] _rows = new int[2];

    /** The drop block piece columns. */
    protected int[] _cols = new int[2];

    /** The external piece location in board coordinates. */
    protected int _ecol, _erow;
}
