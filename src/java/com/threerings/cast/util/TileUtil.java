//
// $Id: TileUtil.java,v 1.14 2002/05/04 19:38:14 mdb Exp $

package com.threerings.cast.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.threerings.media.util.ImageUtil;

import com.threerings.cast.Log;
import com.threerings.cast.ActionFrames;
import com.threerings.cast.Colorization;

/**
 * Miscellaneous tile-related utility functions.
 */ 
public class TileUtil
{
    /**
     * Renders and returns the <code>index</code>th image from each of the
     * supplied source multi-frame images to a newly created buffered
     * image. This is used to render a single frame of a composited
     * character action, and accordingly, the source image array should be
     * already sorted into the proper rendering order.
     */
    public static Image compositeFrames (
        int orient, int index, ActionFrames[] sources,
        Colorization[][] zations)
    {
        Image dest = null;
        Graphics g = null;
//         long start = System.currentTimeMillis();

        for (int ii = 0; ii < sources.length; ii++) {
            // create the image now that we know how big it should be
            if (dest == null) {
                dest = ImageUtil.createImage(sources[ii].getWidth(index),
                                             sources[ii].getHeight(index));
                g = dest.getGraphics();
            }

            // make sure the action frames are configured with the right
            // orientation before we render
            sources[ii].setOrientation(orient);

            // render the particular action from this component into the
            // target image
            Colorization[] cz = (zations != null) ? zations[ii] : null;
            sources[ii].paintColoredFrame(g, index, 0, 0, cz);
        }

        // clean up after ourselves
        if (g != null) {
            g.dispose();
        }

//         long now = System.currentTimeMillis();
//         Log.info("Composited " + sources.length + " frames in " +
//                  (now-start) + " millis.");

        return dest;
    }
}
