//
// $Id: ExplodeAnimation.java,v 1.2 2002/01/11 19:37:49 shaper Exp $

package com.threerings.media.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;

import com.threerings.media.Log;

/**
 * An animation that displays an image exploding into chunks.  The
 * animation ends when all image chunks have exited the animation bounds.
 */
public class ExplodeAnimation extends Animation
{
    /**
     * Constructs an explode animation.
     *
     * @param bounds the bounds within which to animate.
     * @param image the image to animate.
     * @param xchunk the number of image chunks on the x-axis.
     * @param ychunk the number of image chunks on the y-axis.
     * @param sx the starting x-position.
     * @param sy the starting y-position.
     * @param vel the chunk velocity in pixels per millisecond.
     * @param rvel the chunk rotation velocity in rotations per millisecond.
     */
    public ExplodeAnimation (
        Rectangle bounds, Image image, int xchunk, int ychunk,
        int sx, int sy, float vel, float rvel)
    {
        super(bounds);

        // save things off
        _image = image;
        _xchunk = xchunk;
        _ychunk = ychunk;
        _sx = sx;
        _sy = sy;
        _vel = vel;

        _rvel = (float)((2.0f * Math.PI) * rvel);
        _chunkcount = (_xchunk * _ychunk);
        _cpos = new int[_chunkcount];

        // save the animation starting time
        _start = System.currentTimeMillis();

        // determine chunk dimensions
        _cwid = image.getWidth(null) / _xchunk;
        _chei = image.getHeight(null) / _ychunk;
        _hcwid = _cwid / 2;
        _hchei = _chei / 2;

        // initialize the chunk positions
        int cx = sx + (image.getWidth(null) / 2);
        int cy = sy + (image.getHeight(null) / 2);
        int cpos = (cx << 16 | cy);
        for (int ii = 0; ii < _chunkcount; ii++) {
            int xpos = ii % _xchunk;
            int ypos = ii / _xchunk;
            _cpos[ii] = ((sx + (xpos * _cwid)) << 16 | (sy + (ypos * _chei)));
        }

        // initialize the chunk rotation angle
        _angle = 0.0f;
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        // figure out the distance the chunks have travelled
        long msecs = timestamp - _start;
        int travpix = (int)(msecs * _vel);

        // move all chunks and check whether any remain to be animated
        int inside = 0;
        for (int ii = 0; ii < _chunkcount; ii++) {
            // retrieve the current chunk position
            int x = _cpos[ii] >> 16;
            int y = _cpos[ii] & 0xFFFF;

            // determine the chunk movement direction
            int xpos = ii % _xchunk;
            int ypos = ii / _xchunk;
            int mx = 0, my = 0;
            if (xpos == 0) {
                mx = -1;
            } else if (xpos == (_xchunk - 1)) {
                mx = 1;
            }
            if (ypos == 0) {
                my = -1;
            } else if (ypos == (_ychunk - 1)) {
                my = 1;
            }

            // deal with the center point for odd dimensions
            if (mx == 0 && my == 0) {
                my = 1;
            }

            // update the chunk position
            x = _sx + (xpos * _cwid) + (travpix * mx);
            y = _sy + (ypos * _chei) + (travpix * my);
            _cpos[ii] = (x << 16 | y);

            // note whether this chunk is still within our bounds
            if (_bounds.contains(x, y)) {
                inside++;
            }
        }

        // increment the rotation angle
        _angle += _rvel;

        _finished = (inside == 0);
        invalidate();
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        for (int ii = 0; ii < _chunkcount; ii++) {
            // get the chunk location on-screen
            int x = _cpos[ii] >> 16;
            int y = _cpos[ii] & 0xFFFF;

            // get the chunk position within the image
            int xpos = ii % _xchunk;
            int ypos = ii / _xchunk;

            // calculate image chunk offset
            int xoff = -(xpos * _cwid);
            int yoff = -(ypos * _chei);

            // draw the chunk
            Shape oclip = gfx.getClip();

            // translate the origin to center on the chunk
            int tx = x + _hcwid, ty = y + _hchei;
            gfx.translate(tx, ty);

            // set up the desired rotation
            gfx.rotate(_angle);

            // draw the image chunk
            gfx.clipRect(-_hcwid, -_hchei, _cwid, _chei);
            gfx.drawImage(_image, -_hcwid + xoff, -_hchei + yoff, null);
            gfx.setClip(oclip);

            // restore the original transform
            gfx.rotate(-_angle);
            gfx.translate(-tx, -ty);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);

        buf.append(", sx=").append(_sx);
        buf.append(", sy=").append(_sy);
    }

    /** The current chunk rotation. */
    protected float _angle;

    /** The current position of each chunk. */
    protected int[] _cpos;

    /** The individual chunk dimensions in pixels. */
    protected int _cwid, _chei;

    /** The individual chunk dimensions in pixels, halved for handy use in
     * repeated calculations. */
    protected int _hcwid, _hchei;

    /** The chunk rotational velocity in radians. */
    protected float _rvel;

    /** The chunk velocity. */
    protected float _vel;

    /** The starting position. */
    protected int _sx, _sy;

    /** The total number of image chunks. */
    protected int _chunkcount;

    /** The number of image chunks on each axis. */
    protected int _xchunk, _ychunk;

    /** The image to animate. */
    protected Image _image;

    /** The starting animation time. */
    protected long _start;
}
