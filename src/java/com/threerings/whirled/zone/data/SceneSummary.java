//
// $Id: SceneSummary.java,v 1.7 2004/02/25 14:50:28 mdb Exp $

package com.threerings.whirled.zone.data;

import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;
import com.threerings.util.DirectionUtil;

/**
 * The scene summary class is used to provide info about the connected
 * group of scenes that make up a zone. The group of scenes that make up a
 * zone is a self-contained set of scenes, connected with one another (by
 * portals) but not to any scenes outside the group.
 */
public class SceneSummary implements Streamable
{
    /** The id of this scene. */
    public int sceneId;

    /** The name of this scene. */
    public String name;

    /** The ids of the scenes to which this scene is connected via
     * portals. */
    public int[] neighbors;

    /** The compass directions in which each of the neighbors lay. The
     * direction constants are as defined in {@link DirectionCodes}. */
    public int[] neighborDirs;

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[sceneId=" + sceneId + ", name=" + name +
            ", neighbors=" + StringUtil.toString(neighbors) +
            ", neighborDirs=" + DirectionUtil.toString(neighborDirs) + "]";
    }
}
