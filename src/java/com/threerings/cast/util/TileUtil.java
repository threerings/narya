//
// $Id: TileUtil.java,v 1.8 2002/02/24 02:20:43 mdb Exp $

package com.threerings.cast.util;

import java.awt.Image;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.ImageUtil;

import com.threerings.cast.Log;

/**
 * Miscellaneous tile-related utility functions.
 */ 
public class TileUtil
{
    /**
     * Renders each of the given <code>src</code> component frames into
     * the corresponding frames of <code>dest</code>, allocating blank
     * image frames for <code>dest</code> if none yet exist.
     *
     * @throws IllegalArgumentException if the frame count of the source
     * and destination images don't match.
     */
    public static void compositeFrames (
        MultiFrameImage[] dest, MultiFrameImage[] src)
    {
        for (int orient = 0; orient < Sprite.DIRECTION_COUNT; orient++) {
            MultiFrameImage sframes = src[orient];
            MultiFrameImage dframes = dest[orient];

            // create blank destination frames if needed
            if (dframes == null) {
                dframes = (dest[orient] = new BlankFrameImage(sframes));
            }

            // sanity check
            int dsize = dframes.getFrameCount();
            int ssize = sframes.getFrameCount();
            if (dsize != ssize) {
                String errmsg = "Can't composite images with inequal " +
                    "frame counts [dest=" + dsize + ", src=" + ssize + "].";
                throw new IllegalArgumentException(errmsg);
            }

            // slap the images together
            for (int ii = 0; ii < dsize; ii++) {
                Image dimg = dframes.getFrame(ii);
                Image simg = sframes.getFrame(ii);
                dimg.getGraphics().drawImage(simg, 0, 0, null);
            }
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
         * Constructs a blank frame image based on the dimensions of
         * template multi-frame image.
         */
        public BlankFrameImage (MultiFrameImage template)
        {
            int frameCount = template.getFrameCount();
            _images = new Image[frameCount];
            for (int i = 0; i < frameCount; i++) {
                Image img = template.getFrame(i);
                _images[i] = ImageUtil.createImage(
                    img.getWidth(null), img.getHeight(null));
            }
        }

        // documentation inherited
        public int getFrameCount ()
        {
            return _images.length;
        }

        // documentation inherited
        public Image getFrame (int index)
        {
            return _images[index];
        }

        /** The frame images. */
        protected Image[] _images;
    }

    /** The image file name suffix appended to component image file names. */
    protected static final String IMAGE_SUFFIX = ".png";
}
