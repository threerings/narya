//
// $Id: CharacterSprite.java,v 1.14 2001/10/24 00:55:08 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Point;

import com.threerings.media.sprite.*;

import com.threerings.miso.Log;
import com.threerings.miso.tile.MisoTile;
import com.threerings.miso.scene.util.IsoUtil;

/**
 * An ambulatory sprite is a sprite that animates itself while walking
 * about in a scene.
 */
public class AmbulatorySprite extends Sprite implements Traverser
{
    /**
     * Construct an <code>AmbulatorySprite</code>, with a multi-frame
     * image associated with each of the eight compass directions. The
     * array should be in the order defined by the <code>Sprite</code>
     * direction constants (SW, W, NW, N, NE, E, SE, S).
     *
     * @param x the sprite x-position in pixels.
     * @param y the sprite y-position in pixels.
     * @param anims the set of multi-frame images to use when animating
     * the sprite in each of the compass directions.
     */
    public AmbulatorySprite (
        IsoSceneViewModel model, int x, int y, MultiFrameImage[] anims)
    {
        super(x, y);

        // keep track of these
        _model = model;
        _anims = anims;

        // give ourselves an initial orientation
        setOrientation(DIR_NORTH);
    }

    // documentation inherited
    public void setOrientation (int orient)
    {
        super.setOrientation(orient);

        // update the sprite frames to reflect the direction
        setFrames(_anims[_orient]);
    }

    /**
     * Sets the origin coordinates representing the "base" of the
     * sprite, which in most cases corresponds to the center of the
     * bottom of the sprite image.
     */
    public void setOrigin (int x, int y)
    {
        _xorigin = x;
        _yorigin = y;

        updateRenderOffset();
        updateRenderOrigin();
    }

    // documentation inherited
    protected void updateRenderOffset ()
    {
        super.updateRenderOffset();

        if (_frame != null) {
            // our location is based on the character origin coordinates
            _rxoff = -_xorigin;
            _ryoff = -_yorigin;
        }
    }

    // documentation inherited
    public void cancelMove ()
    {
        super.cancelMove();
        halt();
    }

    // documentation inherited
    protected void pathBeginning ()
    {
        super.pathBeginning();

        // enable walking animation
        setAnimationMode(TIME_BASED);
    }

    // documentation inherited
    protected void pathCompleted ()
    {
        super.pathCompleted();
        halt();
    }

    /**
     * Updates the sprite animation frame to reflect the cessation of
     * movement and disables any further animation.
     */
    protected void halt ()
    {
        // come to a halt looking settled and at peace
        _frame = _frames.getFrame(_frameIdx = 0);
        invalidate();

        // disable walking animation
        setAnimationMode(NO_ANIMATION);
    }

    // documentation inherited
    public boolean canTraverse (MisoTile tile)
    {
	// by default, passability is solely the province of the tile
	return tile.passable;
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

    // documentation inherited
    public void setLocation (int x, int y)
    {
        super.setLocation(x, y);

        if (_path == null) {
            // we only calculate the sprite's tile and fine
            // coordinates if we have no path, since paths that move
            // us about are responsible for keeping our scene
            // coordinates up to date since only they can know where
            // we really are while in transition from one place to
            // another.

            // get the sprite's position in full coordinates
            Point fpos = new Point();
            IsoUtil.screenToFull(_model, _x, _y, fpos);

            // save off the sprite's tile and fine coordinates
            _tilex = IsoUtil.fullToTile(fpos.x);
            _tiley = IsoUtil.fullToTile(fpos.y);
            _finex = IsoUtil.fullToFine(fpos.x);
            _finey = IsoUtil.fullToFine(fpos.y);
        }
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

    /** The animation frames for the sprite facing each direction. */
    protected MultiFrameImage[] _anims;

    /** The iso scene view model. */
    protected IsoSceneViewModel _model;

    /** The origin of the sprite. */
    protected int _xorigin, _yorigin;

    /** The sprite location in tile coordinates. */
    protected int _tilex, _tiley;

    /** The sprite location in fine coordinates. */
    protected int _finex, _finey;
}
