//
// $Id: ExitPoint.java,v 1.5 2001/07/24 22:52:02 shaper Exp $

package com.threerings.miso.scene;

/**
 * The ExitPoint class represents a point in a scene that leads to a
 * different scene.
 */
public class ExitPoint
{
    /** Coordinates for this exit point. */
    public byte x, y;

    /** The scene name this exit transitions to. */
    public String name;

    /** The scene id this exit transitions to. */
    public short sid;

    /**
     * Return a String representation of this ExitPoint object.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[x=").append(x);
        buf.append(", y=").append(y);
        buf.append(", name=").append(name);
        buf.append(", sid=").append(sid);
        return buf.append("]").toString();
    }
}
