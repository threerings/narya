//
// $Id: ImageUtil.java,v 1.17 2002/06/20 17:54:26 mdb Exp $

package com.threerings.media.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Transparency;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import com.samskivert.util.StringUtil;
import com.threerings.media.Log;

/**
 * Image related utility functions.
 */
public class ImageUtil
{
    /**
     * Extracts a subimage from the supplied image with the specified
     * dimensions. If the supplied image is an instance of {@link
     * BufferedImage}, then the subimage will simply reference the main
     * image. If it is not, the subimage will be created and the data will
     * be rendered into the newly created image.
     *
     * @param source the source image.
     * @param x the left coordinate of the sub-image.
     * @param y the top coordinate of the sub-image.
     * @param width the sub-image width.
     * @param height the sub-image height.
     *
     * @return the desired subimage.
     */
    public static Image getSubimage (
        Image source, int x, int y, int width, int height)
    {
        if (source instanceof BufferedImage) {
            return ((BufferedImage)source).getSubimage(x, y, width, height);

        } else {
            BufferedImage target = createImage(width, height);
            Graphics g = target.getGraphics();
            g.drawImage(source, 0, 0, width, height,
                        x, y, x+width, y+height, null);
            g.dispose();
            return target;
        }
    }

    /**
     * Creates a new blank image with the given dimensions and
     * transparency set to {@link Transparency#BITMASK}.  The format of
     * the created image is compatible with the graphics configuration of
     * the default screen device, such that no format conversion will be
     * necessary when rendering the image to that device.
     *
     * @param width the desired image width.
     * @param height the desired image height.
     *
     * @return the blank image.
     */
    public static BufferedImage createImage (int width, int height)
    {
        return createImage(width, height, Transparency.BITMASK);
    }

    /**
     * Creates a new blank image with the given dimensions and
     * transparency.  The format of the created image is compatible with
     * the graphics configuration of the default screen device, such that
     * no format conversion will be necessary when rendering the image to
     * that device.
     *
     * @param width the desired image width.
     * @param height the desired image height.
     * @param transparency the desired image transparency; one of the
     * constants in {@link java.awt.Transparency}.
     *
     * @return the blank image.
     */
    public static BufferedImage createImage (
        int width, int height, int transparency)
    {
        return getDefGC().createCompatibleImage(width, height, transparency);
    }

    /**
     * Creates a new buffered image with the same sample model and color
     * model as the source image but with the new width and height.
     */
    public static BufferedImage createCompatibleImage (
        BufferedImage source, int width, int height)
    {
        WritableRaster raster =
            source.getRaster().createCompatibleWritableRaster(width, height);
        return new BufferedImage(source.getColorModel(), raster, false, null);
    }

    /**
     * Used to recolor images by shifting bands of color (in HSV color
     * space) to a new hue. The source images must be 8-bit color mapped
     * images, as the recoloring process works by analysing the color map
     * and modifying it.
     */
    public static BufferedImage recolorImage (
        BufferedImage image, Color rootColor, float[] dists, float[] offsets)
    {
        return recolorImage(image, new Colorization[] {
            new Colorization(-1, rootColor, dists, offsets) });
    }

    /**
     * Recolors the supplied image as in {@link
     * #recolorImage(BufferedImage,Color,float[],float[])} obtaining the
     * recoloring parameters from the supplied {@link Colorization}
     * instance.
     */
    public static BufferedImage recolorImage (
        BufferedImage image, Colorization cz)
    {
        return recolorImage(image, new Colorization[] { cz });
    }

    /**
     * Recolors the supplied image using the supplied colorizations.
     */
    public static BufferedImage recolorImage (
        BufferedImage image, Colorization[] zations)
    {
        ColorModel cm = image.getColorModel();
        if (!(cm instanceof IndexColorModel)) {
            String errmsg = "Unable to recolor images with non-index color " +
                "model [cm=" + cm.getClass() + "]";
            throw new RuntimeException(errmsg);
        }

        // now process the image
        IndexColorModel icm = (IndexColorModel)cm;
        int size = icm.getMapSize();
        int zcount = zations.length;
        int[] rgbs = new int[size];

        // fetch the color data
        icm.getRGBs(rgbs);

        // convert the colors to HSV
        float[] hsv = new float[3];
        int[] fhsv = new int[3];
        int tpixel = -1;
        for (int i = 0; i < size; i++) {
            int value = rgbs[i];

            // don't fiddle with alpha pixels
            if ((value & 0xFF000000) == 0) {
                tpixel = i;
                continue;
            }

            // convert the color to HSV
            int red = (value >> 16) & 0xFF;
            int green = (value >> 8) & 0xFF;
            int blue = (value >> 0) & 0xFF;
            Color.RGBtoHSB(red, green, blue, hsv);
            Colorization.toFixedHSV(hsv, fhsv);

            // see if this color matches and of our colorizations and
            // recolor it if it does
            for (int z = 0; z < zcount; z++) {
                Colorization cz = zations[z];
                if (cz != null && cz.matches(hsv, fhsv)) {
                    // massage the HSV bands and update the RGBs array
                    rgbs[i] = cz.recolorColor(hsv);
                    break;
                }
            }
        }

        // create a new image with the adjusted color palette
        IndexColorModel nicm = new IndexColorModel(
            icm.getPixelSize(), size, rgbs, 0, icm.hasAlpha(),
            icm.getTransparentPixel(), icm.getTransferType());
        return new BufferedImage(nicm, image.getRaster(), false, null);
    }

    /**
     * Paints multiple copies of the supplied image using the supplied
     * graphics context such that the requested width is filled with the
     * image.
     */
    public static void tileImageAcross (Graphics g, Image image,
                                        int x, int y, int width)
    {
        int iwidth = image.getWidth(null), iheight = image.getHeight(null);
        int tcount = width/iwidth, extra = width % iwidth;

        // draw the full copies of the image
        for (int ii = 0; ii < tcount; ii++) {
            g.drawImage(image, x, y, null);
            x += iwidth;
        }

        // clip the final blit
        if (extra > 0) {
            Shape oclip = g.getClip();
            g.clipRect(x, y, extra, iheight);
            g.drawImage(image, x, y, null);
            g.setClip(oclip);
        }
    }

    /**
     * Paints multiple copies of the supplied image using the supplied
     * graphics context such that the requested height is filled with the
     * image.
     */
    public static void tileImageDown (Graphics g, Image image,
                                      int x, int y, int height)
    {
        int iwidth = image.getWidth(null), iheight = image.getHeight(null);
        int tcount = height/iheight, extra = height % iheight;

        // draw the full copies of the image
        for (int ii = 0; ii < tcount; ii++) {
            g.drawImage(image, x, y, null);
            y += iheight;
        }

        // clip the final blit
        if (extra > 0) {
            Shape oclip = g.getClip();
            g.clipRect(x, y, iwidth, extra);
            g.drawImage(image, x, y, null);
            g.setClip(oclip);
        }
    }

    /**
     * Create an image using the alpha channel from the first and the RGB
     * values from the second.
     */
    public static BufferedImage composeMaskedImage (
        BufferedImage mask, BufferedImage base)
    {
        int wid = base.getWidth(null);
        int hei = base.getHeight(null);

        Raster maskdata = mask.getData();
        Raster basedata = base.getData();

        // create a new image using the rasters if possible
        if (maskdata.getNumBands() == 4 && basedata.getNumBands() >= 3) {
            WritableRaster target =
                basedata.createCompatibleWritableRaster(wid, hei);

            // copy the alpha from the mask image
            int[] adata = maskdata.getSamples(0, 0, wid, hei, 3, (int[]) null);
            target.setSamples(0, 0, wid, hei, 3, adata);

            // copy the RGB from the base image
            for (int ii=0; ii < 3; ii++) {
                int[] cdata = basedata.getSamples(0, 0, wid, hei,
                                                  ii, (int[]) null);
                target.setSamples(0, 0, wid, hei, ii, cdata);
            }

            return new BufferedImage(mask.getColorModel(), target, true, null);

        } else {
            // otherwise composite them by rendering them with an alpha
            // rule
            BufferedImage target = createImage(wid, hei);
            Graphics2D g2 = target.createGraphics();
            try {
                g2.drawImage(mask, 0, 0, null);
                g2.setComposite(AlphaComposite.SrcIn);
                g2.drawImage(base, 0, 0, null);
            } finally {
                g2.dispose();
            }
            return target;
        }
    }

    /**
     * Returns true if the supplied image contains a non-transparent pixel
     * at the specified coordinates, false otherwise.
     */
    public static boolean hitTest (Image image, int x, int y)
    {
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage)image;
            int argb = bimage.getRGB(x, y);
            // int alpha = argb >> 24;
            // Log.info("Checking [x=" + x + ", y=" + y + ", " + alpha);

            // it's only a hit if the pixel is non-transparent
            return (argb >> 24) != 0;

        } else {
            Log.warning("Can't check for transparent pixel " +
                        "[image=" + image + "].");
            return true;
        }
    }

    /**
     * Computes the bounds of the smallest rectangle that contains all
     * non-transparent pixels of this image. This isn't extremely
     * efficient, so you shouldn't be doing this anywhere exciting.
     */
    public static void computeTrimmedBounds (
        BufferedImage image, Rectangle tbounds)
    {
        // this could be more efficient, but it's run as a batch process
        // and doesn't really take that long anyway
        int width = image.getWidth(), height = image.getHeight();

        int firstrow = -1, lastrow = -1, minx = width, maxx = 0;
        for (int yy = 0; yy < height; yy++) {

            int firstidx = -1, lastidx = -1;
            for (int xx = 0; xx < width; xx++) {
                // if this pixel is transparent, do nothing
                int argb = image.getRGB(xx, yy);
                if ((argb >> 24) == 0) {
                    continue;
                }

                // otherwise, if we've not seen a non-transparent pixel,
                // make a note that this is the first non-transparent
                // pixel in the row
                if (firstidx == -1) {
                    firstidx = xx;
                }
                // keep track of the last non-transparent pixel we saw
                lastidx = xx;
            }

            // if we saw no pixels on this row, we can bail now
            if (firstidx == -1) {
                continue;
            }

            // update our min and maxx
            minx = Math.min(firstidx, minx);
            maxx = Math.max(lastidx, maxx);

            // otherwise keep track of the first row on which we see
            // pixels and the last row on which we see pixels
            if (firstrow == -1) {
                firstrow = yy;
            }
            lastrow = yy;
        }

        // fill in the dimensions
        tbounds.x = minx;
        tbounds.y = firstrow;
        tbounds.width = maxx - minx + 1;
        tbounds.height = lastrow - firstrow + 1;
    }

    /**
     * Obtains the default graphics configuration for this VM.
     */
    protected static GraphicsConfiguration getDefGC ()
    {
        if (_gc == null) {
            // obtain information on our graphics environment
            GraphicsEnvironment env =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = env.getDefaultScreenDevice();
            _gc = gd.getDefaultConfiguration();
        }
        return _gc;
    }

    /** The graphics configuration for the default screen device. */
    protected static GraphicsConfiguration _gc;
}
