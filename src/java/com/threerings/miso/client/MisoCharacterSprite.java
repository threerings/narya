//
// $Id: MisoCharacterSprite.java,v 1.4 2002/06/20 07:47:14 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.cast.CharacterSprite;

import com.threerings.miso.tile.BaseTile;

/**
 * The miso character sprite extends the basic character sprite to
 * support the notion of tile passability, and to allow the sprite to
 * store and make available its tile and fine coordinates within the
 * scene.  Note that the tile and fine coordinates must initially be
 * set properly by whomever creates the sprite.  Thereafter, the
 * sprite will only be moved about via the {@link TilePath}, which
 * will itself keep the sprite coordinates properly up to date.
 */
public class MisoCharacterSprite extends CharacterSprite
    implements Traverser
{
    // documentation inherited
    public boolean canTraverse (BaseTile tile)
    {
	// by default, passability is solely the province of the tile
	return tile.isPassable();
    }

    /**
     * Returns the sprite's location on the x-axis in tile coordinates.
     */
    public int getTileX ()
    {
        return _tilex;
    }

    /**
     * Returns the sprite's location on the y-axis in tile coordinates.
     */
    public int getTileY ()
    {
        return _tiley;
    }

    /**
     * Returns the sprite's location on the x-axis within its current
     * tile in fine coordinates.
     */
    public int getFineX ()
    {
        return _finex;
    }

    /**
     * Returns the sprite's location on the y-axis within its current
     * tile in fine coordinates.
     */
    public int getFineY ()
    {
        return _finey;
    }

    /**
     * Sets the sprite's location in tile coordinates; the sprite is
     * not actually moved in any way.  This method is only intended
     * for use in updating the sprite's stored position which is made
     * accessible to others that may care to review it.
     */
    public void setTileLocation (int x, int y)
    {
        _tilex = x;
        _tiley = y;
    }

    /**
     * Sets the sprite's location in fine coordinates; the sprite is
     * not actually moved in any way.  This method is only intended
     * for use in updating the sprite's stored position which is made
     * accessible to others that may care to review it.
     */
    public void setFineLocation (int x, int y)
    {
        _finex = x;
        _finey = y;
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", tilex=").append(_tilex);
        buf.append(", tiley=").append(_tiley);
        buf.append(", finex=").append(_finex);
        buf.append(", finey=").append(_finey);
    }

    /** The sprite location in tile coordinates. */
    protected int _tilex, _tiley;

    /** The sprite location in fine coordinates. */
    protected int _finex, _finey;
}
