//
// $Id: ZoneSummary.java,v 1.3 2001/12/17 00:58:04 mdb Exp $

package com.threerings.whirled.zone.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.samskivert.util.StringUtil;
import com.threerings.presents.io.Streamable;

/**
 * The zone summary contains information on a zone, including its name and
 * summary info on all of the scenes in this zone (which can be used to
 * generate a map of the zone on the client).
 */
public class ZoneSummary implements Streamable
{
    /** The zone's fully qualified unique identifier. */
    public int zoneId;

    /** The name of the zone. */
    public String name;

    /** The summary information for all of the scenes in the zone. */
    public SceneSummary[] scenes;

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeInt(zoneId);
        out.writeUTF(name);
        int scount = scenes.length;
        out.writeInt(scount);
        for (int i = 0; i < scount; i++) {
            scenes[i].writeTo(out);
        }
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        zoneId = in.readInt();
        name = in.readUTF();
        int scount = in.readInt();
        scenes = new SceneSummary[scount];
        for (int i = 0; i < scount; i++) {
            scenes[i] = new SceneSummary();
            scenes[i].readFrom(in);
        }
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[zoneId=" + zoneId + ", name=" + name +
            ", scenes=" + StringUtil.toString(scenes) + "]";
    }
}
