//
// $Id: Colorization.java,v 1.3 2002/03/10 22:30:46 mdb Exp $

package com.threerings.cast;

import java.awt.Color;
import com.samskivert.util.StringUtil;
import com.threerings.media.util.ImageUtil;

/**
 * Used to support colorization of character component images.
 */
public class Colorization
{
    /** Every colorization must have a unique id that can be used to
     * compare a particular colorization record with another. */
    public int colorizationId;

    /** The root color for the colorization. */
    public Color rootColor;

    /** The range around the root color that will be colorized (in
     * delta-hue, delta-saturation, delta-value. */
    public float[] range;

    /** The adjustments to make to hue, saturation and value. */
    public float[] offsets;

    /**
     * Constructs a colorization record with the specified identifier.
     */
    public Colorization (int colorizationId, Color rootColor,
                         float[] range, float[] offsets)
    {
        this.colorizationId = colorizationId;
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
     * Compares this colorization to another based on id.
     */
    public boolean equals (Object other)
    {
        if (other instanceof Colorization) {
            return ((Colorization)other).colorizationId == colorizationId;
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of this colorization.
     */
    public String toString ()
    {
        return String.valueOf(colorizationId);
    }

    /**
     * Returns a long string representation of this colorization.
     */
    public String toVerboseString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
