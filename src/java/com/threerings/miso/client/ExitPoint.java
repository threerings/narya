//
// $Id: ExitPoint.java,v 1.2 2001/07/18 21:45:42 shaper Exp $

package com.threerings.miso.scene;

/**
 * Represents a point in a scene that leads to a different scene.
 */
public class ExitPoint
{
    byte x, y;   // coordinates for this exit point
    short sid;   // scene id this exit transitions to
}
