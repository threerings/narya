//
// $Id: SceneModel.java,v 1.1 2001/11/12 20:56:55 mdb Exp $

package com.threerings.whirled.data;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.io.Streamable;

/**
 * The scene model is the bare bones representation of the data for a
 * scene in the Whirled system. From the scene model, one would create an
 * instance of {@link com.threerings.whirled.server.RuntimeScene}, {@link
 * com.threerings.whirled.client.DisplayScene} or {@link
 * com.threerings.whirled.tools.EditableScene}.
 *
 * <p> The scene model is what is loaded from the scene repositories and
 * what is transmitted over the wire when communicating scenes from the
 * server to the client.
 */
public class SceneModel implements Streamable
{
    /** This scene's unique identifier. */
    public int sceneId;

    /** The version number of this scene. Versions are incremented
     * whenever modifications are made to a scene so that clients can
     * determine whether or not they have the latest version of a
     * scene. */
    public int version;

    /** The scene ids of the scenes that neighbor this scene. A neighbor
     * is a scene that can be entered from this scene. */
    public int[] neighborIds;

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeInt(sceneId);
        out.writeInt(version);
        int nlength = neighborIds.length;
        out.writeInt(nlength);
        for (int i = 0; i < nlength; i++) {
            out.writeInt(neighborIds[i]);
        }
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        sceneId = in.readInt();
        version = in.readInt();
        int nlength = in.readInt();
        neighborIds = new int[nlength];
        for (int i = 0; i < nlength; i++) {
            neighborIds[i] = in.readInt();
        }
    }
}
