//
// $Id: YoSceneUtil.java 19769 2005-03-17 07:38:31Z mdb $

package com.threerings.stage.util;

import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

import com.threerings.util.DirectionCodes;
import com.threerings.util.DirectionUtil;
import com.threerings.util.MessageBundle;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.ObjectTileSet;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.data.ObjectInfo;

import com.threerings.stage.Log;
import com.threerings.stage.data.StageCodes;
import com.threerings.stage.data.StageMisoSceneModel;
import com.threerings.stage.data.StageScene;

/**
 * Maintains extra information on objects in a scene and checks proposed
 * placement operations for constraint violations.  When the constraints
 * object is in use, all placement operations (object additions and removals)
 * must go through the constraints object so that the object's internal state
 * remains consistent.
 */
public class PlacementConstraints
    implements DirectionCodes, StageCodes
{
    /**
     * Default constructor.
     */
    public PlacementConstraints (TileManager tilemgr, StageScene scene)
    {
        _tilemgr = tilemgr;
        _scene = scene;
        _mmodel = StageMisoSceneModel.getSceneModel(scene.getSceneModel());
        
        // add all the objects in the scene
        StageMisoSceneModel.ObjectVisitor visitor =
            new StageMisoSceneModel.ObjectVisitor() {
            public void visit (ObjectInfo info) {
                ObjectData data = createObjectData(info);
                if (data != null) {
                    // clone the map key, as the visit method reuses a
                    // single ObjectInfo instance for uninteresting objects
                    // in a section
                    _objectData.put((ObjectInfo)info.clone(), data);
                }
            }
        };
        _mmodel.visitObjects(visitor);
    }
    
    /**
     * Determines whether the constraints allow the specified object to be
     * added to the scene.
     *
     * @return <code>null</code> if the constraints allow the operation,
     * otherwise a translatable string explaining why the object can't be
     * added
     */
    public String allowAddObject (ObjectInfo info)
    {
        return allowModifyObjects(new ObjectInfo[] { info },
            new ObjectInfo[0]);
    }
    
    /**
     * Adds the specified object through the constraints.
     */
    public void addObject (ObjectInfo info)
    {
        ObjectData data = createObjectData(info);
        if (data != null) {
            _scene.addObject(info);
            _objectData.put(info, data);
        }
    }
    
    /**
     * Determines whether the constraints allow the specified object to be
     * removed from the scene.
     *
     * @return <code>null</code> if the constraints allow the operation,
     * otherwise a translatable string explaining why the object can't be
     * removed
     */
    public String allowRemoveObject (ObjectInfo info)
    {
        return allowModifyObjects(new ObjectInfo[0],
            new ObjectInfo[] { info });
    }
    
    /**
     * Removes the specified object through the constraints.
     */
    public void removeObject (ObjectInfo info)
    {
        _scene.removeObject(info);
        _objectData.remove(info);
    }
    
    /**
     * Determines whether the constraints allow the specified objects to be
     * added and removed simultaneously.
     *
     * @return <code>null</code> if the constraints allow the operation,
     * otherwise a translatable string explaining why the objects can't be
     * modified
     */
    public String allowModifyObjects (ObjectInfo[] added,
        ObjectInfo[] removed)
    {
        ObjectData[] addedData = new ObjectData[added.length];
        for (int i = 0; i < added.length; i++) {
            addedData[i] = createObjectData(added[i]);
            if (addedData[i] == null) {
                return INTERNAL_ERROR;
            }
        }
        
        ObjectData[] removedData = getObjectDataFromInfo(removed);
        if (removedData == null) {
            return INTERNAL_ERROR;
        }
        
        return allowModifyObjects(addedData, removedData);
    }

    /**
     * Returns an ObjectData array that corresponds to the supplied
     * ObjectInfo array.  Returns null on error.
     */
    protected ObjectData[] getObjectDataFromInfo (ObjectInfo[] info)
    {
        if (info == null) {
            return null;
        }
        ObjectData[] data = new ObjectData[info.length];
        for (int ii = 0; ii < info.length; ii++) {
            data[ii] = (ObjectData)_objectData.get(info[ii]);
            if (data[ii] == null) {
                Log.warning("Couldn't match object info up to data [info=" +
                        info[ii] + "].");
                return null;
            }
        }
        return data;
    }
    
    /**
     * Determines whether the constraints allow the specified objects to be
     * added and removed simultaneously.  Subclasses that wish to define
     * additional constraints should override this method.
     *
     * @return <code>null</code> if the constraints allow the operation,
     * otherwise a qualified translatable string explaining why the objects
     * can't be modified
     */
    protected String allowModifyObjects (ObjectData[] added,
        ObjectData[] removed)
    {
        DirectionHeight dirheight = new DirectionHeight();
        
        for (int i = 0; i < added.length; i++) {
            if (added[i].tile.hasConstraint(ObjectTileSet.ON_SURFACE) &&
                !isOnSurface(added[i], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.not_on_surface");
            }
            
            int dir = getConstraintDirection(added[i], ObjectTileSet.ON_WALL);
            if (dir != NONE && !isOnWall(added[i], added, removed, dir)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.not_on_wall");
            }
            
            if (getConstraintDirectionHeight(added[i], ObjectTileSet.ATTACH,
                    dirheight) && !isAttached(added[i], added, removed,
                        dirheight.dir, dirheight.low)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.not_attached");
            }
            
            dir = getConstraintDirection(added[i], ObjectTileSet.SPACE);
            if (dir != NONE && !hasSpace(added[i], added, removed, dir)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.no_space");
            }
            
            if (hasSpaceConstrainedAdjacent(added[i], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.no_space_adj");
            }
        }
        
        for (int i = 0; i < removed.length; i++) {
            if (removed[i].tile.hasConstraint(ObjectTileSet.SURFACE) &&
                hasOnSurface(removed[i], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.has_on_surface");
            }
            
            int dir = getConstraintDirection(removed[i], ObjectTileSet.WALL);
            if (dir != NONE) {
                if (hasOnWall(removed[i], added, removed, dir)) {
                    return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                        "m.has_on_wall");
                
                } else if (hasAttached(removed[i], added, removed, dir)) {
                    return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                        "m.has_attached");
                }
            }
        }
        
        return null;
    }
    
    /**
     * Determines whether the specified surface has anything on it that won't
     * be held up if the surface is removed.
     */
    protected boolean hasOnSurface (ObjectData data, ObjectData[] added,
        ObjectData[] removed)
    {
        ArrayList objects = getObjectData(data.bounds, added, removed);
        for (int i = 0, size = objects.size(); i < size; i++) {
            ObjectData odata = (ObjectData)objects.get(i);
            if (odata.tile.hasConstraint(ObjectTileSet.ON_SURFACE) &&
                !isOnSurface(odata, added, removed)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines whether the specified wall has anything on it that won't be
     * held up if the wall is removed.
     */
    protected boolean hasOnWall (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir)
    {
        ArrayList objects = getObjectData(data.bounds, added, removed);
        for (int i = 0, size = objects.size(); i < size; i++) {
            ObjectData odata = (ObjectData)objects.get(i);
            if (getConstraintDirection(odata, ObjectTileSet.ON_WALL) == dir &&
                !isOnWall(odata, added, removed, dir)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines whether the specified wall has anything attached to it that
     * won't be held up if the wall is removed.
     */
    protected boolean hasAttached (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir)
    {
        DirectionHeight dirheight = new DirectionHeight();
        
        ArrayList objects = getObjectData(getAdjacentEdge(data.bounds,
            DirectionUtil.getOpposite(dir)), added, removed);
        for (int i = 0, size = objects.size(); i < size; i++) {
            ObjectData odata = (ObjectData)objects.get(i);
            if (getConstraintDirectionHeight(odata, ObjectTileSet.ATTACH,
                    dirheight) && !isAttached(odata, added, removed,
                        dirheight.dir, dirheight.low)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifies that the objects adjacent to the given object will still have
     * their space constraints met if the object is added.
     */
    protected boolean hasSpaceConstrainedAdjacent (ObjectData data,
        ObjectData[] added, ObjectData[] removed)
    {
        Rectangle r = data.bounds;
        // grow the ObjectData bounds 1 square in each direction
        _constrainRect.setBounds(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
        
        ArrayList objects = getObjectData(_constrainRect, added, removed);
        for (int i = 0, size = objects.size(); i < size; i++) {
            ObjectData odata = (ObjectData)objects.get(i);
            int dir = getConstraintDirection(odata, ObjectTileSet.SPACE);
            if (dir != NONE && !hasSpace(odata, added, removed, dir)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines whether the specified object has empty space in the specified
     * direction.
     */
    protected boolean hasSpace (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir)
    {
        return getObjectData(getAdjacentEdge(data.bounds, dir), added,
            removed).size() == 0;
    }
    
    /**
     * Determines whether the specified object is on a surface.
     */
    protected boolean isOnSurface (ObjectData data, ObjectData[] added,
        ObjectData[] removed)
    {
        return isCovered(data.bounds, added, removed, ObjectTileSet.SURFACE,
            null);
    }
    
    /**
     * Determines whether the specified object is on a wall facing the
     * specified direction.
     */
    protected boolean isOnWall (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir)
    {
        return isCovered(data.bounds, added, removed,
            getDirectionalConstraint(ObjectTileSet.WALL, dir), null);
    }
    
    /**
     * Determines whether the specified object is attached to another object in
     * the specified direction and at the specified height.
     */
    protected boolean isAttached (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir, boolean low)
    {
        return isCovered(getAdjacentEdge(data.bounds, dir), added, removed,
            getDirectionalConstraint(ObjectTileSet.WALL, dir), low ?
            getDirectionalConstraint(ObjectTileSet.WALL, dir, true) : null);
    }
    
    /**
     * Given a rectangle, determines whether all of the tiles within
     * the rectangle intersect an object.  If the constraint parameter is
     * non-null, the intersected objects must have that constraint (or the
     * alternate constraint, if specified).
     */
    protected boolean isCovered (Rectangle rect, ObjectData[] added,
        ObjectData[] removed, String constraint, String altstraint)
    {
        ArrayList objects = getObjectData(rect, added, removed);
        for (int y = rect.y, ymax = rect.y + rect.height; y < ymax; y++) {
            for (int x = rect.x, xmax = rect.x + rect.width; x < xmax; x++) {
                boolean covered = false;
                for (int i = 0, size = objects.size(); i < size; i++) {
                    ObjectData data = (ObjectData)objects.get(i);
                    if (data.bounds.contains(x, y) && (constraint == null ||
                            data.tile.hasConstraint(constraint) ||
                            (altstraint != null &&
                                data.tile.hasConstraint(altstraint)))) {
                        covered = true;
                        break;
                    }
                }
                if (!covered) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Creates and returns a rectangle that covers the specified rectangle's
     * adjacent edge (the squares one tile beyond the bounds) in the specified
     * direction.
     */
    protected Rectangle getAdjacentEdge (Rectangle rect, int dir)
    {
        switch (dir) {
            case NORTH:
                return new Rectangle(rect.x - 1, rect.y, 1, rect.height);
                
            case EAST:
                return new Rectangle(rect.x, rect.y - 1, rect.width, 1);
                
            case SOUTH:
                return new Rectangle(rect.x + rect.width, rect.y, 1,
                    rect.height);
  
            case WEST:
                return new Rectangle(rect.x, rect.y + rect.height,
                    rect.width, 1);
                
            default:
                return null;
        }
    }
    
    /**
     * Returns the direction in which the specified object is constrained by
     * appending "[NESW]" to the given constraint prefix.  Returns
     * <code>NONE</code> if there is no such directional constraint.
     */
    protected int getConstraintDirection (ObjectData data, String prefix)
    {
        DirectionHeight dirheight = new DirectionHeight();
        return getConstraintDirectionHeight(data, prefix, dirheight) ?
            dirheight.dir : NONE;
    }
    
    /**
     * Populates the supplied {@link DirectionHeight} object with the direction
     * and height of the constraint identified by the given prefix.
     *
     * @return true if the object was successfully populated, false if there is
     * no such constraint
     */
    protected boolean getConstraintDirectionHeight (ObjectData data,
        String prefix, DirectionHeight dirheight)
    {
        String[] constraints = data.tile.getConstraints();
        if (constraints == null) {
            return false;
        }
        
        for (int i = 0; i < constraints.length; i++) {
            if (constraints[i].startsWith(prefix)) {
                int fromidx = prefix.length(),
                    toidx = constraints[i].indexOf('_', fromidx);
                dirheight.dir = DirectionUtil.fromShortString(toidx == -1 ?
                    constraints[i].substring(fromidx) :
                    constraints[i].substring(fromidx, toidx));
                dirheight.low = constraints[i].endsWith(ObjectTileSet.LOW);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Given a constraint prefix and a direction, returns the directional
     * constraint.
     */
    protected String getDirectionalConstraint (String prefix, int dir)
    {
        return getDirectionalConstraint(prefix, dir, false);
    }
    
    /**
     * Given a constraint prefix, direction, and height, returns the
     * directional constraint.
     */
    protected String getDirectionalConstraint (String prefix, int dir,
        boolean low)
    {
        return prefix + DirectionUtil.toShortString(dir) +
            (low ? ObjectTileSet.LOW : "");
    }
    
    /**
     * Finds all objects whose bounds intersect the given rectangle and
     * returns a list containing their {@link ObjectData} elements.
     *
     * @param added an array of objects to add to the search
     * @param removed an array of objects to exclude from the search
     */
    protected ArrayList getObjectData (Rectangle rect, ObjectData[] added,
        ObjectData[] removed)
    {
        ArrayList list = new ArrayList();
        
        for (Iterator it = _objectData.values().iterator(); it.hasNext(); ) {
            ObjectData data = (ObjectData)it.next();
            if (rect.intersects(data.bounds) && !ListUtil.contains(removed,
                    data)) {
                list.add(data);
            }
        }
        
        for (int i = 0; i < added.length; i++) {
            if (rect.intersects(added[i].bounds)) {
                list.add(added[i]);
            }
        }
        
        return list;
    }
    
    /**
     * Using the tile manager, computes and returns the specified object's
     * data.
     */
    protected ObjectData createObjectData (ObjectInfo info)
    {
        try {
            ObjectTile tile = (ObjectTile)_tilemgr.getTile(info.tileId);
            Rectangle bounds = new Rectangle(info.x, info.y, tile.getBaseWidth(),
                tile.getBaseHeight());
            bounds.translate(1 - bounds.width, 1 - bounds.height);
            return new ObjectData(bounds, tile);
            
        } catch (Exception e) {
            Log.warning("Error retrieving tile for object [info=" +
                info + ", error=" + e + "].");
            Log.logStackTrace(e);
            return null;
        }
    }
    
    /** Contains information about an object used in checking constraints. */
    protected class ObjectData
    {
        public Rectangle bounds;
        public ObjectTile tile;
        
        public ObjectData (Rectangle bounds, ObjectTile tile)
        {
            this.bounds = bounds;
            this.tile = tile;
        }
    }
    
    /** Contains the direction and height of a constraint. */
    protected class DirectionHeight
    {
        public int dir;
        public boolean low;
    }
    
    /** The tile manager to use for object dimensions and constraints. */
    protected TileManager _tilemgr;
    
    /** The scene being checked for constraints. */
    protected StageScene _scene;
    
    /** The Miso scene model. */
    protected StageMisoSceneModel _mmodel;
    
    /** For all objects in the scene, maps {@link ObjectInfo}s to
     * {@link ObjectData}s. */
    protected HashMap _objectData = new HashMap();

    /** One rectangle we'll re-use for all constraints ops. */
    protected static final Rectangle _constrainRect = new Rectangle();
}
