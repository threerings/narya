//
// $Id: ZonedBodyObject.java,v 1.2 2002/09/20 00:54:06 mdb Exp $

package com.threerings.whirled.zone.data;

import com.threerings.whirled.data.ScenedBodyObject;

/**
 * A system that uses the zone services must provide a body object
 * extension that implements this interface.
 */
public interface ZonedBodyObject extends ScenedBodyObject
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
