//
// $Id: TileUtil.java,v 1.2 2001/10/26 01:40:22 mdb Exp $

package com.threerings.cast;

import java.awt.Image;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.*;

import com.threerings.cast.Log;
import com.threerings.cast.CharacterComponent.ComponentFrames;

/**
 * Miscellaneous tile-related utility functions.
 */ 
public class TileUtil
{
    /**
     * Returns a {@link CharacterComponent.ComponentFrames} object
     * containing the frames of animation used to render the sprite while
     * standing or walking in each of the directions it may face.  The
     * tileset id referenced must contain
     * <code>Sprite.NUM_DIRECTIONS</code> rows of tiles, with each row
     * containing first the single standing tile, followed by
     * <code>frameCount</code> tiles describing the walking animation.
     *
     * @param tilemgr the tile manager to retrieve tiles from.
     * @param tsid the tileset id containing the sprite tiles.
     * @param frameCount the number of walking frames of animation.
     */
    public static ComponentFrames getComponentFrames (
        TileManager tilemgr, int tsid, int frameCount)
    {
        ComponentFrames frames = new ComponentFrames();
        frames.walk = new MultiFrameImage[Sprite.NUM_DIRECTIONS];
        frames.stand = new MultiFrameImage[Sprite.NUM_DIRECTIONS];

	try {
	    for (int ii = 0; ii < Sprite.NUM_DIRECTIONS; ii++) {

                Image walkimgs[] = new Image[frameCount];
                int rowcount = frameCount + 1;
		for (int jj = 0; jj < rowcount; jj++) {
		    int idx = (ii * rowcount) + jj;

                    Image img = tilemgr.getTile(tsid, idx).img;
                    if (jj == 0) {
                        frames.stand[ii] = new SingleFrameImageImpl(img);
                    } else {
                        walkimgs[jj - 1] = img;
                    }
		}

                frames.walk[ii] = new MultiFrameImageImpl(walkimgs);
	    }

	} catch (TileException te) {
	    Log.warning("Exception retrieving character images " +
			"[te=" + te + "].");
            return null;
	}

        return frames;
    }
}
