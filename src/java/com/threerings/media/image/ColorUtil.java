//
// $Id: ColorUtil.java,v 1.4 2003/01/08 04:09:02 mdb Exp $

package com.threerings.media.image;

import java.awt.Color;

/**
 * Utilities to manipulate colors.
 */
public class ColorUtil
{
    /**
     * Blends the two supplied colors.
     *
     * @return a color halfway between the two colors.
     */
    public static final Color blend (Color c1, Color c2)
    {
        return new Color((c1.getRed() + c2.getRed()) >> 1,
                         (c1.getGreen() + c2.getGreen()) >> 1,
                         (c1.getBlue() + c2.getBlue()) >> 1);
    }

    /**
     * Blends the two supplied colors, using the supplied percentage
     * as the amount of the first color to use.
     *
     * @param firstperc The percentage of the first color to use, from 0.0f
     * to 1.0f inclusive.
     */
    public static final Color blend (Color c1, Color c2, float firstperc)
    {
        float p2 = 1.0f - firstperc;
        return new Color((int) (c1.getRed() * firstperc + c2.getRed() * p2),
                 (int) (c1.getGreen() * firstperc + c2.getGreen() * p2),
                 (int) (c1.getBlue() * firstperc + c2.getBlue() * p2));
    }
}
