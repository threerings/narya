//
// $Id: ZoneSummary.java,v 1.6 2004/08/23 21:05:04 mdb Exp $

package com.threerings.whirled.zone.data;

import com.samskivert.util.StringUtil;

import com.threerings.io.TrackedStreamableObject;
import com.threerings.util.Name;

/**
 * The zone summary contains information on a zone, including its name and
 * summary info on all of the scenes in this zone (which can be used to
 * generate a map of the zone on the client).
 */
public class ZoneSummary extends TrackedStreamableObject
{
    /** The zone's fully qualified unique identifier. */
    public int zoneId;

    /** The name of the zone. */
    public Name name;

    /** The summary information for all of the scenes in the zone. */
    public SceneSummary[] scenes;

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[zoneId=" + zoneId + ", name=" + name +
            ", scenes=" + StringUtil.toString(scenes) + "]";
    }
}
