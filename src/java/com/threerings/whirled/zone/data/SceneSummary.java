//
// $Id: SceneSummary.java,v 1.2 2001/12/17 00:58:04 mdb Exp $

package com.threerings.whirled.zone.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.samskivert.util.StringUtil;
import com.threerings.presents.io.Streamable;
import com.threerings.presents.io.StreamableUtil;

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

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeInt(sceneId);
        out.writeUTF(name);
        StreamableUtil.writeInts(out, neighbors);
        out.writeInt(_population);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        sceneId = in.readInt();
        name = in.readUTF();
        neighbors = StreamableUtil.readInts(in);
        _population = in.readInt();
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[sceneId=" + sceneId + ", name=" + name +
            ", neighbors=" + StringUtil.toString(neighbors) + "]";
    }

    /** The number of people currently occupying this scene. */
    protected int _population;
}
