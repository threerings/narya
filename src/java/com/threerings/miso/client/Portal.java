//
// $Id: Portal.java,v 1.3 2001/08/21 01:15:16 shaper Exp $

package com.threerings.miso.scene;

/**
 * The portal class represents a {@link Location} in a scene that both
 * leads to a different scene and serves as a potential entrance into
 * its containing scene.
 */
public class Portal extends Location
{
    /** The portal name used for binding the portal to another scene. */
    public String name;

    /** The destination scene id. */
    public int sid;

    /** The destination portal within the destination scene. */
    public Portal dest;

    /**
     * Construct a portal object.
     *
     * @param loc the location associated with the portal.
     * @param name the portal name.
     */
    public Portal (Location loc, String name)
    {
	super(loc.x, loc.y, loc.orient);
	this.name = name;
	sid = MisoScene.SID_INVALID;
    }

    /**
     * Set the destination information for this portal.
     *
     * @param sid the scene id.
     * @param dest the destination portal.
     */
    public void setDestination (int sid, Portal dest)
    {
	this.sid = sid;
	this.dest = dest;
    }

    /**
     * Return whether this portal has a valid destination scene and
     * portal.
     */
    public boolean hasDestination ()
    {
	return (sid != MisoScene.SID_INVALID && dest != null);
    }

    /**
     * Return a String representation of this object.
     */
    protected void toString (StringBuffer buf)
    {
	super.toString(buf);
        buf.append(", name=").append(name);
	buf.append(", sid=").append(sid);
	buf.append(", dest=").append(dest);
    }
}
