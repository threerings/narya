//
// $Id: TileUtil.java,v 1.3 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast;

import java.awt.Image;
import java.awt.image.BufferedImage;

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
     * Renders each of the given <code>src</code> component frames
     * into the corresponding frames of <code>dest</code>.
     */
    public static void compositeFrames (
        ComponentFrames dest, ComponentFrames src)
    {
        for (int ii = 0; ii < Sprite.NUM_DIRECTIONS; ii++) {
            // composite the standing frames
            compositeFrames(dest.stand[ii], src.stand[ii]);

            // composite the walking frames
            compositeFrames(dest.walk[ii], src.walk[ii]);
        }
    }

    /**
     * Constructs and returns a new {@link
     * CharacterComponent.ComponentFrames} object with empty images
     * for all of its frames.
     */
    public static ComponentFrames createBlankFrames (
        ComponentFrames src, int frameCount)
    {
        ComponentFrames frames = new ComponentFrames();

        // for now, just use the first frame from the source image to
        // get the width and height for all of the blank frames
        Image img = src.walk[0].getFrame(0);
        int wid = img.getWidth(null), hei = img.getHeight(null);

        // allocate the blank frame images
        for (int ii = 0; ii < Sprite.NUM_DIRECTIONS; ii++) {
            frames.stand[ii] = new BlankFrameImage(wid, hei, 1);
            frames.walk[ii] = new BlankFrameImage(wid, hei, frameCount);
        }

        return frames;
    }

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

    /**
     * Renders each of the given <code>src</code> frames into the
     * corresponding frames of <code>dest</code>.
     */
    protected static void compositeFrames (
        MultiFrameImage dest, MultiFrameImage src)
    {
        int dsize = dest.getFrameCount(), ssize = src.getFrameCount();
        if (dsize != ssize) {
            Log.warning("Can't composite multi frame images " +
                        "with differing frame counts " +
                        "[dest=" + dsize + ", src=" + ssize + "].");
            return;
        }

        for (int ii = 0; ii < dsize; ii++) {
            Image dimg = dest.getFrame(ii);
            Image simg = src.getFrame(ii);
            dimg.getGraphics().drawImage(simg, 0, 0, null);
        }
    }

    /**
     * An implementation of the {@link MultiFrameImage} interface that
     * initializes itself to contain a specified number of blank
     * frames of the requested dimensions.
     */
    protected static class BlankFrameImage implements MultiFrameImage
    {
        /**
         * Constructs a blank frame image.
         */
        public BlankFrameImage (int width, int height, int frameCount)
        {
            _imgs = new Image[frameCount];
            for (int ii = 0; ii < frameCount; ii++) {
                _imgs[ii] = new BufferedImage(
                    width, height, BufferedImage.TYPE_INT_ARGB);
            }
        }

        // documentation inherited
        public int getFrameCount ()
        {
            return _imgs.length;
        }

        // documentation inherited
        public Image getFrame (int index)
        {
            return _imgs[index];
        }

        /** The frame images. */
        protected Image _imgs[];
    }
}
