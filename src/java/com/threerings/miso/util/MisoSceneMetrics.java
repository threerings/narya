//
// $Id: MisoSceneMetrics.java,v 1.6 2004/08/27 02:20:10 mdb Exp $
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

package com.threerings.miso.util;

/**
 * Contains information on the configuration of a particular isometric
 * view. The member data are public to facilitate convenient referencing
 * by the {@link MisoScenePanel} class, the values should not be modified
 * once the metrics are constructed.
 */
public class MisoSceneMetrics
{
    /** Tile dimensions and half-dimensions in the view. */
    public int tilewid, tilehei, tilehwid, tilehhei;

    /** Fine coordinate dimensions. */
    public int finehwid, finehhei;

    /** Number of fine coordinates on each axis within a tile. */
    public int finegran;

    /** Dimensions of our scene blocks in tile count. */
    public short blockwid = 4, blockhei = 4;

    /** The length of a tile edge in pixels. */
    public float tilelen;

    /** The slope of the x- and y-axis lines. */
    public float slopeX, slopeY;

    /** The length between fine coordinates in pixels. */
    public float finelen;

    /** The y-intercept of the x-axis line within a tile. */
    public float fineBX;

    /** The slope of the x- and y-axis lines within a tile. */
    public float fineSlopeX, fineSlopeY;

    /**
     * Constructs scene metrics by directly specifying the desired config
     * parameters.
     *
     * @param tilewid the width in pixels of the tiles.
     * @param tilehei the height in pixels of the tiles.
     * @param finegran the number of sub-tile divisions to use for fine
     * coordinates.
     */
    public MisoSceneMetrics (int tilewid, int tilehei, int finegran)
    {
        // keep track of this stuff
        this.tilewid = tilewid;
        this.tilehei = tilehei;
        this.finegran = finegran;

        // halve the dimensions
        tilehwid = (tilewid / 2);
        tilehhei = (tilehei / 2);

        // calculate the length of a tile edge in pixels
        tilelen = (float) Math.sqrt(
            (tilehwid * tilehwid) + (tilehhei * tilehhei));

        // calculate the slope of the x- and y-axis lines
        slopeX = (float)tilehei / (float)tilewid;
        slopeY = -slopeX;

	// calculate the edge length separating each fine coordinate
	finelen = tilelen / (float)finegran;

	// calculate the fine-coordinate x-axis line
	fineSlopeX = (float)tilehei / (float)tilewid;
	fineBX = -(fineSlopeX * (float)tilehwid);
	fineSlopeY = -fineSlopeX;

	// calculate the fine coordinate dimensions
	finehwid = (int)((float)tilehwid / (float)finegran);
	finehhei = (int)((float)tilehhei / (float)finegran);
    }
}
