//
// $Id: ExplodeAnimation.java,v 1.9 2002/05/09 04:42:10 shaper Exp $

package com.threerings.media.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;

import com.samskivert.util.StringUtil;

import com.threerings.util.RandomUtil;

import com.threerings.media.Log;

/**
 * An animation that displays an object exploding into chunks.  The
 * animation ends when all chunks have exited the animation bounds.
 */
public class ExplodeAnimation extends Animation
{
    /**
     * A class that describes an explosion's attributes.
     */
    public static class ExplodeInfo
    {
        /** The bounds within which to animate. */
        public Rectangle bounds;

        /** The number of image chunks on each axis. */
        public int xchunk, ychunk;

        /** The maximum chunk velocity on each axis in pixels per
         * millisecond. */
        public float xvel, yvel;

        /** The y-axis chunk acceleration in pixels per millisecond. */
        public float yacc;

        /** The chunk rotational velocity in rotations per millisecond. */
        public float rvel;

        /** The animation length in milliseconds, or -1 if the animation
         * should continue until all pieces are outside the bounds. */
        public long delay;

        /** Returns a string representation of this instance. */
        public String toString ()
        {
            return StringUtil.fieldsToString(this);
        }
    }

    /**
     * Constructs an explode animation with the chunks represented as
     * filled rectangles of the specified color.
     *
     * @param color the color to render the chunks in.
     * @param info the explode info object.
     * @param x the x-position of the object.
     * @param y the y-position of the object.
     * @param width the width of the object.
     * @param height the height of the object.
     */
    public ExplodeAnimation (
        Color color, ExplodeInfo info, int x, int y, int width, int height)
    {
        super(info.bounds);

        _color = color;
        init(info, x, y, width, height);
    }

    /**
     * Constructs an explode animation with the chunks represented as
     * portions of the actual image.
     *
     * @param image the image to animate.
     * @param info the explode info object.
     * @param x the x-position of the object.
     * @param y the y-position of the object.
     * @param width the width of the object.
     * @param height the height of the object.
     */
    public ExplodeAnimation (
        Image image, ExplodeInfo info, int x, int y, int width, int height)
    {
        super(info.bounds);

        _image = image;
        init(info, x, y, width, height);
    }

    /**
     * Initializes the animation with the attributes of the given explode
     * info object.
     */
    protected void init (ExplodeInfo info, int x, int y, int width, int height)
    {
        _info = info;
        _ox = x;
        _oy = y;
        _owid = width;
        _ohei = height;

        _info.rvel = (float)((2.0f * Math.PI) * _info.rvel);
        _chunkcount = (_info.xchunk * _info.ychunk);
        _cpos = new int[_chunkcount];
        _sxvel = new float[_chunkcount];
        _syvel = new float[_chunkcount];

        // determine chunk dimensions
        _cwid = _owid / _info.xchunk;
        _chei = _ohei / _info.ychunk;
        _hcwid = _cwid / 2;
        _hchei = _chei / 2;

        // initialize all chunks
        for (int ii = 0; ii < _chunkcount; ii++) {
            // initialize chunk position
            int xpos = ii % _info.xchunk;
            int ypos = ii / _info.xchunk;
            _cpos[ii] = ((_ox + (xpos * _cwid)) << 16 |
                         (_oy + (ypos * _chei)));

            // initialize chunk velocity
            _sxvel[ii] = RandomUtil.getFloat(_info.xvel) *
                ((xpos < (_info.xchunk / 2)) ? -1.0f : 1.0f);
            _syvel[ii] = -(RandomUtil.getFloat(_info.yvel));
        }

        // initialize the chunk rotation angle
        _angle = 0.0f;
    }

    /**
     * Sets the animation starting time.
     */
    public void setStartTime (long timestamp)
    {
        _start = timestamp;
    }

    // documentation inherited
    public void fastForward (long timeDelta)
    {
        _start += timeDelta;
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        // figure out the distance the chunks have travelled
        long msecs = timestamp - _start;

        // move all chunks and check whether any remain to be animated
        int inside = 0;
        for (int ii = 0; ii < _chunkcount; ii++) {
            // retrieve the current chunk position
            int x = _cpos[ii] >> 16;
            int y = _cpos[ii] & 0xFFFF;

            // determine the chunk travel distance
            int xtrav = (int)(_sxvel[ii] * msecs);
            int ytrav = (int)((_syvel[ii] * msecs) +
                              (0.5f * _info.yacc * (msecs * msecs)));

            // determine the chunk movement direction
            int xpos = ii % _info.xchunk;
            int ypos = ii / _info.xchunk;

            // update the chunk position
            x = _ox + (xpos * _cwid) + xtrav;
            y = _oy + (ypos * _chei) + ytrav;
            _cpos[ii] = (x << 16 | y);

            // note whether this chunk is still within our bounds
            if (_bounds.contains(x, y)) {
                inside++;
            }
        }

        // increment the rotation angle
        _angle += _info.rvel;

        _finished = (inside == 0) ||
            (_info.delay > -1 && timestamp >= (_start + _info.delay));
        invalidate();
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        Shape oclip = gfx.getClip();
        for (int ii = 0; ii < _chunkcount; ii++) {
            // get the chunk location on-screen
            int x = _cpos[ii] >> 16;
            int y = _cpos[ii] & 0xFFFF;

            // get the chunk position within the image
            int xpos = ii % _info.xchunk;
            int ypos = ii / _info.xchunk;

            // calculate image chunk offset
            int xoff = -(xpos * _cwid);
            int yoff = -(ypos * _chei);

            // translate the origin to center on the chunk
            int tx = x + _hcwid, ty = y + _hchei;
            gfx.translate(tx, ty);

            // set up the desired rotation
            gfx.rotate(_angle);

            if (_image != null) {
                // draw the image chunk
                gfx.clipRect(-_hcwid, -_hchei, _cwid, _chei);
                gfx.drawImage(_image, -_hcwid + xoff, -_hchei + yoff, null);

            } else {
                // draw the color chunk
                gfx.setColor(_color);
                gfx.fillRect(-_hcwid, -_hchei, _cwid, _chei);
            }

            // restore the original transform and clip
            gfx.rotate(-_angle);
            gfx.translate(-tx, -ty);
            gfx.setClip(oclip);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);

        buf.append(", ox=").append(_ox);
        buf.append(", oy=").append(_oy);
        buf.append(", cwid=").append(_cwid);
        buf.append(", chei=").append(_chei);
        buf.append(", hcwid=").append(_hcwid);
        buf.append(", hchei=").append(_hchei);
        buf.append(", chunkcount=").append(_chunkcount);
        buf.append(", info=").append(_info);
    }

    /** The current chunk rotation. */
    protected float _angle;

    /** The starting x-axis velocity of each chunk. */
    protected float[] _sxvel;

    /** The starting y-axis velocity of each chunk. */
    protected float[] _syvel;

    /** The current position of each chunk. */
    protected int[] _cpos;

    /** The individual chunk dimensions in pixels. */
    protected int _cwid, _chei;

    /** The individual chunk dimensions in pixels, halved for handy use in
     * repeated calculations. */
    protected int _hcwid, _hchei;

    /** The total number of image chunks. */
    protected int _chunkcount;

    /** The explode info. */
    protected ExplodeInfo _info;

    /** The exploding object position and dimensions. */
    protected int _ox, _oy, _owid, _ohei;

    /** The color to render the object chunks in if we're using a color. */
    protected Color _color;

    /** The image to animate if we're using an image. */
    protected Image _image;

    /** The starting animation time. */
    protected long _start;
}
