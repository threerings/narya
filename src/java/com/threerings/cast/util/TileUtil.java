//
// $Id: TileUtil.java,v 1.4 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast.util;

import java.awt.Image;
import java.awt.image.BufferedImage;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.sprite.Sprite;
// import com.threerings.media.tile.*;

import com.threerings.cast.Log;
// import com.threerings.cast.*;

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
        for (int orient = 0; orient < Sprite.NUM_DIRECTIONS; orient++) {
            MultiFrameImage sframes = src[orient];
            MultiFrameImage dframes = dest[orient];

            // create blank destination frames if needed
            if (dframes == null) {
                dest[orient] = new BlankFrameImage(sframes);
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

//     /**
//      * Returns a two-dimensional array of multi frame images containing
//      * the frames of animation used to render the sprite while standing or
//      * walking in each of the directions it may face.
//      */
//     public static MultiFrameImage[][] getComponentFrames (
//         String imagedir, CharacterComponent c)
//     {
//         ActionSequence seqs[] = c.getActionSequences();
//         MultiFrameImage frames[][] =
//             new MultiFrameImage[seqs.length][Sprite.NUM_DIRECTIONS];

// 	try {
//             for (int ii = 0; ii < seqs.length; ii++) {
//                 ActionSequence as = seqs[ii];

//                 // get the tile set containing the component tiles for
//                 // this action sequence
//                 String file = getImageFile(imagedir, as, c);
//                 TileSet tset = null;

//                 try {
//                     tset = as.tileset.clone(file);
//                 } catch (CloneNotSupportedException e) {
//                     Log.warning("Failed to clone tile set " +
//                                 "[tset=" + as.tileset + "].");
//                     return null;
//                 }

//                 // get the number of frames of animation
//                 int frameCount = tset.getTileCount() / Sprite.NUM_DIRECTIONS;

//                 for (int dir = 0; dir < Sprite.NUM_DIRECTIONS; dir++) {
//                     // retrieve all images for the sequence and direction
//                     Image imgs[] = new Image[frameCount];
//                     for (int jj = 0; jj < frameCount; jj++) {
//                         int idx = (dir * frameCount) + jj;
//                         imgs[jj] = tset.getTileImage(idx);
//                     }

//                     // create the multi frame image
//                     frames[ii][dir] = new MultiFrameImageImpl(imgs);
//                 }
// 	    }

// 	} catch (TileException te) {
// 	    Log.warning("Exception retrieving character images " +
// 			"[te=" + te + "].");
//             return null;
// 	}

//         return frames;
//     }

//     /**
//      * Returns the file path for the given action sequence and component. 
//      */
//     protected static String getImageFile (
//         String imagedir, ActionSequence as, CharacterComponent c)
//     {
//         return imagedir + as.fileid + "_" + c.getFileId() + IMAGE_SUFFIX;
//     }

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
                _images[i] = new BufferedImage(img.getWidth(null),
                                               img.getHeight(null),
                                               BufferedImage.TYPE_INT_ARGB);
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
