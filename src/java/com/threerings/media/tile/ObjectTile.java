//
// $Id: ObjectTile.java,v 1.18 2004/08/27 02:12:41 mdb Exp $
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

package com.threerings.media.tile;

import java.awt.Dimension;
import java.awt.Point;

import com.samskivert.util.StringUtil;

import com.threerings.util.DirectionUtil;

/**
 * An object tile extends the base tile to provide support for objects
 * whose image spans more than one unit tile.
 *
 * <p> An object tile is generally positioned based on its origin rather
 * than the upper left of its image. Generally this origin is in the
 * bottom center of the object image, but can be configured to be anywhere
 * that the natural center point of "contact" is for the object. Note that
 * this does not automatically adjust the semantics of {@link #paint}, it
 * is just expected that the caller will account for the object tile's
 * origin when painting, if appropriate.
 *
 * <p> An object tile has dimensions (in tile units) that represent its
 * footprint or "shadow".
 */
public class ObjectTile extends Tile
{
    /**
     * Returns the object footprint width in tile units.
     */
    public int getBaseWidth ()
    {
        return _base.width;
    }

    /**
     * Returns the object footprint height in tile units.
     */
    public int getBaseHeight ()
    {
        return _base.height;
    }

    /**
     * Sets the object footprint in tile units.
     */
    protected void setBase (int width, int height)
    {
        _base.width = width;
        _base.height = height;
    }

    /**
     * Returns the x offset into the tile image of the origin (which will
     * be aligned with the bottom center of the origin tile) or
     * <code>Integer.MIN_VALUE</code> if the origin is not explicitly
     * specified and should be computed from the image size and tile
     * footprint.
     */
    public int getOriginX ()
    {
        return _origin.x;
    }

    /**
     * Returns the y offset into the tile image of the origin (which will
     * be aligned with the bottom center of the origin tile) or
     * <code>Integer.MIN_VALUE</code> if the origin is not explicitly
     * specified and should be computed from the image size and tile
     * footprint.
     */
    public int getOriginY ()
    {
        return _origin.y;
    }

    /**
     * Sets the offset in pixels from the origin of the tile image to the
     * origin of the object. The object will be rendered such that its
     * origin is at the bottom center of its origin tile. If no origin is
     * specified, the bottom of the image is aligned with the bottom of
     * the origin tile and the left side of the image is aligned with the
     * left edge of the left-most base tile.
     */
    protected void setOrigin (int x, int y)
    {
        _origin.x = x;
        _origin.y = y;
    }

    /**
     * Returns this object tile's default render priority.
     */
    public int getPriority ()
    {
        return _priority;
    }

    /**
     * Sets this object tile's default render priority.
     */
    protected void setPriority (int priority)
    {
        _priority = priority;
    }

    /**
     * Configures the "spot" associated with this object.
     */
    public void setSpot (int x, int y, byte orient)
    {
        _spot = new Point(x, y);
        _sorient = orient;
    }

    /**
     * Returns true if this object has a spot.
     */
    public boolean hasSpot ()
    {
        return (_spot != null);
    }

    /**
     * Returns the x-coordinate of the "spot" associated with this object.
     */
    public int getSpotX ()
    {
        return (_spot == null) ? 0 : _spot.x;
    }

    /**
     * Returns the x-coordinate of the "spot" associated with this object.
     */
    public int getSpotY ()
    {
        return (_spot == null) ? 0 : _spot.y;
    }

    /**
     * Returns the orientation of the "spot" associated with this object.
     */
    public int getSpotOrient ()
    {
        return _sorient;
    }

    // documentation inherited
    public void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", base=").append(StringUtil.toString(_base));
        buf.append(", origin=").append(StringUtil.toString(_origin));
        buf.append(", priority=").append(_priority);
        if (_spot != null) {
            buf.append(", spot=").append(StringUtil.toString(_spot));
            buf.append(", sorient=");
            buf.append(DirectionUtil.toShortString(_sorient));
        }
    }

    /** The object footprint width in unit tile units. */
    protected Dimension _base = new Dimension(1, 1);

    /** The offset from the origin of the tile image to the object's
     * origin or MIN_VALUE if the origin should be calculated based on the
     * footprint. */
    protected Point _origin = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

    /** This object tile's default render priority. */
    protected int _priority;

    /** The coordinates of the "spot" associated with this object. */
    protected Point _spot;

    /** The orientation of the "spot" associated with this object. */
    protected byte _sorient;
}
