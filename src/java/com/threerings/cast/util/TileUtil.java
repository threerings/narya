//
// $Id: TileUtil.java,v 1.1 2001/11/02 15:28:20 shaper Exp $

package com.threerings.cast.util;

import java.awt.Image;
import java.awt.image.BufferedImage;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.*;

import com.threerings.cast.Log;
import com.threerings.cast.*;

/**
 * Miscellaneous tile-related utility functions.
 */ 
public class TileUtil
{
    /**
     * Renders each of the given <code>src</code> component frames
     * into the corresponding frames of <code>dest</code>, allocating
     * blank image frames for <code>dest</code> if none yet exist.
     */
    public static void compositeFrames (
        MultiFrameImage[][] dest, MultiFrameImage[][] src)
    {
        for (int ii = 0; ii < dest.length; ii++) {
            for (int orient = 0; orient < Sprite.NUM_DIRECTIONS; orient++) {
                // create blank destination frames if needed
                if (dest[ii][orient] == null) {
                    dest[ii][orient] = createBlankFrames(src[ii][orient]);
                }

                // slap the images together
                compositeFrames(dest[ii][orient], src[ii][orient]);
            }
        }
    }

    /**
     * Returns a new {@link MultiFrameImage} that has empty images in
     * all frames.  The number of frames in the resulting multi frame
     * image is the same as the frame count for <code>src</code>.
     */
    public static MultiFrameImage createBlankFrames (MultiFrameImage src)
    {
        // TODO: for now, just use the first frame from the source to
        // get the width and height for all of the blank frames.  fix
        // this hack soon.
        Image img = src.getFrame(0);
        int wid = img.getWidth(null), hei = img.getHeight(null);
        return new BlankFrameImage(wid, hei, src.getFrameCount());
    }

    /**
     * Returns a two-dimensional array of multi frame images
     * containing the frames of animation used to render the sprite
     * while standing or walking in each of the directions it may
     * face.
     */
    public static MultiFrameImage[][] getComponentFrames (
        String imagedir, CharacterComponent c)
    {
        ActionSequence seqs[] = c.getActionSequences();
        MultiFrameImage frames[][] =
            new MultiFrameImage[seqs.length][Sprite.NUM_DIRECTIONS];

	try {
            for (int ii = 0; ii < seqs.length; ii++) {
                ActionSequence as = seqs[ii];

                // get the tile set containing the component tiles for
                // this action sequence
                String file = getImageFile(imagedir, as, c);
                TileSet tset = null;

                try {
                    tset = as.tileset.clone(file);
                } catch (CloneNotSupportedException e) {
                    Log.warning("Failed to clone tile set " +
                                "[tset=" + as.tileset + "].");
                    return null;
                }

                // get the number of frames of animation
                int frameCount = tset.getNumTiles() / Sprite.NUM_DIRECTIONS;

                for (int dir = 0; dir < Sprite.NUM_DIRECTIONS; dir++) {
                    // retrieve all images for the sequence and direction
                    Image imgs[] = new Image[frameCount];
                    for (int jj = 0; jj < frameCount; jj++) {
                        int idx = (dir * frameCount) + jj;
                        imgs[jj] = tset.getTile(idx).img;
                    }

                    // create the multi frame image
                    frames[ii][dir] = new MultiFrameImageImpl(imgs);
                }
	    }

	} catch (TileException te) {
	    Log.warning("Exception retrieving character images " +
			"[te=" + te + "].");
            return null;
	}

        return frames;
    }

    /**
     * Returns the file path for the given action sequence and component. 
     */
    protected static String getImageFile (
        String imagedir, ActionSequence as, CharacterComponent c)
    {
        return imagedir + as.fileid + "_" + c.getFileId() + IMAGE_SUFFIX;
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
            Log.warning(
                "Can't composite images with differing frame counts " +
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

    /** The image file name suffix appended to component image file names. */
    protected static final String IMAGE_SUFFIX = ".png";
}
