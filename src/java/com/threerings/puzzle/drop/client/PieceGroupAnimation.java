//
// $Id: PieceGroupAnimation.java,v 1.1 2004/08/18 01:34:19 mdb Exp $

package com.threerings.puzzle.drop.client;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.threerings.media.animation.Animation;
import com.threerings.media.sprite.ImageSprite;
import com.threerings.media.sprite.PathObserver;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.Path;

import com.threerings.puzzle.drop.data.DropBoard;
import com.threerings.puzzle.drop.data.DropPieceCodes;

/**
 * Animates all the pieces on a puzzle board doing some sort of global
 * effect like all flying into place or out into the ether.
 */
public abstract class PieceGroupAnimation extends Animation
    implements PathObserver
{
    /**
     * Creates a piece group animation which must be initialized with a
     * subsequent call to {@link #init}.
     */
    public PieceGroupAnimation (DropBoardView view, DropBoard board)
    {
        super(new Rectangle(0, 0, 0, 0)); // we don't render ourselves
        _view = view;
        _board = board;
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        // nothing doing
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public void pathCancelled (Sprite sprite, Path path)
    {
        _finished = (--_penders == 0);
    }

    // documentation inherited from interface
    public void pathCompleted (Sprite sprite, Path path, long when)
    {
        _finished = (--_penders == 0);
    }

    // documentation inherited
    protected void willStart (long tickStamp)
    {
        super.willStart(tickStamp);

        // create an image sprite for every piece on the board and set
        // them on their paths
        int width = _board.getWidth(), height = _board.getHeight();
        _sprites = new Sprite[width * height];
        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                int spos = yy*width+xx;
                _sprites[spos] = createSprite(_view, xx, yy);
                if (_sprites[spos] != null) {
                    configureSprite(_sprites[spos], xx, yy);
                    _sprites[spos].addSpriteObserver(this);
                    _view.addSprite(_sprites[spos]);
                    _penders++;
                }
            }
        }
    }

    // documentation inherited
    protected void didFinish (long tickStamp)
    {
        super.didFinish(tickStamp);

        // remove all of our sprites
        for (int ii = 0; ii < _sprites.length; ii++) {
            if (_sprites[ii] != null) {
                _view.removeSprite(_sprites[ii]);
            }
        }
    }

    /**
     * Creates a sprite for each piece position. This method can return
     * null and no sprite will be used for the specified coordinates. The
     * default implementation creates an image sprite with the piece image
     * from the supplied coordinates.
     */
    protected Sprite createSprite (DropBoardView view, int xx, int yy)
    {
        int piece = _board.getPiece(xx, yy);
        return (piece == DropPieceCodes.PIECE_NONE) ? null :
            new ImageSprite(_view.getPieceImage(piece));
    }

    /**
     * An animation must override this method to configure each sprite
     * with a path, potentially a render order, and whatever other
     * configurations are needed.
     */
    protected abstract void configureSprite (Sprite sprite, int xx, int yy);

    protected DropBoardView _view;
    protected DropBoard _board;
    protected Sprite[] _sprites;
    protected int _penders;
}
