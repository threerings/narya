//
// $Id: Tile.java,v 1.28 2003/05/31 00:56:38 mdb Exp $

package com.threerings.media.tile;

import java.awt.Graphics2D;
import java.util.Arrays;

import com.threerings.media.image.Colorization;
import com.threerings.media.image.Mirage;

/**
 * A tile represents a single square in a single layer in a scene.
 */
public class Tile // implements Cloneable
{
    /** Used when caching tiles. */
    public static class Key
    {
        public TileSet tileSet;
        public int tileIndex;
        public Colorization[] zations;

        public Key (TileSet tileSet, int tileIndex, Colorization[] zations) {
            this.tileSet = tileSet;
            this.tileIndex = tileIndex;
            this.zations = zations;
        }

        public boolean equals (Object other) {
            if (other instanceof Key) {
                Key okey = (Key)other;
                return (tileSet == okey.tileSet &&
                        tileIndex == okey.tileIndex &&
                        Arrays.equals(zations, okey.zations));
            } else {
                return false;
            }
        }

        public int hashCode () {
            int code = (tileSet == null) ? tileIndex :
                (tileSet.hashCode() ^ tileIndex);
            int zcount = (zations == null) ? 0 : zations.length;
            for (int ii = 0; ii < zcount; ii++) {
                if (zations[ii] != null) {
                    code ^= zations[ii].hashCode();
                }
            }
            return code;
        }
    }

    /** The key associated with this tile. */
    public Key key;

    /**
     * Configures this tile with its tile image.
     */
    public void setImage (Mirage image)
    {
        if (_mirage != null) {
            _totalTileMemory -= _mirage.getEstimatedMemoryUsage();
        }
        _mirage = image;
        if (_mirage != null) {
            _totalTileMemory += _mirage.getEstimatedMemoryUsage();
        }
    }

    /**
     * Returns the width of this tile.
     */
    public int getWidth ()
    {
        return _mirage.getWidth();
    }

    /**
     * Returns the height of this tile.
     */
    public int getHeight ()
    {
        return _mirage.getHeight();
    }

    /**
     * Returns the estimated memory usage of our underlying tile image.
     */
    public long getEstimatedMemoryUsage ()
    {
        return _mirage.getEstimatedMemoryUsage();
    }

    /**
     * Render the tile image at the specified position in the given
     * graphics context.
     */
    public void paint (Graphics2D gfx, int x, int y)
    {
        _mirage.paint(gfx, x, y);
    }

    /**
     * Returns true if the specified coordinates within this tile contains
     * a non-transparent pixel.
     */
    public boolean hitTest (int x, int y)
    {
        return _mirage.hitTest(x, y);
    }

//     /**
//      * Creates a shallow copy of this tile object.
//      */
//     public Object clone ()
//     {
//         try {
//             return (Tile)super.clone();
//         } catch (CloneNotSupportedException cnse) {
//             String errmsg = "All is wrong with the universe: " + cnse;
//             throw new RuntimeException(errmsg);
//         }
//     }

    /**
     * Return a string representation of this tile.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer("[");
        toString(buf);
	return buf.append("]").toString();
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific tile information to the string buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append(_mirage.getWidth()).append("x");
        buf.append(_mirage.getHeight());
    }

    /** Decrement total tile memory by our value. */
    protected void finalize ()
    {
        if (_mirage != null) {
            _totalTileMemory -= _mirage.getEstimatedMemoryUsage();
        }
    }

    /** Our tileset image. */
    protected Mirage _mirage;

    /** Used to track total (estimated) memory in use by tiles. */
    protected static long _totalTileMemory = 0L;
}
