//
// $Id: ExitPoint.java,v 1.1 2001/07/12 22:38:03 shaper Exp $

package com.threerings.cocktail.miso.scene;

/**
 * Represents a point in a scene that leads to a different scene.
 */
public class ExitPoint
{
    byte x, y;   // coordinates for this exit point
    short sid;   // scene id this exit transitions to
}
