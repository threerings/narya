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
                    _objectData.put(info, data);
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
        }
        
        ObjectData[] removedData = new ObjectData[removed.length];
        for (int i = 0; i < removed.length; i++) {
            removedData[i] = (ObjectData)_objectData.get(removed[i]);
        }
        
        return allowModifyObjects(addedData, removedData);
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
        for (int i = 0; i < added.length; i++) {
            if (added[i].tile.hasConstraint(ObjectTileSet.ON_SURFACE) &&
                !isOnSurface(added[i], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.not_on_surface");
                
            } else if (added[i].tile.hasConstraint(ObjectTileSet.ON_WALL) &&
                !isOnWall(added[i], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.not_on_wall");
            }
            
            int dir = getConstraintDirection(added[i], "ATTACH");
            if (dir != NONE && !isAttached(added[i], added, removed, dir)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.not_attached");
            }
            
            dir = getConstraintDirection(added[i], "SPACE");
            if (dir != NONE && !hasSpace(added[i], added, removed, dir)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.no_space");
            }
            
            if (!checkAdjacentSpace(added[i], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.no_space_adj");
            }
        }
        
        for (int i = 0; i < removed.length; i++) {
            if (removed[i].tile.hasConstraint(ObjectTileSet.SURFACE) &&
                hasOnSurface(removed[i], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.has_on_surface");
            
            } else if (removed[i].tile.hasConstraint(ObjectTileSet.WALL) &&
                hasOnWall(removed[i], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.has_on_wall");
                
            } else if (!checkAdjacentAttached(removed[i], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.has_attached");
            }
        }
        
        return null;
    }
    
    /**
     * Determines whether the specified object is on a surface.
     */
    protected boolean isOnSurface (ObjectData data, ObjectData[] added,
        ObjectData[] removed)
    {
        return isCovered(data.bounds, added, removed, ObjectTileSet.SURFACE);
    }
    
    /**
     * Determines whether the specified object is on a wall.
     */
    protected boolean isOnWall (ObjectData data, ObjectData[] added,
        ObjectData[] removed)
    {
        return isCovered(data.bounds, added, removed, ObjectTileSet.WALL);
    }
    
    /**
     * Verifies that the objects adjacent to the given object will still have
     * their attachment constraints met if the object is removed.
     */
    protected boolean checkAdjacentAttached (ObjectData data,
        ObjectData[] added, ObjectData[] removed)
    {
        Rectangle rect = new Rectangle(data.bounds);
        rect.grow(1, 1);
        ArrayList objects = getObjectData(rect, added, removed);
        
        for (int i = 0, size = objects.size(); i < size; i++) {
            ObjectData odata = (ObjectData)objects.get(i);
            int dir = getConstraintDirection(odata, "ATTACH");
            if (dir != NONE && !isAttached(odata, added, removed, dir)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Determines whether the specified object is attached to another object in
     * the specified direction.
     */
    protected boolean isAttached (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir)
    {
        return isCovered(getAdjacentEdge(data.bounds, dir), added, removed,
            ObjectTileSet.WALL);
    }
    
    /**
     * Given a rectangle, determines whether all of the tiles within
     * the rectangle intersect an object.  If the constraint parameter is
     * non-null, the intersected objects must have that constraint.
     */
    protected boolean isCovered (Rectangle rect, ObjectData[] added,
        ObjectData[] removed, String constraint)
    {
        ArrayList objects = getObjectData(rect, added, removed);
        for (int y = rect.y, ymax = rect.y + rect.height; y < ymax; y++) {
            for (int x = rect.x, xmax = rect.x + rect.width; x < xmax; x++) {
                boolean covered = false;
                for (int i = 0, size = objects.size(); i < size; i++) {
                    ObjectData data = (ObjectData)objects.get(i);
                    if (data.bounds.contains(x, y) && (constraint == null ||
                            data.tile.hasConstraint(constraint))) {
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
     * Verifies that that objects adjacent to the given object will still have
     * their space constraints met if the object is added.
     */
    protected boolean checkAdjacentSpace (ObjectData data, ObjectData[] added,
        ObjectData[] removed)
    {
        Rectangle rect = new Rectangle(data.bounds);
        rect.grow(1, 1);
        ArrayList objects = getObjectData(rect, added, removed);
        
        for (int i = 0, size = objects.size(); i < size; i++) {
            ObjectData odata = (ObjectData)objects.get(i);
            int dir = getConstraintDirection(odata, "SPACE");
            if (dir != NONE && !hasSpace(odata, added, removed, dir)) {
                return false;
            }
        }
        return true;
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
     * Determines whether the specified surface has anything on it.
     */
    protected boolean hasOnSurface (ObjectData data, ObjectData[] added,
        ObjectData[] removed)
    {
        return hasConstrained(data.bounds, added, removed,
            ObjectTileSet.ON_SURFACE);
    }
    
    /**
     * Determines whether the specified wall has anything on it.
     */
    protected boolean hasOnWall (ObjectData data, ObjectData[] added,
        ObjectData[] removed)
    {
        return hasConstrained(data.bounds, added, removed,
            ObjectTileSet.ON_WALL);
    }
    
    /**
     * Determines whether the given rectangle overlaps any objects with the
     * given constraint.
     */
    protected boolean hasConstrained (Rectangle rect, ObjectData[] added,
        ObjectData[] removed, String constraint)
    {
        ArrayList objects = getObjectData(rect, added, removed);
        for (int i = 0, size = objects.size(); i < size; i++) {
            ObjectData data = (ObjectData)objects.get(i);
            if (data.tile.hasConstraint(constraint)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the direction in which the specified object is constrained by
     * appending "_[NESW]" to the given constraint prefix.  Returns
     * <code>NONE</code> if there is no such directional constraint.
     */
    protected int getConstraintDirection (ObjectData data, String prefix)
    {
        if (data.tile.hasConstraint(prefix + "_N")) {
            return NORTH;
            
        } else if (data.tile.hasConstraint(prefix + "_E")) {
            return EAST;
            
        } else if (data.tile.hasConstraint(prefix + "_S")) {
            return SOUTH;
            
        } else if (data.tile.hasConstraint(prefix + "_W")) {
            return WEST;
            
        } else {
            return NONE;
        }
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
    
    /** The tile manager to use for object dimensions and constraints. */
    protected TileManager _tilemgr;
    
    /** The scene being checked for constraints. */
    protected StageScene _scene;
    
    /** The Miso scene model. */
    protected StageMisoSceneModel _mmodel;
    
    /** For all objects in the scene, maps {@link ObjectInfo}s to
     * {@link ObjectData}s. */
    protected HashMap _objectData = new HashMap();
}
