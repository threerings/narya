//
// $Id: MisoCharacterSprite.java,v 1.7 2002/12/05 23:06:30 mdb Exp $

package com.threerings.miso.scene;

import com.threerings.cast.CharacterSprite;
import com.threerings.miso.tile.BaseTile;

/**
 * The miso character sprite extends the basic character sprite to support
 * the notion of tile passability.
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
}
