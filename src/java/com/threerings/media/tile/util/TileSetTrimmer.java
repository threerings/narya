//
// $Id: TileSetTrimmer.java,v 1.4 2002/09/11 19:17:55 shaper Exp $

package com.threerings.media.tile.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.WritableRaster;

import javax.imageio.ImageIO;

import com.samskivert.util.StringUtil;

import com.threerings.media.Log;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileSet;
import com.threerings.media.util.ImageUtil;

/**
 * Contains routines for trimming the images from an existing tileset
 * which means that each tile is converted to an image that contains the
 * smallest rectangular region of the original image that contains all
 * non-transparent pixels. These trimmed images are then written out to a
 * single image, packed together left to right.
 */
public class TileSetTrimmer
{
    /**
     * Used to communicate the metrics of the trimmed tiles back to the
     * caller so that they can do what they like with them.
     */
    public static interface TrimMetricsReceiver
    {
        /**
         * Called for each trimmed tile.
         *
         * @param tileIndex the index of the tile in the original tileset.
         * @param imageX the x offset into the newly created tileset image
         * of the trimmed image data.
         * @param imageY the y offset into the newly created tileset image
         * of the trimmed image data.
         * @param trimX the x offset into the untrimmed tile image where
         * the trimmed data begins.
         * @param trimY the y offset into the untrimmed tile image where
         * the trimmed data begins.
         * @param trimWidth the width of the trimmed tile image.
         * @param trimHeight the height of the trimmed tile image.
         */
        public void trimmedTile (int tileIndex, int imageX, int imageY,
                                 int trimX, int trimY,
                                 int trimWidth, int trimHeight);
    }

    /**
     * Generates a trimmed tileset image from the supplied source
     * tileset. The source tileset must be configured with an image
     * provider so that the tile images can be obtained. The tile images
     * will be trimmed and a new tileset image generated and written to
     * the <code>destImage</code> output stream argument.
     *
     * @param source the source tileset.
     * @param destImage an output stream to which the new trimmed image
     * will be written.
     * @param tmr a callback object that will be used to inform the caller
     * of the trimmed tile metrics.
     */
    public static void trimTileSet (
        TileSet source, OutputStream destImage, TrimMetricsReceiver tmr)
        throws IOException
    {
        int tcount = source.getTileCount();
        BufferedImage[] timgs = new BufferedImage[tcount];

        // these will contain the bounds of the trimmed image in the
        // coordinate system defined by the untrimmed image
        Rectangle[] tbounds = new Rectangle[tcount];

        // compute some tile metrics
        int nextx = 0, maxy = 0;
        for (int ii = 0; ii < tcount; ii++) {
            // extract the image from the original tileset
            try {
                Tile tile = source.getTile(ii);
                timgs[ii] = (BufferedImage)tile.getImage();

            } catch (RasterFormatException rfe) {
                throw new IOException("Failed to get tile image " +
                                      "[tidx=" + ii + ", tset=" + source +
                                      ", rfe=" + rfe + "].");

            } catch (NoSuchTileException nste) {
                throw new RuntimeException("WTF? No such tile [tset=" + source +
                                           ", tidx=" + ii + "]");
            }

            // figure out how tightly we can trim it
            tbounds[ii] = new Rectangle();
            ImageUtil.computeTrimmedBounds(timgs[ii], tbounds[ii]);

            // let our caller know what we did
            tmr.trimmedTile(ii, nextx, 0, tbounds[ii].x, tbounds[ii].y,
                            tbounds[ii].width, tbounds[ii].height);

            // adjust the new tileset image dimensions
            maxy = Math.max(maxy, tbounds[ii].height);
            nextx += tbounds[ii].width;
        }

        // create the new tileset image
        BufferedImage image = null;
        try {
            image = ImageUtil.createCompatibleImage(
                (BufferedImage)source.getTileSetImage(), nextx, maxy);

        } catch (RasterFormatException rfe) {
            throw new IOException("Failed to create trimmed tileset image " +
                                  "[wid=" + nextx + ", hei=" + maxy +
                                  ", tset=" + source + ", rfe=" + rfe + "].");
        }

        // copy the tile data
        WritableRaster drast = image.getRaster();
        int xoff = 0;
        for (int ii = 0; ii < tcount; ii++) {
            Rectangle tb = tbounds[ii];
            Raster srast = timgs[ii].getRaster().createChild(
                tb.x, tb.y, tb.width, tb.height, 0, 0, null);
            drast.setRect(xoff, 0, srast);
            xoff += tb.width;
        }

        // write out trimmed image
        ImageIO.write(image, "png", destImage);
    }
}
