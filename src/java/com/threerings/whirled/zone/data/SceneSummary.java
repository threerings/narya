//
// $Id: SceneSummary.java,v 1.5 2002/12/20 23:41:27 mdb Exp $

package com.threerings.whirled.zone.data;

import com.samskivert.util.StringUtil;

import com.threerings.util.DirectionCodes;
import com.threerings.util.DirectionUtil;

import com.threerings.io.Streamable;

/**
 * The scene summary class is used to provide info about the connected
 * group of scenes that make up an island. The group of scenes that make
 * up an island is a self-contained set of scenes, connected with one
 * another (by portals) but not to any scenes outside the group.
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
     * Returns the population of this scene summary instance. This is
     * synchronized because the population can be updated by a background
     * thread.
     */
    public synchronized int getPopulation ()
    {
        return _population;
    }

    /**
     * Used to set the population of this scene summary instance.
     */
    public synchronized void setPopulation (int population)
    {
        _population = population;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[sceneId=" + sceneId + ", name=" + name +
            ", neighbors=" + StringUtil.toString(neighbors) +
            ", neighborDirs=" + DirectionUtil.toString(neighborDirs) +
            ", pop=" + _population + "]";
    }

    /** The number of people currently occupying this scene. */
    protected int _population;
}
