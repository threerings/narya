//
// $Id: ExitPoint.java,v 1.4 2001/07/24 19:15:51 shaper Exp $

package com.threerings.miso.scene;

/**
 * The ExitPoint class represents a point in a scene that leads to a
 * different scene.
 */
public class ExitPoint
{
    /** Coordinates for this exit point. */
    public byte x, y;

    /** The scene id this exit transitions to. */
    public short sid;

    /** The scene name this exit transitions to. */
    public String name;
}
