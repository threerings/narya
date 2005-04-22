//
// $Id: HourglassView.java 18862 2005-01-27 00:34:51Z tedv $

package com.threerings.media;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JComponent;

import com.samskivert.util.Interval;
import com.samskivert.util.ResultListener;

import com.threerings.media.image.Mirage;
import com.threerings.media.util.MultiFrameImage;

/**
 * Displays an hourglass with the sand level representing the amount of
 * time remaining.
 */
public class HourglassView extends TimerView
{
    /**
     * Constructs an hourglass view.
     */
    public HourglassView (FrameManager fmgr, JComponent host, int x, int y,
                          Mirage glassImage, Mirage topImage, Rectangle topRect,
                          Mirage botImage, Rectangle botRect,
                          MultiFrameImage sandTrickle)
    {
        this(fmgr, host, x, y, glassImage, topImage, topRect, new Point(0, 0),
             botImage, botRect, new Point(0, 0), sandTrickle);
    }

    /**
     * Constructs an hourglass view.
     */
    public HourglassView (
        FrameManager fmgr, JComponent host, int x, int y, Mirage glassImage,
        Mirage topImage, Rectangle topRect, Point topOff,
        Mirage botImage, Rectangle botRect, Point botOff,
        MultiFrameImage sandTrickle)
    {
        super(fmgr, host, new Rectangle(x, y, glassImage.getWidth(),
                                        glassImage.getHeight()));

        // Store relevant coordinate data
        _topRect = topRect;
        _topOff = topOff;
        _botRect = botRect;
        _botOff = botOff;

        // Save hourglass images
        _hourglass = glassImage;
        _sandTop = topImage;
        _sandBottom = botImage;
        _sandTrickle = sandTrickle;

        // Initialize the falling grain of sand
        _sandY = 0;

        // Percentage changes smaller than one pixel in the hourglass
        // itself definitely won't be noticed
        _changeThreshold = 1.0f / _bounds.height;
    }

    // documentation inherited
    public void changeComplete (float complete)
    {
        super.changeComplete(complete);
        setSandTrickleY();
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        // Let the parent handle its stuff
        super.tick(tickStamp);

        // check if the sand should be updated
        if (tickStamp > _sandStamp) {

            // update the sand frame
            setSandTrickleY();
            _sandFrame = (_sandFrame + 1) % _sandTrickle.getFrameCount();
            _sandStamp = tickStamp + SAND_RATE;

            // Make sure the timer gets repainted
            invalidate();
        }
    }

    // documentation inherited
    public void paint (Graphics2D gfx, float completed)
    {
        // Handle processing from parent class
        super.paint(gfx, completed);

        // Paint the hourglass
        gfx.translate(_bounds.x, _bounds.y);
        _hourglass.paint(gfx, 0, 0);

        // Paint the remaining top sand level
        Shape oclip = gfx.getClip();
        int top = _topRect.y + (int)(_topRect.height * completed);
        gfx.clipRect(_topRect.x, top, _topRect.width,
                     _topRect.height - (top-_topRect.y));
        _sandTop.paint(gfx, _topOff.x, _topOff.y);

        // paint the current sand frame
        gfx.setClip(oclip);
        int sandtop = _topRect.y + _topRect.height;
        if (_sandY < _botRect.height) {
            gfx.clipRect(_botRect.x, sandtop, _botRect.width, _sandY);
        }
        _sandTrickle.paintFrame(gfx, _sandFrame,
            _botRect.x + (_botRect.width - _sandTrickle.getWidth(_sandFrame))/2,
            sandtop);
        gfx.setClip(oclip);

        // Paint the current bottom sand level
        top = getSandBottomTop(completed);
        gfx.clipRect(_botRect.x, top, _botRect.width,
                     _botRect.height-(top-_botRect.y));
        _sandBottom.paint(gfx, _botOff.x, _botOff.y);
        gfx.setClip(oclip);

        // untranslate
        gfx.translate(-_bounds.x, -_bounds.y);
    }

    /**
     * Set the y position of the trickle.
     */
    protected void setSandTrickleY ()
    {
        _sandY = (int) (_botRect.height * Math.min(1f, (_complete / .025)));
    }

    /**
     * Returns the current top coordinate of the bottom sand pile.
     */
    protected int getSandBottomTop (float completed)
    {
        return _botRect.y + _botRect.height -
            (int)(_botRect.height * completed);
    }

    /** The bounds of the sand within the top and bottom sand images. */
    protected Rectangle _topRect, _botRect;

    /** Offsets at which to render the sand images. */
    protected Point _topOff, _botOff;

    /** Our images. */
    protected Mirage _hourglass, _sandTop, _sandBottom;

    /** The sand trickle. */
    protected MultiFrameImage _sandTrickle;

    /** The last time the sand updated moved. */
    protected long _sandStamp;

    /** The next sand frame to be painted. */
    protected int _sandFrame;

    /** The position of the top of the sand. */
    protected int _sandY;

    /** How often the sand frame updates. */
    protected static final long SAND_RATE = 80;
}
