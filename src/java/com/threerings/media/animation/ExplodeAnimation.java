//
// $Id: ExplodeAnimation.java,v 1.16 2004/08/27 02:12:38 mdb Exp $
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

package com.threerings.media.animation;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import com.samskivert.util.StringUtil;
import com.threerings.util.RandomUtil;

import com.threerings.media.image.Mirage;

/**
 * An animation that displays an object exploding into chunks, fading out
 * as they fly apart.  The animation ends when all chunks have exited the
 * animation bounds, or when the given delay time (if any is specified)
 * has elapsed.
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
        Mirage image, ExplodeInfo info, int x, int y, int width, int height)
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
        _cxpos = new int[_chunkcount];
        _cypos = new int[_chunkcount];
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
            _cxpos[ii] = _ox + (xpos * _cwid);
            _cypos[ii] = _oy + (ypos * _chei);

            // initialize chunk velocity
            _sxvel[ii] = RandomUtil.getFloat(_info.xvel) *
                ((xpos < (_info.xchunk / 2)) ? -1.0f : 1.0f);
            _syvel[ii] = -(RandomUtil.getFloat(_info.yvel));
        }

        // initialize the chunk rotation angle
        _angle = 0.0f;
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
            if (_info.delay != -1) {
                _end = _start + _info.delay;
            }
        }

        // figure out the distance the chunks have travelled
        long msecs = timestamp - _start;

        if (_info.delay != -1) {
            // calculate the alpha level with which to render the chunks
            float pctdone = msecs / (float)_info.delay;
            _alpha = Math.max(0.1f, Math.min(1.0f, 1.0f - pctdone));
        }

        // move all chunks and check whether any remain to be animated
        int inside = 0;
        for (int ii = 0; ii < _chunkcount; ii++) {
            // determine the chunk travel distance
            int xtrav = (int)(_sxvel[ii] * msecs);
            int ytrav = (int)
                ((_syvel[ii] * msecs) + (0.5f * _info.yacc * (msecs * msecs)));

            // determine the chunk movement direction
            int xpos = ii % _info.xchunk;
            int ypos = ii / _info.xchunk;

            // update the chunk position
            _cxpos[ii] = _ox + (xpos * _cwid) + xtrav;
            _cypos[ii] = _oy + (ypos * _chei) + ytrav;

            // note whether this chunk is still within our bounds
            _wrect.setBounds(_cxpos[ii], _cypos[ii], _cwid, _chei);
            if (_bounds.intersects(_wrect)) {
                inside++;
            }
        }

        // increment the rotation angle
        _angle += _info.rvel;

        // note whether we're finished
        _finished = (inside == 0) || (_info.delay != -1 && timestamp >= _end);

        // dirty ourselves
        invalidate();
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        Shape oclip = gfx.getClip();
        Composite ocomp = null;

        if (_info.delay != -1) {
            // set the alpha composite to reflect the current fade-out
            ocomp = gfx.getComposite();
            gfx.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha));
        }

        for (int ii = 0; ii < _chunkcount; ii++) {
            // get the chunk position within the image
            int xpos = ii % _info.xchunk;
            int ypos = ii / _info.xchunk;

            // calculate image chunk offset
            int xoff = -(xpos * _cwid);
            int yoff = -(ypos * _chei);

            // translate the origin to center on the chunk
            int tx = _cxpos[ii] + _hcwid, ty = _cypos[ii] + _hchei;
            gfx.translate(tx, ty);

            // set up the desired rotation
            gfx.rotate(_angle);

            if (_image != null) {
                // draw the image chunk
                gfx.clipRect(-_hcwid, -_hchei, _cwid, _chei);
                _image.paint(gfx, -_hcwid + xoff, -_hchei + yoff);

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

        if (_info.delay != -1) {
            // restore the original composite
            gfx.setComposite(ocomp);
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

    /** The current x-axis position of each chunk. */
    protected int[] _cxpos;

    /** The current y-axis position of each chunk. */
    protected int[] _cypos;

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
    protected Mirage _image;

    /** The starting animation time. */
    protected long _start;

    /** The ending animation time. */
    protected long _end;

    /** The percent alpha with which to render the chunks. */
    protected float _alpha;

    /** A reusable working rectangle. */
    protected Rectangle _wrect = new Rectangle();
}
