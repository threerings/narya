//
// $Id: ExplodeAnimation.java,v 1.1 2002/01/11 16:17:33 shaper Exp $

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
     * @param sx the starting x-position.
     * @param sy the starting y-position.
     * @param vel the initial chunk velocity.
     */
    public ExplodeAnimation (
        Rectangle bounds, Image image, int sx, int sy, float vel)
    {
        super(bounds);

        // save things off
        _image = image;
        _sx = sx;
        _sy = sy;
        _vel = vel;

        // save the animation starting time
        _start = System.currentTimeMillis();

        // determine chunk dimensions
        _cwid = image.getWidth(null) / X_CHUNK_COUNT;
        _chei = image.getHeight(null) / Y_CHUNK_COUNT;

        // initialize the chunk positions
        int cx = sx + (image.getWidth(null) / 2);
        int cy = sy + (image.getHeight(null) / 2);
        int cpos = (cx << 16 | cy);
        for (int ii = 0; ii < NUM_CHUNKS; ii++) {
            int xpos = ii % X_CHUNK_COUNT;
            int ypos = ii / X_CHUNK_COUNT;
            _cpos[ii] = ((sx + xpos) << 16 | (sy + ypos));
        }
    }

    // documentation inherited
    public void tick (long timestamp)
    {
        // figure out the distance the chunks have travelled
        long msecs = timestamp - _start;
        int travpix = (int)(msecs * _vel);

        // move all chunks and check whether any remain to be animated
        int inside = 0;
        for (int ii = 0; ii < NUM_CHUNKS; ii++) {
            // retrieve the current chunk position
            int x = _cpos[ii] >> 16;
            int y = _cpos[ii] & 0xFFFF;

            // determine the chunk movement direction
            int xpos = ii % X_CHUNK_COUNT;
            int ypos = ii / X_CHUNK_COUNT;
            int mx, my;
            switch (xpos) {
            case 0: mx = -1; break;
            case X_CHUNK_COUNT - 1: mx = 1; break;
            default: mx = 0; break;
            }
            switch (ypos) {
            case 0: my = -1; break;
            case Y_CHUNK_COUNT - 1: my = 1; break;
            default: my = 0;
            }
            // deal with the center point for odd dimensions
            if (mx == 0 && my == 0) {
                my = 1;
            }

            // update the chunk position
            x = _sx + (travpix * mx);
            y = _sy + (travpix * my);
            _cpos[ii] = (x << 16 | y);

            // note whether this chunk is still within our bounds
            if (_bounds.contains(x, y)) {
                inside++;
            }
        }

        _finished = (inside == 0);
        invalidate();
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        for (int ii = 0; ii < NUM_CHUNKS; ii++) {
            // get the chunk location on-screen
            int x = _cpos[ii] >> 16;
            int y = _cpos[ii] & 0xFFFF;

            // get the chunk position within the image
            int xpos = ii % X_CHUNK_COUNT;
            int ypos = ii / X_CHUNK_COUNT;

            // calculate image chunk offset
            int xoff = -(xpos * _cwid);
            int yoff = -(ypos * _chei);

            // draw the chunk
            Shape oclip = gfx.getClip();
            gfx.clipRect(x, y, _cwid, _chei);
            gfx.drawImage(_image, x + xoff, y + yoff, null);
            gfx.setClip(oclip);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);

        buf.append(", sx=").append(_sx);
        buf.append(", sy=").append(_sy);
    }

    /** The number of horizontal chunks from the image to animate. */
    protected static final int X_CHUNK_COUNT = 3;

    /** The number of vertical chunks from the image to animate. */
    protected static final int Y_CHUNK_COUNT = 3;

    /** The total number of chunks. */
    protected static final int NUM_CHUNKS = X_CHUNK_COUNT * Y_CHUNK_COUNT;

    /** The current position of each chunk. */
    protected int[] _cpos = new int[NUM_CHUNKS];

    /** The individual chunk dimensions in pixels. */
    protected int _cwid, _chei;

    /** The chunk velocity. */
    protected float _vel;

    /** The starting position. */
    protected int _sx, _sy;

    /** The image to animate. */
    protected Image _image;

    /** The starting animation time. */
    protected long _start;
}
