//
// $Id: MisoCharacterSprite.java,v 1.6 2002/11/28 03:42:17 mdb Exp $

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
    implements Traverser, IsoSprite
{
    // documentation inherited
    public boolean canTraverse (BaseTile tile)
    {
	// by default, passability is solely the province of the tile
	return tile.isPassable();
    }

    // documentation inherited from interface
    public int getTileX ()
    {
        return _tilex;
    }

    // documentation inherited from interface
    public int getTileY ()
    {
        return _tiley;
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

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", tilex=").append(_tilex);
        buf.append(", tiley=").append(_tiley);
    }

    /** The sprite location in tile coordinates. */
    protected int _tilex, _tiley;
}
