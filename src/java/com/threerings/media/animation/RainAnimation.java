//
// $Id: RainAnimation.java,v 1.1 2002/01/18 16:22:23 shaper Exp $

package com.threerings.media.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.threerings.media.util.RandomUtil;

/**
 * An animation that displays raindrops spattering across an image.
 */
public class RainAnimation extends Animation
{
    /**
     * Constructs a rain animation with reasonable defaults for the number
     * of raindrops and their dimensions.
     *
     * @param bounds the bounding rectangle for the animation.
     * @param delay the number of seconds the animation should last.
     */
    public RainAnimation (Rectangle bounds, long delay)
    {
        super(bounds);

        init(delay, DEFAULT_COUNT, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Constructs a rain animation.
     *
     * @param bounds the bounding rectangle for the animation.
     * @param delay the number of seconds the animation should last.
     * @param count the number of raindrops to render in each frame.
     * @param wid the width of a raindrop's bounding rectangle.
     * @param hei the height of a raindrop's bounding rectangle.
     */
    public RainAnimation (
        Rectangle bounds, long delay, int count, int wid, int hei)
    {
        super(bounds);
        init(delay, count, wid, hei);
    }

    protected void init (long delay, int count, int wid, int hei)
    {
        // save things off
        _count = count;
        _wid = wid;
        _hei = hei;

        // create the raindrop array
        _drops = new int[count];

        // calculate ending time
        _end = System.currentTimeMillis() + delay;
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        _finished = (timestamp >= _end);
        _animmgr.addDirtyRect(new Rectangle(_bounds));

        // calculate the latest raindrop locations
        for (int ii = 0; ii < _count; ii++) {
            int x = RandomUtil.getInt(_bounds.width);
            int y = RandomUtil.getInt(_bounds.height);
            _drops[ii] = (x << 16 | y);
        }
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(Color.white);
        for (int ii = 0; ii < _count; ii++) {
            int x = _drops[ii] >> 16;
            int y = _drops[ii] & 0xFFFF;
            gfx.drawLine(x, y, x + _wid, y + _hei);
        }
    }

    /** The number of raindrops. */
    protected static final int DEFAULT_COUNT = 300;

    /** The raindrop streak dimensions. */
    protected static final int DEFAULT_WIDTH = 13;
    protected static final int DEFAULT_HEIGHT = 10;

    /** The number of raindrops. */
    protected int _count;

    /** The dimensions of each raindrop's bounding rectangle. */
    protected int _wid, _hei;

    /** The raindrop locations. */
    protected int[] _drops;

    /** The ending animation time. */
    protected long _end;
}
