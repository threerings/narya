//
// $Id: BobbleAnimation.java,v 1.1 2002/01/11 16:17:33 shaper Exp $

package com.threerings.media.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import com.threerings.media.Log;
import com.threerings.media.util.RandomUtil;

/**
 * An animation that bobbles an image around within a specific horizontal
 * and vertical pixel range for a given period of time.
 */
public class BobbleAnimation extends Animation
{
    /**
     * Constructs a bobble animation.
     *
     * @param image the image to animate.
     * @param sx the starting x-position.
     * @param sy the starting y-position.
     * @param rx the horizontal bobble range.
     * @param ry the vertical bobble range.
     * @param length the time to animate in milliseconds.
     */
    public BobbleAnimation (
        Image image, int sx, int sy, int rx, int ry, int length)
    {
        super(new Rectangle(sx - rx, sy - ry, sx + image.getWidth(null) + rx,
                            sy + image.getHeight(null) + ry));

        // save things off
        _image = image;
        _sx = sx;
        _sy = sy;
        _rx = rx;
        _ry = ry;

        // calculate animation ending time
        _end = System.currentTimeMillis() + length;

        // calculate the dirty rectangle bounds
        int wid = image.getWidth(null), hei = image.getHeight(null);
        // _bounds = new Rectangle(sx - rx, sy - ry, sx + wid + rx, sy + hei + ry);
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        _finished = (timestamp >= _end);
        invalidate();

        // calculate the latest position
        int dx = RandomUtil.getInt(_rx);
        int dy = RandomUtil.getInt(_ry);
        _x = (_sx + dx) * ((RandomUtil.getInt(2) == 0) ? -1 : 1);
        _y = (_sy + dy) * ((RandomUtil.getInt(2) == 0) ? -1 : 1);
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.drawImage(_image, _x, _y, null);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);

        buf.append(", x=").append(_x);
        buf.append(", y=").append(_y);
        buf.append(", rx=").append(_rx);
        buf.append(", ry=").append(_ry);
        buf.append(", sx=").append(_sx);
        buf.append(", sy=").append(_sy);
    }

    /** The current position of the image. */
    protected int _x, _y;

    /** The horizontal and vertical bobbling range. */
    protected int _rx, _ry;

    /** The starting position. */
    protected int _sx, _sy;

    /** The image to animate. */
    protected Image _image;

    /** The ending animation time. */
    protected long _end;
}
