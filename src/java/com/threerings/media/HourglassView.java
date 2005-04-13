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
                          Mirage botImage, Rectangle botRect, Color sandColor)
    {
        this(fmgr, host, x, y, glassImage, topImage, topRect, new Point(0, 0),
             botImage, botRect, new Point(0, 0), sandColor);
    }

    /**
     * Constructs an hourglass view.
     */
    public HourglassView (
        FrameManager fmgr, JComponent host, int x, int y, Mirage glassImage,
        Mirage topImage, Rectangle topRect, Point topOff,
        Mirage botImage, Rectangle botRect, Point botOff, Color sandColor)
    {
        super(fmgr, host, new Rectangle(x, y, glassImage.getWidth(),
                                        glassImage.getHeight()));

        // Store relevant coordinate data
        _topRect = topRect;
        _topOff = topOff;
        _botRect = botRect;
        _botOff = botOff;
        _sandColor = sandColor;

        // Save hourglass images
        _hourglass = glassImage;
        _sandTop = topImage;
        _sandBottom = botImage;

        // Initialize the falling grain of sand
        _sandY = _topRect.y + _topRect.height;
        _sandTime = 0;

        // Percentage changes smaller than one pixel in the hourglass
        // itself definitely won't be noticed
        _changeThreshold = 1.0f / _bounds.height;
    }

    // documentation inherited
    public void tick (long tickStamp)
    {
        // Let the parent hand its stuff
        super.tick(tickStamp);

        // Check if the falling sand dot should move
        if (_sandTime + SAND_RATE < tickStamp)
        {
            // Move the sand grain, possibly wrapping around
            if (++_sandY > getSandBottomTop(_renderComplete)) {
                _sandY = _topRect.y + _topRect.height;
            }

            // This is when the grain was last updated
            _sandTime = tickStamp;

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
        gfx.setClip(oclip);

        // Paint the current bottom sand level
        top = getSandBottomTop(completed);
        gfx.clipRect(_botRect.x, top, _botRect.width,
                     _botRect.height-(top-_botRect.y));
        _sandBottom.paint(gfx, _botOff.x, _botOff.y);
        gfx.setClip(oclip);

        // Paint the sand trickle
        gfx.setColor(_sandColor);
        gfx.fillRect(_hourglass.getWidth() / 2, _sandY, 1, 1);

        gfx.translate(-_bounds.x, -_bounds.y);
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

    /** The y-coordinate of the current sand bit trickling down. */
    protected int _sandY;

    /** The color of the rendered sand. */
    protected Color _sandColor;

    /** Our images. */
    protected Mirage _hourglass, _sandTop, _sandBottom;

    /** The last time the sand pixel moved. */
    protected long _sandTime = 0;

    /** How many milliseconds it takes the animated sand drop to move
     * one pixel. */
    protected static final long SAND_RATE = 100;
}
