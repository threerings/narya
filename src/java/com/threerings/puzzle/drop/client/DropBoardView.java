//
// $Id: DropBoardView.java,v 1.3 2004/02/25 14:48:44 mdb Exp $

package com.threerings.puzzle.drop.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Iterator;

import com.threerings.media.image.Mirage;
import com.threerings.media.sprite.Sprite;

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
     * Dirties the rectangle encompassing the piece segment with the given
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
     * Dirties the rectangle encompassing the specified piece in the
     * board.
     */
    public void dirtyPiece (int col, int row)
    {
        _remgr.invalidateRegion(_pwid * col, (_phei * row) - _roff,
                                _pwid, _phei);
    }

    /**
     * Dirties a rectangular region of pieces.
     */
    public void dirtyPieces (int xx, int yy, int width, int height)
    {
        _remgr.invalidateRegion(xx*_pwid, yy*_phei, width*_pwid, height*_phei);
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

        super.setBoard(board);

        _dboard = (DropBoard)board;
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

    /** The piece dimensions in pixels. */
    protected int _pwid, _phei;

    /** The board rising offset. */
    protected int _roff;

    /** The board dimensions in pieces. */
    protected int _bwid, _bhei;
}
