//
// $Id: SparkAnimation.java,v 1.2 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.animation;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.Arrays;

import com.threerings.util.RandomUtil;

import com.threerings.media.animation.Animation;
import com.threerings.media.image.Mirage;

/**
 * Displays a set of spark images originating from a specified position
 * and flying outward in random directions, fading out as they go, for a
 * specified period of time.
 */
public class SparkAnimation extends Animation
{
    /**
     * Constructs a spark animation with the supplied parameters.
     *
     * @param bounds the bounding rectangle for the animation.
     * @param x the starting x-position for the sparks.
     * @param y the starting y-position for the sparks.
     * @param jog the maximum distance by which to "jog" the initial spark
     * positions, or 0 if no jogging is desired.
     * @param minxvel the minimum starting x-velocity of the sparks.
     * @param minyvel the minimum starting y-velocity of the sparks.
     * @param xvel the maximum x-velocity of the sparks.
     * @param yvel the maximum y-velocity of the sparks.
     * @param g the gravitational acceleration, or 0 if none is desired.
     * @param images the spark images to be animated.
     * @param delay the duration of the animation in milliseconds.
     */
    public SparkAnimation (Rectangle bounds, int x, int y, int jog,
                           float minxvel, float minyvel,
                           float xvel, float yvel,
                           float g, Mirage[] images, long delay)
    {
        super(bounds);

        // save things off
        _ox = x;
        _oy = y;
        _g = g;
        _images = images;
        _delay = delay;

        // initialize various things
        _icount = images.length;
        _xpos = new int[_icount];
        _ypos = new int[_icount];
        _sxvel = new float[_icount];
        _syvel = new float[_icount];

        for (int ii = 0; ii < _icount; ii++) {
            // initialize spark position
            int ajog = (jog == 0) ? 0 : RandomUtil.getInt(jog);
            _xpos[ii] = x + ajog * getDirectionMult();
            _ypos[ii] = y + ajog * getDirectionMult();

            // choose a random x-axis velocity between the minimum and
            // maximum passed in, and moving left or right at random
            _sxvel[ii] = Math.max(RandomUtil.getFloat(xvel), minxvel) *
                getDirectionMult();

            // choose a random y-axis velocity between the minimum and
            // maximum passed in. if there is any gravitational
            // acceleration, make the y-axis velocity negative; else,
            // choose to move up or down at random.
            _syvel[ii] = (Math.max(RandomUtil.getFloat(yvel), minyvel)) *
                ((g != 0) ? -1.0f : getDirectionMult());
        }

        _alpha = 1.0f;
        _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);
    }

    protected int getDirectionMult ()
    {
        return (RandomUtil.getInt(2) == 0) ? -1 : 1;
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        if (_start > 0) {
            _start += timeDelta;
            _end += timeDelta;
        }
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        if (_start == 0) {
            // initialize our starting time
            _start = timestamp;
            _end = _start + _delay;
        }

        // figure out the distance the chunks have travelled
        long msecs = Math.max(timestamp - _start, 0);

        // calculate the alpha level with which to render the chunks
        float pctdone = msecs / (float)_delay;
        _alpha = Math.max(0.1f, Math.min(1.0f, 1.0f - pctdone));
        _comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha);

        // move all sparks and check whether any remain to be animated
        for (int ii = 0; ii < _icount; ii++) {
            // determine the travel distance
            int xtrav = (int)(_sxvel[ii] * msecs);
            int ytrav = (int)
                ((_syvel[ii] * msecs) + (0.5f * _g * (msecs * msecs)));

            // update the position
            _xpos[ii] = _ox + xtrav;
            _ypos[ii] = _oy + ytrav;
        }

        // note whether we're finished
        _finished = (timestamp >= _end);

        // dirty ourselves
        // TODO: only do this if at least one spark actually moved
        invalidate();
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        Composite ocomp = gfx.getComposite();
        // set the alpha composite to reflect the current fade-out
        gfx.setComposite(_comp);
        // draw all sparks
        for (int ii = 0; ii < _icount; ii++) {
            _images[ii].paint(gfx, _xpos[ii], _ypos[ii]);
        }
        // restore the original composite
        gfx.setComposite(ocomp);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);

        buf.append(", ox=").append(_ox);
        buf.append(", oy=").append(_oy);
        buf.append(", alpha=").append(_alpha);
    }

    /** The spark images we're animating. */
    protected Mirage[] _images;

    /** The number of images we're animating. */
    protected int _icount;

    /** The gravitational acceleration in pixels per millisecond. */
    protected float _g;

    /** The starting x-axis velocity of each chunk. */
    protected float[] _sxvel;

    /** The starting y-axis velocity of each chunk. */
    protected float[] _syvel;

    /** The current x-axis position of each spark. */
    protected int[] _xpos;

    /** The current y-axis position of each spark. */
    protected int[] _ypos;

    /** The starting spark position. */
    protected int _ox, _oy;

    /** The starting animation time. */
    protected long _start;

    /** The ending animation time. */
    protected long _end;

    /** The percent alpha with which to render the images. */
    protected float _alpha;

    /** The alpha composite with which to render the images. */
    protected AlphaComposite _comp;

    /** The duration of the spark animation in milliseconds. */
    protected long _delay;
}
