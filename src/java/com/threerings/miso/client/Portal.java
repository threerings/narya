//
// $Id: Portal.java,v 1.1 2001/08/16 18:05:17 shaper Exp $

package com.threerings.miso.scene;

/**
 * The <code>Portal</code> class represents a <code>Location</code> in
 * a scene that both leads to a different scene and serves as a
 * potential entrance into its containing scene.
 */
public class Portal extends Location
{
    /** The portal name used for binding the portal to another scene. */
    public String name;

    /**
     * Construct an <code>Portal</code> object.
     *
     * @param loc the location associated with the portal.
     * @param name the portal name.
     */
    public Portal (Location loc, String name)
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
