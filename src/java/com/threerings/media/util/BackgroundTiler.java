//
// $Id: BackgroundTiler.java,v 1.3 2002/09/25 21:49:53 shaper Exp $

package com.threerings.media.util;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import com.threerings.media.Log;

/**
 * Used to tile a background image into regions of various sizes. The
 * source image is divided into nine quadrants (of mostly equal size)
 * which are tiled accordingly to fill whatever size background image is
 * desired.
 */
public class BackgroundTiler
{
    /**
     * Creates a background tiler with the specified source image.
     */
    public BackgroundTiler (BufferedImage src)
    {
        // make sure we were given the goods
        if (src == null) {
            Log.info("Backgrounder given null source image. Coping.");
            return;
        }

        // compute some values
        _width = src.getWidth(null);
        _w3 = _width/3;
        _cw3 = _width-2*_w3;
        _height = src.getHeight(null);
        _h3 = _height/3;
        _ch3 = _height-2*_h3;

        // create our sub-divided images
        _tiles = new BufferedImage[9];
        int[] sy = { 0, _h3, _h3+_ch3 };
        int[] thei = { _h3, _ch3, _h3 };
        for (int i = 0; i < 3; i++) {
            _tiles[3*i] = src.getSubimage(0, sy[i], _w3, thei[i]);
            _tiles[3*i+1] = src.getSubimage(_w3, sy[i], _cw3, thei[i]);
            _tiles[3*i+2] =
                src.getSubimage(_width-_w3, sy[i], _w3, thei[i]);
        }
    }

    /**
     * Fills the requested region with the background defined by our
     * source image.
     */
    public void paint (Graphics g, int x, int y, int width, int height)
    {
        // bail out now if we were passed a bogus source image at
        // construct time
        if (_tiles == null) {
            return;
        }

        int rwid = width-2*_w3, rhei = height-2*_h3, cy = y;
        Shape oclip = g.getClip();

        // tile the top row
        paintRow(0, g, x, cy, width);
        cy += _h3;

        // tile the (complete) intermediate rows
        int ycount = rhei/_ch3;
        for (int row = 0; row < ycount; row++) {
            paintRow(1, g, x, cy, width);
            cy += _ch3;
        }

        // set the clip and paint the clipped intermediate row (if we
        // didn't tile evenly in the vertical direction)
        int yextra = (rhei - ycount * _ch3);
        if (yextra > 0) {
            g.clipRect(x, cy, width, yextra);
            paintRow(1, g, x, cy, width);
            g.setClip(oclip);
        }

        // tile the last row
        int lasty = y + height - _h3;
        paintRow(2, g, x, lasty, width);

        // now, set the clipping rectangle and render the horizontal tiles
        // that we missed the first time around because we want to clip
        // only once instead of once per row
        int xcount = rwid/_cw3;
        int xextra = (rwid - xcount * _cw3);
        int xoff = x + width - _w3 - xextra;
        if (xextra < width) {
            cy = y; // start back at the top
            g.clipRect(xoff, y, xextra, height);
            g.drawImage(_tiles[1], xoff, cy, null);
            cy += _h3;
            for (int row = 0; row < ycount; row++) {
                g.drawImage(_tiles[4], xoff, cy, null);
                cy += _ch3;
            }
            g.drawImage(_tiles[7], xoff, lasty, null);
        }

        // finally, clip the tiny region where the xextra and yextra rects
        // intersect and paint that last niggling bit (we value
        // correctness, so we're doing things properly)
        if (xextra > 0 && yextra > 0) {
            // we know the clip is still set from the xextra render, so we
            // just restrict it once again to the yextra region and the
            // intersection will happen automatically
            g.clipRect(x, cy, width, yextra);
            g.drawImage(_tiles[4], xoff, lasty - yextra, null);
        }

        // phew, we're done
        g.setClip(oclip);
    }

    /**
     * Used by {@link #paint} to render rows.
     */
    protected void paintRow (int srow, Graphics g, int x, int y, int width)
    {
        int xcount = (width-2*_w3)/_cw3;
        int tidx = 3*srow;

        // draw the first image in the row
        int cx = x;
        g.drawImage(_tiles[tidx++], cx, y, null);
        cx += _w3;

        // draw the (complete) tiled middle images
        for (int ii = 0; ii < xcount; ii++) {
            g.drawImage(_tiles[tidx], cx, y, null);
            cx += _cw3;
        }

        // we'll render the last (incomplete) tiled image in a final
        // cleanup render so that we only have to set the clipping region
        // once

        // draw the end image
        cx = x+width-_w3;
        g.drawImage(_tiles[++tidx], cx, y, null);
    }

    /** Our nine sub-divided images. */
    protected BufferedImage[] _tiles;

    /** The width/height of our source image. */
    protected int _width, _height;

    /** One third of width/height of our source image. */
    protected int _w3, _h3;

    /** The size of the center chunk of our subdivided images. */
    protected int _cw3, _ch3;
}
