//
// $Id: Exit.java,v 1.1 2001/08/09 21:17:06 shaper Exp $

package com.threerings.miso.scene;

/**
 * The <code>Exit</code> class represents a <code>Location</code> in a
 * scene that leads to a different scene.
 *
 * @see Location
 */
public class Exit
{
    /** The location this exit is associated with. */
    public Location loc;

    /** The scene name this exit transitions to. */
    public String name;

    /**
     * Construct an <code>Exit</code> object.
     *
     * @param loc the location associated with the exit.
     * @param name the scene name this exit leads to.
     */
    public Exit (Location loc, String name)
    {
	this.loc = loc;
	this.name = name;
    }

    /**
     * Return a String representation of this object.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[loc=").append(loc);
        buf.append(", name=").append(name);
        return buf.append("]").toString();
    }
}
