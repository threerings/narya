//
// $Id: MaskedUniformTileSet.java,v 1.1 2002/04/06 02:15:37 ray Exp $

package com.threerings.media.tile;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;


/**
 * A tileset created with a regular base tile and a fringe alpha mask.
 */
public class MaskedUniformTileSet extends UniformTileSet
{
    public MaskedUniformTileSet (Tile base, TileSet mask)
    {
        final Image bimg = base.getImage();
        final Image mimg = mask.getTilesetImage();

        setTileCount(mask.getTileCount());
        setWidth(bimg.getWidth(null));
        setHeight(bimg.getHeight(null));

        setImageProvider(new ImageProvider() {

            // perhaps this should be cleaned up
            public Image loadImage (String ignored)
            {
                int bhei = bimg.getHeight(null);
                int bwid = bimg.getWidth(null);

                int height = mimg.getHeight(null);
                int width = mimg.getWidth(null);

                Raster basedata = ((BufferedImage) bimg).getData();
                Raster maskdata = ((BufferedImage) mimg).getData();

                WritableRaster target =
                    basedata.createCompatibleWritableRaster(width, height);

                // now lets copy the entire alpha channel of mimg
                int[] adata = maskdata.getSamples(0, 0, width, height, 3,
                                                  (int[]) null);
                target.setSamples(0, 0, width, height, 3, adata);

                // now copy the RGB from the base image
                for (int ii=0; ii < 3; ii++) {

                    // copy each row from the base image one at a time
                    for (int jj=0; jj < bhei; jj++) {

                        int[] cdata = basedata.getSamples(0, jj, bwid, 1, ii,
                                                          (int[]) null);

                        // paste the row everywhere we need one in the
                        // newly created fringe image
                        for (int y=0; y < (height / bhei); y++) {
                            for (int x=0; x < (width / bwid); x++) {
                                target.setSamples(x * bwid, y * bhei + jj,
                                                  bwid, 1, ii, cdata);
                            }
                        }
                    }
                }

                ColorModel cm = ((BufferedImage) mimg).getColorModel();

                // work it, baby!
                return new BufferedImage(cm, target, true, null);
            }
        });
    }
}
