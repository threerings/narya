//
// $Id: EditableLocation.java,v 1.1 2001/12/04 22:34:04 mdb Exp $

package com.threerings.whirled.tools.spot;

import com.threerings.whirled.spot.data.Location;

/**
 * An editable location contains a name as well as the standard location
 * information.
 */
public class EditableLocation
{
    /** The location whose data we extend. (My kingdom for multiple
     * inheritance.) */
    public Location location;

    /** The human-readable name of this location. */
    public String name;

    /**
     * Constructs an editable location. A location delegate will be
     * created with the supplied basic location information.
     */
    public EditableLocation (int id, int x, int y, int orientation,
                             int clusterIndex, String name)
    {
        this(new Location(id, x, y, orientation, clusterIndex), name);
    }

    /**
     * Constructs an editable location with the supplied location
     * delegate.
     */
    public EditableLocation (Location source, String name)
    {
        location = source;
        this.name = name;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        delegatesToString(buf);
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * Generates a string representation of this instance's delegates into
     * the supplied string buffer.
     */
    protected void delegatesToString (StringBuffer buf)
    {
        buf.append(location);
    }

    /**
     * Generates a string representation of this instance's members into
     * the supplied string buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append(", name=").append(name);
    }
}
