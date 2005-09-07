//
// $Id$
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

import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

import com.threerings.media.image.Colorization;

/**
 * The object tileset supports the specification of object information for
 * object tiles in addition to all of the features of the swiss army
 * tileset.
 *
 * @see ObjectTile
 */
public class ObjectTileSet extends SwissArmyTileSet
    implements RecolorableTileSet
{   
    /** A constraint prefix indicating that the object must have empty space in
     * the suffixed direction (N, E, S, or W). */
    public static final String SPACE = "SPACE_";
        
    /** A constraint indicating that the object is a surface (e.g., table). */
    public static final String SURFACE = "SURFACE";
    
    /** A constraint indicating that the object must be placed on a surface. */
    public static final String ON_SURFACE = "ON_SURFACE";
 
    /** A constraint prefix indicating that the object is a wall facing the
     * suffixed direction (N, E, S, or W). */
    public static final String WALL = "WALL_";
 
    /** A constraint prefix indicating that the object must be placed on a
     * wall facing the suffixed direction (N, E, S, or W). */
    public static final String ON_WALL = "ON_WALL_";
    
    /** A constraint prefix indicating that the object must be attached to a
     * wall facing the suffixed direction (N, E, S, or W). */
    public static final String ATTACH = "ATTACH_";
    
    /** The low suffix for walls and attachments. Low attachments can be placed
     * on low or normal walls; normal attachments can only be placed on normal
     * walls. */
    public static final String LOW = "_LOW";
    
    /**
     * Sets the widths (in unit tile count) of the objects in this
     * tileset. This must be accompanied by a call to {@link
     * #setObjectHeights}.
     */
    public void setObjectWidths (int[] objectWidths)
    {
        _owidths = objectWidths;
    }

    /**
     * Sets the heights (in unit tile count) of the objects in this
     * tileset. This must be accompanied by a call to {@link
     * #setObjectWidths}.
     */
    public void setObjectHeights (int[] objectHeights)
    {
        _oheights = objectHeights;
    }

    /**
     * Sets the x offset in pixels to the image origin.
     */
    public void setXOrigins (int[] xorigins)
    {
        _xorigins = xorigins;
    }

    /**
     * Sets the y offset in pixels to the image origin.
     */
    public void setYOrigins (int[] yorigins)
    {
        _yorigins = yorigins;
    }

    /**
     * Sets the default render priorities for our object tiles.
     */
    public void setPriorities (byte[] priorities)
    {
        _priorities = priorities;
    }

    /**
     * Provides a set of colorization classes that apply to objects in
     * this tileset.
     */
    public void setColorizations (String[] zations)
    {
        _zations = zations;
    }

    /**
     * Sets the x offset to the "spots" associated with our object tiles.
     */
    public void setXSpots (short[] xspots)
    {
        _xspots = xspots;
    }

    /**
     * Sets the y offset to the "spots" associated with our object tiles.
     */
    public void setYSpots (short[] yspots)
    {
        _yspots = yspots;
    }

    /**
     * Sets the orientation of the "spots" associated with our object
     * tiles.
     */
    public void setSpotOrients (byte[] sorients)
    {
        _sorients = sorients;
    }

    /**
     * Sets the lists of constraints associated with our object tiles.
     */
    public void setConstraints (String[][] constraints)
    {
        _constraints = constraints;
    }
    
    /**
     * Returns the x coordinate of the spot associated with the specified
     * tile index.
     */
    public int getXSpot (int tileIdx)
    {
        return (_xspots == null) ? 0 : _xspots[tileIdx];
    }

    /**
     * Returns the y coordinate of the spot associated with the specified
     * tile index.
     */
    public int getYSpot (int tileIdx)
    {
        return (_yspots == null) ? 0 : _yspots[tileIdx];
    }

    /**
     * Returns the orientation of the spot associated with the specified
     * tile index.
     */
    public int getSpotOrient (int tileIdx)
    {
        return (_sorients == null) ? 0 : _sorients[tileIdx];
    }

    /**
     * Returns the list of constraints associated with the specified tile
     * index, or <code>null</code> if the index has no constraints.
     */
    public String[] getConstraints (int tileIdx)
    {
        return (_constraints == null) ? null : _constraints[tileIdx];
    }
    
    /**
     * Checks whether the tile at the specified index has the given constraint.
     */
    public boolean hasConstraint (int tileIdx, String constraint)
    {
        return (_constraints == null) ? false :
            ListUtil.contains(_constraints[tileIdx], constraint);
    }
    
    // documentation inherited from interface RecolorableTileSet
    public String[] getColorizations ()
    {
        return _zations;
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
	buf.append(", owidths=").append(StringUtil.toString(_owidths));
	buf.append(", oheights=").append(StringUtil.toString(_oheights));
	buf.append(", xorigins=").append(StringUtil.toString(_xorigins));
	buf.append(", yorigins=").append(StringUtil.toString(_yorigins));
	buf.append(", prios=").append(StringUtil.toString(_priorities));
	buf.append(", zations=").append(StringUtil.toString(_zations));
	buf.append(", xspots=").append(StringUtil.toString(_xspots));
	buf.append(", yspots=").append(StringUtil.toString(_yspots));
	buf.append(", sorients=").append(StringUtil.toString(_sorients));
	buf.append(", constraints=").append(StringUtil.toString(_constraints));
    }

    // documentation inherited
    protected Colorization[] getColorizations (int tileIndex, Colorizer rizer)
    {
        Colorization[] zations = null;
        if (rizer != null && _zations != null) {
            zations = new Colorization[_zations.length];
            for (int ii = 0; ii < _zations.length; ii++) {
                zations[ii] = rizer.getColorization(ii, _zations[ii]);
            }
        }
        return zations;
    }

    // documentation inherited
    protected Tile createTile ()
    {
        return new ObjectTile();
    }

    // documentation inherited
    protected void initTile (Tile tile, int tileIndex, Colorization[] zations)
    {
        super.initTile(tile, tileIndex, zations);

        ObjectTile otile = (ObjectTile)tile;
        if (_owidths != null) {
            otile.setBase(_owidths[tileIndex], _oheights[tileIndex]);
        }
        if (_xorigins != null) {
            otile.setOrigin(_xorigins[tileIndex], _yorigins[tileIndex]);
        }
        if (_priorities != null) {
            otile.setPriority(_priorities[tileIndex]);
        }
        if (_xspots != null) {
            otile.setSpot(_xspots[tileIndex], _yspots[tileIndex],
                          _sorients[tileIndex]);
        }
        if (_constraints != null) {
            otile.setConstraints(_constraints[tileIndex]);
        }
    }

    /** The width (in tile units) of our object tiles. */
    protected int[] _owidths;

    /** The height (in tile units) of our object tiles. */
    protected int[] _oheights;

    /** The x offset in pixels to the origin of the tile images. */
    protected int[] _xorigins;

    /** The y offset in pixels to the origin of the tile images. */
    protected int[] _yorigins;

    /** The default render priorities of our objects. */
    protected byte[] _priorities;

    /** Colorization classes that apply to our objects. */
    protected String[] _zations;

    /** The x offset to the "spots" associated with our tiles. */
    protected short[] _xspots;

    /** The y offset to the "spots" associated with our tiles. */
    protected short[] _yspots;

    /** The orientation of the "spots" associated with our tiles. */
    protected byte[] _sorients;

    /** Lists of constraints associated with our tiles. */
    protected String[][] _constraints;
    
    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 2;
}
