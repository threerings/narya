//
// $Id: ZonedBodyObject.java,v 1.1 2001/12/04 00:31:58 mdb Exp $

package com.threerings.whirled.zone.data;

/**
 * A system that uses the zone services must provide a body object
 * extension that implements this interface.
 */
public interface ZonedBodyObject
{
    /**
     * Returns the zone id currently occupied by this body.
     */
    public int getZoneId ();

    /**
     * Sets the zone id currently occupied by this body.
     */
    public void setZoneId (int zoneId);
}
