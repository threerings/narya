//
// $Id: TileSetTrimmer.java,v 1.1 2002/06/19 08:29:59 mdb Exp $

package com.threerings.media.tile.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.imageio.ImageIO;

import com.samskivert.util.StringUtil;

import com.threerings.media.Log;

import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TrimmedTileSet;
import com.threerings.media.util.ImageUtil;

/**
 * Contains routines for generating a trimmed tileset from an existing
 * tileset. Both a {@link TrimmedTileSet} instance and the associated
 * trimmed image are created.
 */
public class TileSetTrimmer
{
    /**
     * Generates a {@link TrimmedTileSet} from the supplied source
     * tileset. The source tileset must be configured with an image
     * provider so that the tile images can be obtained. The tile images
     * will be trimmed and a new tileset image generated and written to
     * the <code>destImage</code> output stream argument.
     *
     * @param source the source tileset.
     * @param destImage an output stream to which the new trimmed image
     * will be written.
     */
    public static TrimmedTileSet trimTileSet (
        TileSet source, OutputStream destImage)
        throws IOException
    {
        int tcount = source.getTileCount();
        BufferedImage[] timgs = new BufferedImage[tcount];

        // these will contain the width and height of the untrimmed tile,
        // but the x and y offset of the trimmed tile image in the
        // supplied tileset source image
        Rectangle[] obounds = new Rectangle[tcount];

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
            } catch (NoSuchTileException nste) {
                throw new RuntimeException("WTF? No such tile [tset=" + source +
                                           ", tidx=" + ii + "]");
            }

            // figure out how tightly we can trim it
            obounds[ii] = new Rectangle(
                0, 0, timgs[ii].getWidth(), timgs[ii].getHeight());
            tbounds[ii] = new Rectangle();
            ImageUtil.computeTrimmedBounds(timgs[ii], tbounds[ii]);

            // assign it a location in the trimmed tile image
            obounds[ii].x = nextx;
            nextx += tbounds[ii].width;
            maxy = Math.max(maxy, tbounds[ii].height);
        }

        // create our tileset object
        TrimmedTileSet ttset = new TrimmedTileSet();
        ttset.setTileMetrics(obounds, tbounds);

//         Log.info("Trimming [obounds=" + StringUtil.toString(obounds) +
//                  ", tbounds=" + StringUtil.toString(tbounds) +
//                  ", nwidth=" + nextx + ", nheight=" + maxy + "].");

        // create the new tileset image
        BufferedImage image = ImageUtil.createCompatibleImage(
            (BufferedImage)source.getTileSetImage(), nextx, maxy);

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

        // we're good to go
        return ttset;
    }
}
