//
// $Id: CharacterSprite.java,v 1.7 2001/08/16 23:14:21 mdb Exp $

package com.threerings.miso.scene;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.Tile;

import com.threerings.miso.Log;

/**
 * An <code>AmbulatorySprite</code> is a sprite that can face in one of
 * the various compass directions and that can animate itself walking
 * along some chosen path.
 */
public class AmbulatorySprite extends Sprite implements Traverser
{
    /**
     * Construct an <code>AmbulatorySprite</code>, with a multi-frame
     * image associated with each of the eight compass directions. The
     * array should be in the order defined by the <code>Path</code>
     * direction constants (SW, W, NW, N, NE, E, SE, S).
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     * @param anims the set of multi-frame images to use when animating
     * the sprite in each of the compass directions.
     */
    public AmbulatorySprite (SpriteManager spritemgr, int x, int y,
                             MultiFrameImage[] anims)
    {
        super(spritemgr, x, y);

        _anims = anims;
        _dir = Path.DIR_SOUTH;

        setFrames(_anims[Path.DIR_NORTH]);
    }

    /**
     * Alter the sprite's direction to reflect the direction the
     * destination point lies in before calling the superclass's
     * <code>setDestination</code> method.
     *
     * @param x the destination x-position.
     * @param y the destination y-position.
     */
    protected void moveAlongPath ()
    {
        // select the new path node
        super.moveAlongPath();

        // bail if we're at the end of the path
        if (_dest == null) {
            return;
        }

        // update the sprite frames to reflect the direction
        setFrames(_anims[_dir = _dest.dir]);

        // start tile animation to show movement
        setAnimationDelay(0);
    }

    public void stop ()
    {
	super.stop();

	// stop any walking animation
	setAnimationDelay(ANIM_NONE);
    }

    public boolean canTraverse (Tile tile)
    {
	// by default, passability is solely the province of the tile
	return tile.passable;
    }

    /** The animation frames for the sprite facing each direction. */
    protected MultiFrameImage[] _anims;

    /** The direction the sprite is currently facing. */
    protected int _dir;
}
