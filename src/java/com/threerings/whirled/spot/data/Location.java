package com.threerings.whirled.spot.data;

import com.threerings.io.Streamable;

public interface Location extends Streamable, Cloneable
{
    /**
     * Get a new Location instance that is equals() to this one but that
     * has an orientation facing the opposite direction.
     */
    public Location getOpposite ();

    /**
     * Two locations are equivalent if they specify the same location
     * and orientation.
     */
    public boolean equivalent (Location other);

    /**
     * Two locations are equals if they specify the same coordinates, but
     * the orientation may be different.
     */
    public boolean equals (Object other);

    /**
     * The hashcode of a Location should be based only on its coordinates.
     */
    public int hashCode ();

    /**
     * Locations are cloneable.
     */
    public Object clone ();
}
