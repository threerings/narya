//
// $Id: TileUtil.java,v 1.13 2002/05/04 06:57:24 mdb Exp $

package com.threerings.cast.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.util.ImageUtil;

import com.threerings.util.DirectionCodes;

import com.threerings.cast.Log;
import com.threerings.cast.Colorization;

/**
 * Miscellaneous tile-related utility functions.
 */ 
public class TileUtil
    implements DirectionCodes
{
//     /**
//      * Renders each of the given <code>src</code> component frames into
//      * the corresponding frames of <code>dest</code>, allocating blank
//      * image frames for <code>dest</code> if none yet exist. If
//      * <code>zations</code> is not null, the provided list of
//      * colorizations will be applied as well.
//      *
//      * @throws IllegalArgumentException if the frame count of the source
//      * and destination images don't match.
//      */
//     public static void compositeFrames (
//         MultiFrameImage[] dest, MultiFrameImage[] src,
//         Colorization[] zations)
//     {
//         for (int orient = 0; orient < DIRECTION_COUNT; orient++) {
//             MultiFrameImage sframes = src[orient];
//             MultiFrameImage dframes = dest[orient];

//             // create blank destination frames if needed
//             if (dframes == null) {
//                 dframes = (dest[orient] = new BlankFrameImage(sframes));
//             }

//             // sanity check
//             int dsize = dframes.getFrameCount();
//             int ssize = sframes.getFrameCount();
//             if (dsize != ssize) {
//                 String errmsg = "Can't composite images with inequal " +
//                     "frame counts [dest=" + dsize + ", src=" + ssize + "].";
//                 throw new IllegalArgumentException(errmsg);
//             }

//             // slap the images together
//             for (int ii = 0; ii < dsize; ii++) {
//                 Image dimg = dframes.getFrame(ii);
//                 BufferedImage simg = (BufferedImage)sframes.getFrame(ii);

//                 // recolor the source image
//                 if (zations != null) {
//                     for (int i = 0; i < zations.length; i++) {
//                         Colorization cz = zations[i];
//                         if (zations[i] != null) {
//                             simg = ImageUtil.recolorImage(
//                                 simg, cz.rootColor, cz.range, cz.offsets);
//                         }
//                     }
//                 }

//                 // now splat the recolored image onto the target
//                 Graphics g = dimg.getGraphics();
//                 g.drawImage(simg, 0, 0, null);
//                 g.dispose();
//             }
//         }
//     }

    /**
     * Renders and returns the <code>index</code>th image from each of the
     * supplied source multi-frame images to a newly created buffered
     * image. This is used to render a single frame of a composited
     * character action, and accordingly, the source image array should be
     * already sorted into the proper rendering order.
     */
    public static Image compositeFrames (
        int index, MultiFrameImage[] sources, Colorization[][] zations)
    {
        Image dest = null;
        Graphics g = null;

        long start = System.currentTimeMillis();
        for (int ii = 0; ii < sources.length; ii++) {
            BufferedImage simg = (BufferedImage)sources[ii].getFrame(index);

            // create the image now that we know how big it should be
            if (dest == null) {
                dest = ImageUtil.createImage(
                    simg.getWidth(null), simg.getHeight(null));
                g = dest.getGraphics();
            }

            // recolor the source image
            if (zations != null) {
                int zcount = zations[ii].length;
                for (int zz = 0; zz < zcount; zz++) {
                    Colorization cz = zations[ii][zz];
                    if (cz != null) {
                        simg = ImageUtil.recolorImage(
                            simg, cz.rootColor, cz.range, cz.offsets);
                    }
                }
            }

            // now splat the recolored image onto the target
            g.drawImage(simg, 0, 0, null);
        }

        // clean up after ourselves
        if (g != null) {
            g.dispose();
        }

        long now = System.currentTimeMillis();
        Log.info("Composited " + sources.length + " frames in " +
                 (now-start) + " millis.");

        return dest;
    }

    /** The image file name suffix appended to component image file names. */
    protected static final String IMAGE_SUFFIX = ".png";
}
