//
// $Id: Colorization.java,v 1.2 2002/03/08 21:16:07 mdb Exp $

package com.threerings.cast;

import java.awt.Color;
import com.threerings.media.util.ImageUtil;

/**
 * Used to support colorization of character component images.
 */
public class Colorization
{
    /** Every colorization must have a unique name that can be used to
     * compare a particular colorization record with another. */
    public String name;

    /** The root color for the colorization. */
    public Color rootColor;

    /** The range around the root color that will be colorized (in
     * delta-hue, delta-saturation, delta-value. */
    public float[] range;

    /** The adjustments to make to hue, saturation and value. */
    public float[] offsets;

    /**
     * Constructs a colorization record with the specified name.
     */
    public Colorization (String name, Color rootColor,
                         float[] range, float[] offsets)
    {
        this.name = name;
        this.rootColor = rootColor;
        this.range = range;
        this.offsets = offsets;
    }

    /**
     * Returns the root color adjusted by the colorization.
     */
    public Color getColorizedRoot ()
    {
        float[] hsv = Color.RGBtoHSB(rootColor.getRed(), rootColor.getGreen(),
                                     rootColor.getBlue(), null);
        return new Color(ImageUtil.recolorColor(hsv, offsets));
    }

    /**
     * Compares this colorization to another based on name.
     */
    public boolean equals (Object other)
    {
        if (other instanceof Colorization) {
            return ((Colorization)other).name.equals(name);
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of this colorization.
     */
    public String toString ()
    {
        return name;
    }
}
