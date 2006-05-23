//
// $Id: YoSceneUtil.java 19769 2005-03-17 07:38:31Z mdb $

package com.threerings.stage.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;

import com.samskivert.util.SortableArrayList;
import com.samskivert.util.StringUtil;
import com.threerings.util.DirectionCodes;
import com.threerings.util.DirectionUtil;

import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileUtil;
import com.threerings.media.tile.TrimmedObjectTileSet;
import com.threerings.media.util.AStarPathUtil;
import com.threerings.media.util.MathUtil;

import com.threerings.miso.MisoConfig;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.tile.BaseTileSet;
import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.miso.util.MisoUtil;
import com.threerings.miso.util.ObjectSet;

import com.threerings.whirled.spot.data.Cluster;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.SceneLocation;

import com.threerings.stage.Log;
import com.threerings.stage.data.StageLocation;
import com.threerings.stage.data.StageMisoSceneModel;
import com.threerings.stage.data.StageSceneModel;

/**
 * Provides scene related utility functions.
 */
public class StageSceneUtil
{
    /**
     * Returns the scene metrics we use to do our calculations.
     */
    public static MisoSceneMetrics getMetrics ()
    {
        return _metrics;
    }

    /**
     * Does the necessary jiggery pokery to figure out where the specified
     * object's associated location is.
     */
    public static StageLocation locationForObject (
        TileManager tilemgr, ObjectInfo info)
    {
        return locationForObject(tilemgr, info.tileId, info.x, info.y);
    }

    /**
     * Does the necessary jiggery pokery to figure out where the specified
     * object's associated location is.
     *
     * @param tilemgr a tile manager that can be used to look up the tile
     * information.
     * @param tileId the fully qualified tile id of the object tile.
     * @param tx the object's x tile coordinate.
     * @param ty the object's y tile coordinate.
     */
    public static StageLocation locationForObject (
        TileManager tilemgr, int tileId, int tx, int ty)
    {
        try {
            int tsid = TileUtil.getTileSetId(tileId);
            int tidx = TileUtil.getTileIndex(tileId);
            TrimmedObjectTileSet tset = (TrimmedObjectTileSet)
                tilemgr.getTileSet(tsid);
            if (tset == null || tset.getSpotOrient(tidx) < 0) {
                return null;
            }

            Point opos = MisoUtil.tilePlusFineToFull(
                _metrics, tx, ty, tset.getXSpot(tidx), tset.getYSpot(tidx),
                new Point());

//             Log.info("Computed location [set=" + tset.getName() +
//                      ", tidx=" + tidx + ", tx=" + tx + ", ty=" + ty +
//                      ", sx=" + tset.getXSpot(tidx) +
//                      ", sy=" + tset.getYSpot(tidx) +
//                      ", lx=" + opos.x + ", ly=" + opos.y +
//                      ", fg=" + _metrics.finegran + "].");
            return new StageLocation(opos.x, opos.y,
                    (byte)tset.getSpotOrient(tidx));

        } catch (Exception e) {
            Log.warning("Unable to look up object tile for scene object " +
                        "[tileId=" + tileId + ", error=" + e + "].");
        }
        return null;
    }

    /**
     * Converts full coordinates to Cartesian coordinates.
     */
    public static void locationToCoords (int lx, int ly, Point coords)
    {
        int tx = MisoUtil.fullToTile(lx), fx = MisoUtil.fullToFine(lx);
        int ty = MisoUtil.fullToTile(ly), fy = MisoUtil.fullToFine(ly);
        coords.x = tx*_metrics.finegran+fx;
        coords.y = ty*_metrics.finegran+fy;
    }

    /**
     * Converts Cartesian coordinates back to full coordinates.
     */
    public static void coordsToLocation (int cx, int cy, Point loc)
    {
        loc.x = MisoUtil.toFull(cx/_metrics.finegran, cx%_metrics.finegran);
        loc.y = MisoUtil.toFull(cy/_metrics.finegran, cy%_metrics.finegran);
    }

    /**
     * Returns the footprint, in absolute tile coordinates, for the
     * specified object with origin as specified.
     */
    public static Rectangle getObjectFootprint (
        TileManager tilemgr, int tileId, int ox, int oy)
    {
        Rectangle foot = new Rectangle();
        getObjectFootprint(tilemgr, tileId, ox, oy, foot);
        return foot;
    }

    /**
     * Fills in the footprint, in absolute tile coordinates, for the
     * specified object with origin as specified.
     *
     * @return true if the object was successfully looked up and the
     * footprint filled in, false if an error occurred trying to look up
     * the associated object tile.
     */
    public static boolean getObjectFootprint (
        TileManager tilemgr, int tileId, int ox, int oy, Rectangle foot)
    {
        try {
            int tsid = TileUtil.getTileSetId(tileId);
            int tidx = TileUtil.getTileIndex(tileId);
            TrimmedObjectTileSet tset = (TrimmedObjectTileSet)
                tilemgr.getTileSet(tsid);
            if (tset == null) {
                return false;
            }

            int bwidth = tset.getBaseWidth(tidx);
            int bheight = tset.getBaseHeight(tidx);
            foot.setBounds(ox - bwidth + 1, oy - bheight + 1, bwidth, bheight);
            return true;

        } catch (Exception e) {
            Log.warning("Unable to look up object tile for scene object " +
                        "[tileId=" + tileId + ", error=" + e + "].");
            return false;
        }
    }

    /**
     * Looks up the base tile set for the specified fully qualified tile
     * identifier and returns true if the associated tile is passable.
     */
    public static boolean isPassable (TileManager tilemgr, int tileId)
    {
        // non-existent tiles are not passable
        if (tileId <= 0) {
            return false;
        }

        try {
            int tsid = TileUtil.getTileSetId(tileId);
            int tidx = TileUtil.getTileIndex(tileId);
            BaseTileSet tset = (BaseTileSet)tilemgr.getTileSet(tsid);
            return tset.getPassability()[tidx];

        } catch (Exception e) {
            Log.warning("Unable to look up base tile [tileId=" + tileId +
                        ", error=" + e + "].");
            return true;
        }
    }

    /**
     * Computes a list of the valid locations in this cluster.
     */
    public static ArrayList<SceneLocation> getClusterLocs (Cluster cluster)
    {
        ArrayList<SceneLocation> list = new ArrayList<SceneLocation>();

        // convert our tile coordinates into a cartesian coordinate system
        // with units equal to one fine coordinate in size
        int fx = cluster.x*_metrics.finegran+1,
            fy = cluster.y*_metrics.finegran+1;
        int fwid = cluster.width*_metrics.finegran-2,
            fhei = cluster.height*_metrics.finegran-2;
        int cx = fx + fwid/2, cy = fy + fhei/2;

        // if it's a 1x1 cluster, return one location in the center of the
        // cluster
        if (cluster.width == 1) {
            StageLocation loc = new StageLocation(
                MisoUtil.toFull(cluster.x, 2), MisoUtil.toFull(cluster.y, 2),
                (byte)DirectionCodes.SOUTHWEST);
            list.add(new SceneLocation(loc, 0));
            return list;
        }

        double radius = (double)fwid/2;
        int clidx = cluster.width-2;
        if (clidx >= CLUSTER_METRICS.length/2 || clidx < 0) {
            Log.warning("Requested locs from invalid cluster " + cluster + ".");
            Thread.dumpStack();
            return list;
        }

        for (double angle = CLUSTER_METRICS[clidx*2]; angle < Math.PI*2;
             angle += CLUSTER_METRICS[clidx*2+1]) {
            int sx = cx + (int)Math.round(Math.cos(angle) * radius);
            int sy = cy + (int)Math.round(Math.sin(angle) * radius);

            // obtain the orientation facing toward the center
            int orient = 2*(int)(Math.round(angle/(Math.PI/4))%8);
            orient = DirectionUtil.rotateCW(DirectionCodes.SOUTH, orient);
            orient = DirectionUtil.getOpposite(orient);

            // convert them back to full coordinates for the location
            int tx = MathUtil.floorDiv(sx, _metrics.finegran);
            sx = MisoUtil.toFull(tx, sx-(tx*_metrics.finegran));
            int ty = MathUtil.floorDiv(sy, _metrics.finegran);
            sy = MisoUtil.toFull(ty, sy-(ty*_metrics.finegran));
            StageLocation loc = new StageLocation(sx, sy, (byte) orient);
            list.add(new SceneLocation(loc, 0));
        }

        return list;
    }

//     /**
//      * Returns true if this user is available to be clustered with.
//      */
//     public static boolean isClusterable (YoOccupantInfo info)
//     {
//         switch (info.activity) {
//         case ActivityCodes.NONE:
//         case ActivityCodes.READING:
//         case ActivityCodes.IDLE:
//         case ActivityCodes.DISCONNECTED:
//             return true;
//         default:
//             return false;
//         }
//     }

    /**
     * Locates a spot to stand near the supplied rectangular footprint.
     * First a spot will be sought in a tile immediately next to the
     * footprint, then one tile removed, then two, up to the maximum
     * distance specified by <code>dist</code>.
     *
     * @param foot the tile coordinate footprint around which we are
     * attempting to stand.
     * @param dist the maximum number of tiles away from the footprint to
     * search before giving up.
     * @param pred a predicate that will be used to determine whether a
     * particular spot can be stood upon (we're hijacking the meaning of
     * "traverse" in this case, but the interface is otherwise so nice).
     * @param traverser the object that will be passed to the traversal
     * predicate.
     * @param nearto a point (in tile coordinates) which will be used to
     * select from among the valid standing spots, the one nearest to the
     * supplied point will be returned.
     * @param orient if not {@link DirectionCodes#NONE} this orientation
     * will be used to override the "natural" orientation of the spot
     * which is facing toward the footprint.
     *
     * @return the closest spot to the 
     */
    public static StageLocation findStandingSpot (
        Rectangle foot, int dist, AStarPathUtil.TraversalPred pred,
        Object traverser, final Point nearto, int orient)
    {
        // generate a list of the tile coordinates of all squares around
        // this footprint
        SortableArrayList spots = new SortableArrayList();

        for (int dd = 1; dd <= dist; dd++) {
            int yy1 = foot.y-dd, yy2 = foot.y+foot.height+dd-1;
            int xx1 = foot.x-dd, xx2 = foot.x+foot.width+dd-1;

            // get the corners
            spots.add(
                new StageLocation(xx1, yy1, (byte)DirectionCodes.SOUTHWEST));
            spots.add(
                new StageLocation(xx1, yy2, (byte)DirectionCodes.SOUTHEAST));
            spots.add(
                new StageLocation(xx2, yy1, (byte)DirectionCodes.NORTHWEST));
            spots.add(
                new StageLocation(xx2, yy2, (byte)DirectionCodes.NORTHEAST));

            // then the sides
            for (int xx = xx1+1; xx < xx2; xx++) {
                spots.add(
                    new StageLocation(xx, yy1, (byte)DirectionCodes.WEST));
                spots.add(
                    new StageLocation(xx, yy2, (byte)DirectionCodes.EAST));
            }
            for (int yy = yy1+1; yy < yy2; yy++) {
                spots.add(
                    new StageLocation(xx1, yy, (byte)DirectionCodes.SOUTH));
                spots.add(
                    new StageLocation(xx2, yy, (byte)DirectionCodes.NORTH));
            }

            // sort them in order of closeness to the players current
            // coordinate
            spots.sort(new Comparator() {
                public int compare (Object o1, Object o2) {
                    return dist((StageLocation)o1) - dist((StageLocation)o2);
                }
                private final int dist (StageLocation l) {
                    return Math.round(100*MathUtil.distance(
                                          l.x, l.y, nearto.x, nearto.y));
                }
            });

            // return the first spot that can be "traversed" which we're
            // taking to mean "stood upon"
            for (int ii = 0, ll = spots.size(); ii < ll; ii++) {
                StageLocation loc = (StageLocation)spots.get(ii);
                if (pred.canTraverse(traverser, loc.x, loc.y)) {
                    // convert to full coordinates
                    loc.x = MisoUtil.toFull(loc.x, 2);
                    loc.y = MisoUtil.toFull(loc.y, 2);

                    // see if we need to override the orientation
                    if (DirectionCodes.NONE != orient) {
                        loc.orient = (byte) orient;
                    }
                    return loc;
                }
            }

            // clear this list and try one further out
            spots.clear();
        }

        return null;
    }

    /**
     * Returns an array of the objects intersected by the supplied tile
     * coordinate rectangle.
     */
    public static ObjectInfo[] getIntersectedObjects (
        TileManager tmgr, StageSceneModel model, Rectangle rect)
    {
        // first get all objects whose origin is in an expanded version of
        // our intersection rect, any object that is *so* large that its
        // origin falls outside of this rectangle but that still
        // intersects this rectangle can go to hell; it's either this or
        // we iterate over every object in the whole goddamned scene which
        // is so hairily inefficient i can't even bear to contemplate it
        ObjectSet objs = new ObjectSet();
        Rectangle orect = new Rectangle(rect);
        orect.grow(MAX_OBJECT_SIZE, MAX_OBJECT_SIZE);
        StageMisoSceneModel mmodel = StageMisoSceneModel.getSceneModel(model);
        mmodel.getObjects(orect, objs);

        // now prune from this set any and all objects that don't actually
        // overlap the specified rectangle
        Rectangle foot = new Rectangle();
        for (int ii = 0; ii < objs.size(); ii++) {
            ObjectInfo info = objs.get(ii);
            if (getObjectFootprint(tmgr, info.tileId, info.x, info.y, foot)) {
                if (!foot.intersects(rect)) {
                    objs.remove(ii--);
                }
            } else {
                Log.warning("Unknown potentially intersecting object?! " +
                            "[scene=" + model.name + " (" + model.sceneId +
                            "), info=" + info + "].");
            }
        }

        return objs.toArray();
    }

    /** Our default scene metrics. */
    protected static MisoSceneMetrics _metrics = MisoConfig.getSceneMetrics();

    /** Contains the starting offset from zero radians for the first
     * occupant and the radial distance between occupants. */
    protected static final double[] CLUSTER_METRICS = {
        Math.PI/4,  Math.PI/2, // 2x
        Math.PI/4,  Math.PI/2, // 3x
        0,          Math.PI/4, // 4x
        Math.PI/12, Math.PI/6, // 5x
        0,          Math.PI/8, // 6x
        Math.PI/24, Math.PI/12, // 7x
    };

    /** The maximum footprint width or height for which we will account in
     * {@link #getIntersectedObjects}. */
    protected static final int MAX_OBJECT_SIZE = 15;
}
