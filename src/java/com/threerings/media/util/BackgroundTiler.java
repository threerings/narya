//
// $Id: BackgroundTiler.java,v 1.9 2004/08/27 02:12:47 mdb Exp $
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

package com.threerings.media.util;

import java.awt.Graphics;
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
        int width = src.getWidth(null);
        int height = src.getHeight(null);

        _w3 = width/3;
        _cw3 = width-2*_w3;
        _h3 = height/3;
        _ch3 = height-2*_h3;

        // make sure the image suits our minimum useful dimensions
        if (_w3 <= 0 || _cw3 <= 0 || _h3 <= 0 || _ch3 <= 0) {
            Log.warning("Backgrounder given source image of insufficient " +
                        "size for tiling " +
                        "[width=" + width + ", height=" + height + "].");
            return;
        }

        // create our sub-divided images
        _tiles = new BufferedImage[9];
        int[] sy = { 0, _h3, _h3+_ch3 };
        int[] thei = { _h3, _ch3, _h3 };
        for (int i = 0; i < 3; i++) {
            _tiles[3*i] = src.getSubimage(0, sy[i], _w3, thei[i]);
            _tiles[3*i+1] = src.getSubimage(_w3, sy[i], _cw3, thei[i]);
            _tiles[3*i+2] =
                src.getSubimage(width-_w3, sy[i], _w3, thei[i]);
        }
    }

    /**
     * Returns the "natural" width of the image being used to tile the
     * background.
     */
    public int getNaturalWidth ()
    {
        return _w3*2+_cw3;
    }

    /**
     * Returns the "natural" height of the image being used to tile the
     * background.
     */
    public int getNaturalHeight ()
    {
        return _h3*2+_ch3;
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

        int rwid = width-2*_w3, rhei = height-2*_h3;

        g.drawImage(_tiles[0], x, y, _w3, _h3, null);
        g.drawImage(_tiles[1], x + _w3, y, rwid, _h3, null);
        g.drawImage(_tiles[2], x + _w3 + rwid, y, _w3, _h3, null);

        y += _h3;
        g.drawImage(_tiles[3], x, y, _w3, rhei, null);
        g.drawImage(_tiles[4], x + _w3, y, rwid, rhei, null);
        g.drawImage(_tiles[5], x + _w3 + rwid, y, _w3, rhei, null);

        y += rhei;
        g.drawImage(_tiles[6], x, y, _w3, _h3, null);
        g.drawImage(_tiles[7], x + _w3, y, rwid, _h3, null);
        g.drawImage(_tiles[8], x + _w3 + rwid, y, _w3, _h3, null);
    }

    /** Our nine sub-divided images. */
    protected BufferedImage[] _tiles;

    /** One third of width/height of our source image. */
    protected int _w3, _h3;

    /** The size of the center chunk of our subdivided images. */
    protected int _cw3, _ch3;
}


// Below is an alternate implementation that uses only the source image
// without chopping it up. It ends up running very slightly slower, that
// may be because the documentation for the version of drawImage used below
// states that scaled instances will not be cached, that the scaling will
// happen each time on the fly. On the other hand, maybe that's a good thing.
// I like it better because it doesn't have to have 9 separate images and
// because it does the render in a loop, which is good fun.

//public class BackgroundTiler
//{
//    /**
//     * Creates a background tiler with the specified source image.
//     */
//    public BackgroundTiler (BufferedImage src)
//    {
//        // make sure we were given the goods
//        if (src == null) {
//            Log.info("Backgrounder given null source image. Coping.");
//            return;
//        }
//
//        _src = src;
//
//        // compute some values
//        int width = src.getWidth(null);
//        int height = src.getHeight(null);
//
//        _w3 = width/3;
//        _cw3 = width-2*_w3;
//        _h3 = height/3;
//        _ch3 = height-2*_h3;
//
//        _sx = new int[] { 0, _w3, width - _w3, width };
//        _sy = new int[] { 0, _h3, height - _h3, height };
//        _dx = new int[4];
//        _dy = new int[4];
//    }
//
//    /**
//     * Fills the requested region with the background defined by our
//     * source image.
//     */
//    public void paint (Graphics g, int x, int y, int width, int height)
//    {
//        _dx[0] = x;
//        _dx[1] = x + _w3;
//        _dx[2] = x + width - _w3;
//        _dx[3] = x + width;
//
//        _dy[0] = y;
//        _dy[1] = y + _h3;
//        _dy[2] = y + height - _h3;
//        _dy[3] = y + height;
//
//        for (int jj=0; jj < 3; jj++) {
//            for (int ii=0; ii < 3; ii++) {
//                g.drawImage(_src, _dx[ii], _dy[jj], _dx[ii + 1], _dy[jj + 1],
//                    _sx[ii], _sy[jj], _sx[ii + 1], _sy[jj + 1], null);
//            }
//        }
//    }
//
//    /** Our source image. */
//    protected BufferedImage _src;
//
//    /** Coordinates. */
//    protected int[] _sx, _sy, _dx, _dy;
//
//    /** One third of width/height of our source image. */
//    protected int _w3, _h3;
//
//    /** The size of the center chunk of our subdivided images. */
//    protected int _cw3, _ch3;
//}
