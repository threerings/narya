//
// $Id: ImageUtil.java,v 1.7 2002/03/08 21:05:01 mdb Exp $

package com.threerings.media.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

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
        return _gc.createCompatibleImage(width, height, transparency);
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
        ColorModel cm = image.getColorModel();
        if (!(cm instanceof IndexColorModel)) {
            String errmsg = "Unable to recolor images with non-index color " +
                "model [cm=" + cm.getClass() + "]";
            throw new RuntimeException(errmsg);
        }

        // first convert the root color to HSV for later comparison
        float[] rHSV = Color.RGBtoHSB(rootColor.getRed(), rootColor.getGreen(),
                                      rootColor.getBlue(), null);
        int[] frHSV = toFixedHSV(rHSV, null);
        int[] rgb = new int[3];

        // now process the image
        IndexColorModel icm = (IndexColorModel)cm;
        int size = icm.getMapSize();
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

            // check to see that this color is sufficiently "close" to the
            // root color based on the supplied distance parameters
            toFixedHSV(hsv, fhsv);
            if (distance(fhsv[0], frHSV[0], Short.MAX_VALUE) >=
                dists[0] * Short.MAX_VALUE) {
                continue;
            }

            // saturation and value don't wrap around like hue
            if (Math.abs(rHSV[1] - hsv[1]) >= dists[1] ||
                Math.abs(rHSV[2] - hsv[2]) >= dists[2]) {
                continue;
            }

            // massage the HSV bands and update the RGBs array
            rgbs[i] = recolorColor(hsv, offsets);
        }

        // create a new image with the adjusted color palette
        IndexColorModel nicm = new IndexColorModel(
            icm.getPixelSize(), size, rgbs, 0, icm.hasAlpha(),
            icm.getTransparentPixel(), icm.getTransferType());
        return new BufferedImage(nicm, image.getRaster(), false, null);
    }

    /**
     * Adjusts the supplied color by the specified offests, taking the
     * appropriate measures for hue (wrapping it around) and saturation
     * and value (clipping).
     *
     * @return the RGB value of the recolored color.
     */
    public static int recolorColor (float[] hsv, float[] offsets)
    {
        // for hue, we wrap around
        hsv[0] += offsets[0];
        if (hsv[0] > 1.0) {
            hsv[0] -= 1.0;
        }

        // otherwise we clip
        hsv[1] = Math.min(Math.max(hsv[1] + offsets[1], 0), 1);
        hsv[2] = Math.min(Math.max(hsv[2] + offsets[2], 0), 1);

        // convert back to RGB space
        return Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * Converts floating point HSV values to a fixed point integer
     * representation.
     */
    protected static int[] toFixedHSV (float[] hsv, int[] fhsv)
    {
        if (fhsv == null) {
            fhsv = new int[hsv.length];
        }
        for (int i = 0; i < hsv.length; i++) {
            // fhsv[i] = (int)(hsv[i]*Integer.MAX_VALUE);
            fhsv[i] = (int)(hsv[i]*Short.MAX_VALUE);
        }
        return fhsv;
    }

    /**
     * Returns the distance between the supplied to numbers modulo N.
     */
    protected static int distance (int a, int b, int N)
    {
        return (a > b) ? Math.min(a-b, b+N-a) : Math.min(b-a, a+N-b);
    }

    /** The graphics configuration for the default screen device. */
    protected static GraphicsConfiguration _gc;
    static {
        // obtain information on our graphics environment
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = env.getDefaultScreenDevice();
        _gc = gd.getDefaultConfiguration();
    };
}
