//
// $Id: SpotSceneModel.java,v 1.7 2002/05/02 19:34:51 mdb Exp $

package com.threerings.whirled.spot.data;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.samskivert.util.StringUtil;
import com.threerings.whirled.data.SceneModel;

/**
 * The spot scene model extends the standard scene model with information
 * on locations, clusters and portals. Locations (and by extension,
 * portals) are referenced by an identifier, unique within the scene and
 * unchanging, so that portals can stably reference the target location in
 * the scene to which they connect. Clusters are tracked by index rather
 * than unique identifier, but only exist as an attribute of locations (a
 * location belongs to zero or one clusters).
 */
public class SpotSceneModel extends SceneModel
{
    /** The unique (within the scope of this scene) identifier of each
     * location in this scene (including portals). */
    public int[] locationIds;

    /** The x coordinates of the locations in this scene (including
     * portals). */
    public int[] locationX;

    /** The y coordinates of the locations in this scene (including
     * portals). */
    public int[] locationY;

    /** The orientations associated with the locations in this scene
     * (including portals). */
    public int[] locationOrients;

    /** The cluster index of each location in this scene, which can be 0
     * to indicate that the location is not a member of any cluster
     * (including portals, which should always be 0). */
    public int[] locationClusters;

    /** The location id of the default entrance to this scene. If a body
     * enters the scene without coming from another scene, this is the
     * location at which they would appear. */
    public int defaultEntranceId;

    /** The location id of each portal in this scene. These must map, in
     * order, to the neighboring scene ids specified in {@link
     * #neighborIds}. The neighbor ids being the scene ids of the scenes
     * to which these portals take a body when "used". Additionally, these
     * must occur in the same order that the location ids appear in the
     * above array. */
    public int[] portalIds;

    /** Contains the location ids of the entry location in the target
     * scene to which this scene's portals connect. Portals in this scene
     * connect to locations in other scenes as dictated by these
     * values. */
    public int[] targetLocIds;

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);

        // write out our location info
        int lcount = locationIds.length;
        out.writeInt(lcount);
        for (int i = 0; i < lcount; i++) {
            out.writeInt(locationIds[i]);
            out.writeInt(locationX[i]);
            out.writeInt(locationY[i]);
            out.writeInt(locationOrients[i]);
            out.writeInt(locationClusters[i]);
        }

        // write out our default entrance id
        out.writeInt(defaultEntranceId);

        // write out our portal info; we need not serialize the portal
        // count because our based class already did when writing out the
        // neighborIds array
        int pcount = portalIds.length;
        for (int i = 0; i < pcount; i++) {
            out.writeInt(portalIds[i]);
            out.writeInt(targetLocIds[i]);
        }
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);

        // read in our location info
        int lcount = in.readInt();
        locationIds = new int[lcount];
        locationX = new int[lcount];
        locationY = new int[lcount];
        locationOrients = new int[lcount];
        locationClusters = new int[lcount];

        for (int i = 0; i < lcount;i++) {
            locationIds[i] = in.readInt();
            locationX[i] = in.readInt();
            locationY[i] = in.readInt();
            locationOrients[i] = in.readInt();
            locationClusters[i] = in.readInt();
        }

        // read in our default entrance id
        defaultEntranceId = in.readInt();

        // and read in our portal info
        int pcount = neighborIds.length;
        portalIds = new int[pcount];
        targetLocIds = new int[pcount];

        for (int i = 0; i < pcount; i++) {
            portalIds[i] = in.readInt();
            targetLocIds[i] = in.readInt();
        }
    }

    // documentation inherited
    public Object clone ()
        throws CloneNotSupportedException
    {
        SpotSceneModel model = (SpotSceneModel)super.clone();
        model.locationIds = (int[])locationIds.clone();
        model.locationX = (int[])locationX.clone();
        model.locationY = (int[])locationY.clone();
        model.locationOrients = (int[])locationOrients.clone();
        model.locationClusters = (int[])locationClusters.clone();
        model.portalIds = (int[])portalIds.clone();
        model.targetLocIds = (int[])targetLocIds.clone();
        return model;
    }

    /**
     * Creates and returns a blank scene model.
     */
    public static SpotSceneModel blankSpotSceneModel ()
    {
        SpotSceneModel model = new SpotSceneModel();
        populateBlankSpotSceneModel(model);
        return model;
    }

    /**
     * Populates a blank scene model with blank values.
     */
    protected static void populateBlankSpotSceneModel (SpotSceneModel model)
    {
        // populate our superclass fields
        populateBlankSceneModel(model);

        // now populate our fields
        model.locationIds = new int[0];
        model.locationX = new int[0];
        model.locationY = new int[0];
        model.locationOrients = new int[0];
        model.locationClusters = new int[0];
        model.defaultEntranceId = -1;
        model.portalIds = new int[0];
        model.targetLocIds = new int[0];
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", locationIds=").append(StringUtil.toString(locationIds));
        buf.append(", locationX=").append(StringUtil.toString(locationX  ));
        buf.append(", locationY=").append(StringUtil.toString(locationY));
        buf.append(", locationOrients=").
            append(StringUtil.toString(locationOrients));
        buf.append(", locationClusters=").
            append(StringUtil.toString(locationClusters));
        buf.append(", defaultEntranceId=").append(defaultEntranceId);
        buf.append(", portalIds=").append(StringUtil.toString(portalIds));
        buf.append(", targetLocIds=").append(StringUtil.toString(targetLocIds));
    }
}
