//
// $Id: TileUtil.java,v 1.8 2001/10/25 18:06:17 shaper Exp $

package com.threerings.miso.tile;

import java.awt.Image;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.*;

import com.threerings.miso.Log;
import com.threerings.miso.scene.AmbulatorySprite.CharacterImages;

/**
 * Tile-related utility functions.
 */ 
public class TileUtil
{
    /**
     * Returns a {@link CharacterImages} object containing the frames
     * of animation used to render the sprite while standing or
     * walking in each of the directions it may face.  The tileset id
     * referenced must contain <code>Sprite.NUM_DIRECTIONS</code> rows
     * of tiles, with each row containing first the single standing
     * tile, followed by <code>frameCount</code> tiles describing the
     * walking animation.
     *
     * @param tilemgr the tile manager to retrieve tiles from.
     * @param tsid the tileset id containing the sprite tiles.
     * @param frameCount the number of walking frames of animation.
     *
     * @return the ambulatory images object.
     */
    public static CharacterImages getCharacterImages (
        TileManager tilemgr, int tsid, int frameCount)
    {
        CharacterImages images = new CharacterImages();
        images.standing = new MultiFrameImage[Sprite.NUM_DIRECTIONS];
        images.walking = new MultiFrameImage[Sprite.NUM_DIRECTIONS];

	try {
	    for (int ii = 0; ii < Sprite.NUM_DIRECTIONS; ii++) {

                Image walkimgs[] = new Image[frameCount];

                int rowcount = frameCount + 1;
		for (int jj = 0; jj < rowcount; jj++) {
		    int idx = (ii * rowcount) + jj;

                    Image img = tilemgr.getTile(tsid, idx).img;
                    if (jj == 0) {
                        images.standing[ii] = new SingleFrameImageImpl(img);
                    } else {
                        walkimgs[jj - 1] = img;
                    }
		}

                images.walking[ii] = new MultiFrameImageImpl(walkimgs);
	    }

	} catch (TileException te) {
	    Log.warning("Exception retrieving character images " +
			"[te=" + te + "].");
	    return null;
	}

        return images;
    }
}
