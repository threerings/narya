//
// $Id: DropBoardView.java,v 1.8 2004/10/20 02:23:36 mdb Exp $
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Iterator;

import com.threerings.media.image.Mirage;
import com.threerings.media.sprite.ImageSprite;
import com.threerings.media.sprite.PathAdapter;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.LinePath;
import com.threerings.media.util.Path;

import com.threerings.puzzle.Log;
import com.threerings.puzzle.client.PuzzleBoardView;
import com.threerings.puzzle.client.ScoreAnimation;
import com.threerings.puzzle.data.Board;
import com.threerings.puzzle.data.PuzzleConfig;
import com.threerings.puzzle.util.PuzzleContext;

import com.threerings.puzzle.drop.data.DropBoard;
import com.threerings.puzzle.drop.data.DropConfig;
import com.threerings.puzzle.drop.data.DropPieceCodes;

/**
 * The drop board view displays a drop puzzle game in progress for a
 * single player.
 */
public abstract class DropBoardView extends PuzzleBoardView
    implements DropPieceCodes
{
    /** The color used to render normal scoring text. */
    public static final Color SCORE_COLOR = Color.white;

    /** The color used to render chain reward scoring text. */
    public static final Color CHAIN_COLOR = Color.yellow;

    /**
     * Constructs a drop board view.
     */
    public DropBoardView (PuzzleContext ctx, int pwid, int phei)
    {
        super(ctx);

        // save off piece dimensions
        _pwid = pwid;
        _phei = phei;

        // determine distance to float score animations
        _scoreDist = 2 * _phei;
    }

    /**
     * Initializes the board with the board dimensions.
     */
    public void init (PuzzleConfig config)
    {
        DropConfig dconfig = (DropConfig)config;

        // save off the board dimensions in pieces
        _bwid = dconfig.getBoardWidth();
        _bhei = dconfig.getBoardHeight();

        super.init(config);
    }

    /**
     * Returns the width in pixels of a single board piece.
     */
    public int getPieceWidth ()
    {
        return _pwid;
    }

    /**
     * Returns the height in pixels of a single board piece.
     */
    public int getPieceHeight ()
    {
        return _phei;
    }

    /**
     * Called by the {@link DropSprite} to populate <code>pos</code> with
     * the screen coordinates in pixels at which a piece at <code>(col,
     * row)</code> in the board should be drawn.  Derived classes may wish
     * to override this method to allow specialised positioning of
     * sprites.
     */
    public void getPiecePosition (int col, int row, Point pos)
    {
        pos.setLocation(col * _pwid, (row * _phei) - _roff);
    }

    /**
     * Called by the {@link DropSprite} to get the dimensions of the area
     * that will be occupied by rendering a piece segment of the given
     * orientation and length whose bottom-leftmost corner is at
     * <code>(col, row)</code>.
     */
    public Dimension getPieceSegmentSize (int col, int row, int orient, int len)
    {
        if (orient == NORTH || orient == SOUTH) {
            return new Dimension(_pwid, len * _phei);
        } else {
            return new Dimension(len * _pwid, _phei);
        }
    }

    /**
     * Creates a new piece sprite and places it directly in it's correct
     * position.
     */
    public void createPiece (int piece, int sx, int sy)
    {
        if (sx < 0 || sy < 0 || sx >= _bwid || sy >= _bhei) {
            Log.warning("Requested to create piece in invalid location " +
                        "[sx=" + sx + ", sy=" + sy + "].");
            Thread.dumpStack();
            return;
        }
        createPiece(piece, sx, sy, sx, sy, 0L);
    }

    /**
     * Refreshes the piece sprite at the specified location, if no sprite
     * exists at the location, one will be created. <em>Note:</em> this
     * method assumes the default {@link ImageSprite} is being used to
     * display pieces. If {@link #createPieceSprite} is overridden to
     * return a non-ImageSprite, this method must also be customized.
     */
    public void updatePiece (int sx, int sy)
    {
        updatePiece(_dboard.getPiece(sx, sy), sx, sy);
    }

    /**
     * Updates the piece sprite at the specified location, if no sprite
     * exists at the location, one will be created. <em>Note:</em> this
     * method assumes the default {@link ImageSprite} is being used to
     * display pieces. If {@link #createPieceSprite} is overridden to
     * return a non-ImageSprite, this method must also be customized.
     */
    public void updatePiece (int piece, int sx, int sy)
    {
        if (sx < 0 || sy < 0 || sx >= _bwid || sy >= _bhei) {
            Log.warning("Requested to update piece in invalid location " +
                        "[sx=" + sx + ", sy=" + sy + "].");
            Thread.dumpStack();
            return;
        }
        int spos = sy * _bwid + sx;
        if (_pieces[spos] != null) {
            ((ImageSprite)_pieces[spos]).setMirage(
                getPieceImage(piece, sx, sy, NORTH));
        } else {
            createPiece(piece, sx, sy);
        }
    }

    /**
     * Creates a new piece sprite and moves it into position on the board.
     */
    public void createPiece (int piece, int sx, int sy, int tx, int ty,
                             long duration)
    {
        if (tx < 0 || ty < 0 || tx >= _bwid || ty >= _bhei) {
            Log.warning("Requested to create and move piece to invalid " +
                        "location [tx=" + tx + ", ty=" + ty + "].");
            Thread.dumpStack();
            return;
        }
        Sprite sprite = createPieceSprite(piece, sx, sy);
        if (sprite != null) {
            // position the piece properly to start
            Point start = new Point();
            getPiecePosition(sx, sy, start);
            sprite.setLocation(start.x, start.y);
            // now add it to the view
            addSprite(sprite);
            // and potentially move it into place
            movePiece(sprite, sx, sy, tx, ty, duration);
        }
    }

    /**
     * Instructs the view to move the piece at the specified starting
     * position to the specified destination position. There must be a
     * sprite at the starting position, if there is a sprite at the
     * destination position, it must also be moved immediately following
     * this call (as in the case of a swap) to avoid badness.
     *
     * @return the piece sprite that is being moved.
     */
    public Sprite movePiece (int sx, int sy, int tx, int ty, long duration)
    {
        int spos = sy * _bwid + sx;
        Sprite piece = _pieces[spos];
        if (piece == null) {
            Log.warning("Missing source sprite for drop [sx=" + sx +
                        ", sy=" + sy + ", tx=" + tx + ", ty=" + ty + "].");
            return null;
        }
        _pieces[spos] = null;
        movePiece(piece, sx, sy, tx, ty, duration);
        return piece;
    }

    /**
     * A helper function for moving pieces into place.
     */
    protected void movePiece (Sprite piece, final int sx, final int sy,
                              final int tx, final int ty, long duration)
    {
        final Exception where = new Exception();

        // if the sprite needn't move, then just position it and be done
        Point start = new Point();
        getPiecePosition(sx, sy, start);
        if (sx == tx && sy == ty) {
            int tpos = ty * _bwid + tx;
            if (_pieces[tpos] != null) {
                Log.warning("Zoiks! Asked to add a piece where we already " +
                            "have one [sx=" + sx + ", sy=" + sy +
                            ", tx=" + tx + ", ty=" + ty + "].");
                Log.logStackTrace(where);
                return;
            }
            _pieces[tpos] = piece;
            piece.setLocation(start.x, start.y);
            return;
        }

        // otherwise create a path and do some bits
        Point end = new Point();
        getPiecePosition(tx, ty, end);
        piece.addSpriteObserver(new PathAdapter() {
            public void pathCompleted (Sprite sprite, Path path, long when) {
                sprite.removeSpriteObserver(this);
                int tpos = ty * _bwid + tx;
                if (_pieces[tpos] != null) {
                    Log.warning("Oh god, we're dropping onto another piece " +
                                "[sx=" + sx + ", sy=" + sy +
                                ", tx=" + tx + ", ty=" + ty + "].");
                    Log.logStackTrace(where);
                    return;
                }
                _pieces[tpos] = sprite;
                if (_actionSprites.remove(sprite)) {
                    maybeFireCleared();
                }
                pieceArrived(when, sprite, tx, ty);
            }
        });
        _actionSprites.add(piece);
        piece.move(new LinePath(start, end, duration));
    }

    /**
     * Called when a piece is finished moving into its requested position.
     * Derived classes may wish to take this opportunity to play a sound
     * or whatnot.
     */
    protected void pieceArrived (long tickStamp, Sprite sprite, int px, int py)
    {
    }

    /**
     * Returns the image used to display the given piece at coordinates
     * <code>(0, 0)</code> with an orientation of {@link #NORTH}.  This
     * serves as a convenience routine for those puzzles that don't bother
     * rendering their pieces differently when placed at different board
     * coordinates or in different orientations.
     */
    public Mirage getPieceImage (int piece)
    {
        return getPieceImage(piece, 0, 0, NORTH);
    }

    /**
     * Returns the image used to display the given piece at the specified
     * column and row with the given orientation.
     */
    public abstract Mirage getPieceImage (
        int piece, int col, int row, int orient);

    // documentation inherited
    public void setBoard (Board board)
    {
        // when a new board arrives, we want to remove all drop sprites
        // so that they don't modify the new board with their old ideas
        for (Iterator iter = _actionSprites.iterator(); iter.hasNext(); ) {
            Sprite s = (Sprite) iter.next();
            if (s instanceof DropSprite) {
                // remove it from _sprites safely
                iter.remove();
                // but then use the standard removal method
                removeSprite(s);
            }
        }

        // remove all of this board's piece sprites
        int pcount = (_pieces == null) ? 0 : _pieces.length;
        for (int ii = 0; ii < pcount; ii++) {
            if (_pieces[ii] != null) {
                removeSprite(_pieces[ii]);
            }
        }

        super.setBoard(board);

        _dboard = (DropBoard)board;

        // create the pieces for the new board
        Point spos = new Point();
        int width = _dboard.getWidth(), height = _dboard.getHeight();
        _pieces = new Sprite[width * height];
        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                Sprite piece = createPieceSprite(
                    _dboard.getPiece(xx, yy), xx, yy);
                if (piece != null) {
                    int ppos = yy * width + xx;
                    getPiecePosition(xx, yy, spos);
                    piece.setLocation(spos.x, spos.y);
                    addSprite(piece);
                    _pieces[ppos] = piece;
                }
            }
        }
    }

    /**
     * Returns the piece sprite at the specified location.
     */
    public Sprite getPieceSprite (int xx, int yy)
    {
        return _pieces[yy * _dboard.getWidth() + xx];
    }

    /**
     * Clears the specified piece from the board.
     */
    public void clearPieceSprite (int xx, int yy)
    {
        int ppos = yy * _dboard.getWidth() + xx;
        if (_pieces[ppos] != null) {
            removeSprite(_pieces[ppos]);
            _pieces[ppos] = null;
        }
    }

    /**
     * Clears out a piece from the board along with its piece sprite.
     */
    public void clearPiece (int xx, int yy)
    {
        _dboard.setPiece(xx, yy, PIECE_NONE);
        clearPieceSprite(xx, yy);
    }

    /**
     * Creates a new drop sprite used to animate the given pieces falling
     * in the specified column.
     */
    public DropSprite createPieces (int col, int row, int[] pieces, int dist)
    {
	return new DropSprite(this, col, row, pieces, dist);
    }

    /**
     * Dirties the rectangle encompassing the segment with the given
     * direction and length whose bottom-leftmost corner is at <code>(col,
     * row)</code>.
     */
    public void dirtySegment (int dir, int col, int row, int len)
    {
        int x = _pwid * col, y = (_phei * row) - _roff;
        int wid = (dir == VERTICAL) ? _pwid : len * _pwid;
        int hei = (dir == VERTICAL) ? _phei * len : _phei;
        _remgr.invalidateRegion(x, y, wid, hei);
    }

    /**
     * Creates and returns an animation which makes use of a label sprite
     * that is assigned a path that floats it a short distance up the
     * view, with the label initially centered within the view.
     *
     * @param score the score text to display.
     * @param color the color of the text.
     */
    public ScoreAnimation createScoreAnimation (String score, Color color)
    {
        return createScoreAnimation(
            score, color, MEDIUM_FONT_SIZE, 0, _bhei - 1, _bwid, _bhei);
    }

    /**
     * Creates and returns an animation showing the specified score
     * floating up the view, with the label initially centered within the
     * view.
     *
     * @param score the score text to display.
     * @param color the color of the text.
     * @param fontSize the size of the text; a value between 0 and {@link
     * #getPuzzleFontSizeCount} - 1.
     */
    public ScoreAnimation createScoreAnimation (
        String score, Color color, int fontSize)
    {
        return createScoreAnimation(
            score, color, fontSize, 0, _bhei - 1, _bwid, _bhei);
    }

    /**
     * Creates and returns an animation showing the specified score
     * floating up the view.
     *
     * @param score the score text to display.
     * @param color the color of the text.
     * @param x the left coordinate in board coordinates of the rectangle
     * within which the score is to be centered.
     * @param y the bottom coordinate in board coordinates of the
     * rectangle within which the score is to be centered.
     * @param width the width in board coordinates of the rectangle within
     * which the score is to be centered.
     * @param height the height in board coordinates of the rectangle
     * within which the score is to be centered.
     */
    public ScoreAnimation createScoreAnimation (
        String score, Color color, int x, int y, int width, int height)
    {
        return createScoreAnimation(
            score, color, MEDIUM_FONT_SIZE, x, y, width, height);
    }

    /**
     * Creates and returns an animation showing the specified score
     * floating up the view.
     *
     * @param score the score text to display.
     * @param color the color of the text.
     * @param fontSize the size of the text; a value between 0 and {@link
     * #getPuzzleFontSizeCount} - 1.
     * @param x the left coordinate in board coordinates of the rectangle
     * within which the score is to be centered.
     * @param y the bottom coordinate in board coordinates of the
     * rectangle within which the score is to be centered.
     * @param width the width in board coordinates of the rectangle within
     * which the score is to be centered.
     * @param height the height in board coordinates of the rectangle
     * within which the score is to be centered.
     */
    public ScoreAnimation createScoreAnimation (String score, Color color,
                                                int fontSize, int x, int y,
                                                int width, int height)
    {
        // create the score animation
        ScoreAnimation anim =
            createScoreAnimation(score, color, fontSize, x, y);

        // position the label within the specified rectangle
        Dimension lsize = anim.getLabel().getSize();
        Point pos = new Point();
        centerRectInBoardRect(
            x, y, width, height, lsize.width, lsize.height, pos);
        anim.setLocation(pos.x, pos.y);

        return anim;
    }

    /**
     * Creates the sprite that is used to display the specified piece. If
     * the piece represents no piece, this method should return null.
     */
    protected Sprite createPieceSprite (int piece, int px, int py)
    {
        if (piece == PIECE_NONE) {
            return null;
        }
        ImageSprite sprite = new ImageSprite(
            getPieceImage(piece, px, py, NORTH));
        sprite.setRenderOrder(-1);
        return sprite;
    }

    /**
     * Populates <code>pos</code> with the most appropriate screen
     * coordinates to center a rectangle of the given width and height (in
     * pixels) within the specified rectangle (in board coordinates).
     *
     * @param bx the bounding rectangle's left board coordinate.
     * @param by the bounding rectangle's bottom board coordinate.
     * @param bwid the bounding rectangle's width in board coordinates.
     * @param bhei the bounding rectangle's height in board coordinates.
     * @param rwid the width of the rectangle to position in pixels.
     * @param rhei the height of the rectangle to position in pixels.
     * @param pos the point to populate with the rectangle's final
     * position.
     */
    protected void centerRectInBoardRect (
        int bx, int by, int bwid, int bhei, int rwid, int rhei, Point pos)
    {
        getPiecePosition(bx, by + 1, pos);
        pos.x += (((bwid * _pwid) - rwid) / 2);
        pos.y -= ((((bhei * _phei) - rhei) / 2) + rhei);

        // constrain to fit wholly within the board bounds
        pos.x = Math.max(Math.min(pos.x, _bounds.width - rwid), 0);
    }

    /**
     * Rotates the given drop block sprite to the specified orientation,
     * updating the image as necessary.  Derived classes that make use of
     * block dropping functionality should override this method to do the
     * right thing.
     */
    public void rotateDropBlock (DropBlockSprite sprite, int orient)
    {
        // nothing for now
    }

    // documentation inherited
    public void paintBetween (Graphics2D gfx, Rectangle dirtyRect)
    {
        gfx.translate(0, -_roff);
        renderBoard(gfx, dirtyRect);
        renderRisingPieces(gfx, dirtyRect);
        gfx.translate(0, _roff);
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
	int wid = _bwid * _pwid;
        int hei = _bhei * _phei;
	return new Dimension(wid, hei);
    }

    /**
     * Renders the row of rising pieces to the given graphics context.
     * Sub-classes that make use of board rising functionality should
     * override this method to draw the rising piece row.
     */
    protected void renderRisingPieces (Graphics2D gfx, Rectangle dirtyRect)
    {
        // nothing for now
    }

    /**
     * Sets the board rising offset to the given y-position.
     */
    protected void setRiseOffset (int y)
    {
        if (y != _roff) {
            _roff = y;
            _remgr.invalidateRegion(_bounds);
        }
    }

    /** The drop board. */
    protected DropBoard _dboard;

    /** A sprite for every piece displayed in the drop board. */
    protected Sprite[] _pieces;

    /** The piece dimensions in pixels. */
    protected int _pwid, _phei;

    /** The board rising offset. */
    protected int _roff;

    /** The board dimensions in pieces. */
    protected int _bwid, _bhei;
}
