//
// $Id: NextBlockView.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.border.Border;

import com.threerings.media.image.Mirage;
import com.threerings.util.DirectionCodes;

/**
 * The next block view displays an image representing the next drop block
 * to appear in the game.
 */
public class NextBlockView extends JComponent
    implements DirectionCodes
{
    /**
     * Constructs a next block view.
     */
    public NextBlockView (DropBoardView view, int pwid, int phei, int orient)
    {
        // save things off
        _view = view;
        _pwid = pwid;
        _phei = phei;
        _orient = orient;

        // configure the component
        setOpaque(false);
    }

    /**
     * Sets the pieces displayed by the view.
     */
    public void setPieces (int[] pieces)
    {
        _pieces = pieces;
        repaint();
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // draw the pieces
        Graphics2D gfx = (Graphics2D)g;
        if (_pieces != null) {
            Dimension size = getSize();
            int xpos = (_orient == VERTICAL) ? 0 : (size.width - _pwid);
            int ypos = (_orient == VERTICAL) ? (size.height - _phei) : 0;

            for (int ii = 0; ii < _pieces.length; ii++) {
                Mirage image = _view.getPieceImage(_pieces[ii]);
                image.paint(gfx, xpos, ypos);
                if (_orient == VERTICAL) {
                    ypos -= _phei;
                } else {
                    xpos -= _pwid;
                }
            }
        }
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        int wid = (_orient == VERTICAL) ? _pwid : (2 * _pwid);
        int hei = (_orient == VERTICAL) ? (2 * _phei) : _phei;
        return new Dimension(wid, hei);
    }

    /** The drop board view from which we obtain piece images. */
    protected DropBoardView _view;

    /** The pieces displayed by this view. */
    protected int[] _pieces;

    /** The piece dimensions in pixels. */
    protected int _pwid, _phei;

    /** The view orientation; one of {@link #HORIZONTAL} or {@link
     * #VERTICAL}. */
    protected int _orient;
}
