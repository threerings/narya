//
// $Id: Colorization.java,v 1.1 2002/03/08 07:50:32 mdb Exp $

package com.threerings.cast;

import java.awt.Color;

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
