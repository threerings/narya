//
// $Id: TileUtil.java,v 1.10 2002/03/08 18:13:14 mdb Exp $

package com.threerings.cast.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.ImageUtil;

import com.threerings.cast.Log;
import com.threerings.cast.Colorization;

/**
 * Miscellaneous tile-related utility functions.
 */ 
public class TileUtil
{
    /**
     * Renders each of the given <code>src</code> component frames into
     * the corresponding frames of <code>dest</code>, allocating blank
     * image frames for <code>dest</code> if none yet exist. If
     * <code>zations</code> is not null, the provided list of
     * colorizations will be applied as well.
     *
     * @throws IllegalArgumentException if the frame count of the source
     * and destination images don't match.
     */
    public static void compositeFrames (
        MultiFrameImage[] dest, MultiFrameImage[] src,
        Colorization[] zations)
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
                BufferedImage simg = (BufferedImage)sframes.getFrame(ii);

                // recolor the source image
                if (zations != null) {
                    for (int i = 0; i < zations.length; i++) {
                        Colorization cz = zations[i];
                        simg = ImageUtil.recolorImage(
                            simg, cz.rootColor, cz.range, cz.offsets);
                    }
                }

                // now splat the recolored image onto the target
                Graphics g = dimg.getGraphics();
                g.drawImage(simg, 0, 0, null);
                g.dispose();
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
