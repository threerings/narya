//
// $Id: Exit.java,v 1.2 2001/08/11 00:00:13 shaper Exp $

package com.threerings.miso.scene;

/**
 * The <code>Exit</code> class represents a <code>Location</code> in a
 * scene that leads to a different scene.
 */
public class Exit extends Location
{
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
	super(loc.x, loc.y, loc.orient);
	this.name = name;
    }

    /**
     * Return a String representation of this object.
     */
    protected void toString (StringBuffer buf)
    {
	super.toString(buf);
        buf.append(", name=").append(name);
    }
}
