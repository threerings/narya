//
// $Id: DisplayMisoSceneImpl.java,v 1.42 2001/10/25 16:36:42 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.samskivert.util.StringUtil;

import com.threerings.media.tile.*;
import com.threerings.whirled.data.Scene;

import com.threerings.miso.Log;
import com.threerings.miso.scene.util.ClusterUtil;
import com.threerings.miso.tile.MisoTile;
import com.threerings.miso.tile.ShadowTile;

/**
 * A scene object represents the data model corresponding to a single
 * screen for game play. For instance, one scene might display a portion
 * of a street with several buildings scattered about on the periphery.
 */
public class MisoSceneImpl implements EditableMisoScene
{
    /** The scene name. */
    public String name = DEF_SCENE_NAME;

    /** The locations within the scene. */
    public ArrayList locations = new ArrayList();

    /** The clusters within the scene. */
    public ArrayList clusters = new ArrayList();

    /** The portals to different scenes. */
    public ArrayList portals = new ArrayList();

    /** The default entrance portal. */
    public Portal entrance;

    /** The base tiles in the scene. */
    public MisoTile[][] baseTiles;

    /** The fringe tiles in the scene. */
    public Tile[][] fringeTiles;

    /** The object tiles in the scene. */
    public ObjectTile[][] objectTiles;

    /** All tiles in the scene. */
    public Tile[][][] tiles;

    /**
     * Construct a new miso scene object. The base layer tiles are
     * initialized to contain tiles of the specified default tileset and
     * tile id.
     *
     * <em>Note:</em> Be sure to call {@link
     * #generateAllObjectShadows} before the scene is first used so
     * that the base layer will be properly populated with shadow
     * tiles in the footprint of all object tiles.
     *
     * @param model the iso scene view model.
     * @param deftile the default tile.
     */
    public MisoSceneImpl (IsoSceneViewModel model, MisoTile deftile)
    {
	_model = model;
	_deftile = deftile;

        // create the individual tile layer arrays
	baseTiles = new MisoTile[_model.scenewid][_model.scenehei];
	fringeTiles = new Tile[_model.scenewid][_model.scenehei];
	objectTiles = new ObjectTile[_model.scenewid][_model.scenehei];

        // create the conjoined array for purely utilitarian purposes
	tiles = new Tile[][][] { baseTiles, fringeTiles, objectTiles };

        // initialize the always-fully-populated base layer
	initBaseTiles();
    }

    // documentation inherited
    public String getName ()
    {
        return name;
    }

    // documentation inherited
    public void setDefaultTile (MisoTile tile)
    {
        _deftile = tile;
    }

    // documentation inherited
    public void setTile (int lnum, int x, int y, Tile tile)
    {
        // if the tile being replaced is an object tile, clear out its
        // shadow tiles
        Tile otile = tiles[lnum][x][y];
        if (otile instanceof ObjectTile) {
            removeObjectShadow(x, y);
        }

        // place the new tile
        tiles[lnum][x][y] = tile;

        // if the tile being placed is an object tile, create the
        // shadow tiles that lie in its footprint
        if (tile instanceof ObjectTile) {
            generateObjectShadow(x, y);
        }
    }

    // documentation inherited
    public int getId ()
    {
        return _sid;
    }

    // documentation inherited
    public int getVersion ()
    {
        return _version;
    }

    // documentation inherited
    public int[] getNeighborIds ()
    {
        return null;
    }

    // documentation inherited
    public Tile[][][] getTiles ()
    {
	return tiles;
    }

    // documentation inherited
    public Tile[][] getTiles (int lnum)
    {
	return tiles[lnum];
    }

    // documentation inherited
    public MisoTile[][] getBaseLayer ()
    {
	return baseTiles;
    }

    // documentation inherited
    public Tile[][] getFringeLayer ()
    {
	return fringeTiles;
    }

    // documentation inherited
    public ObjectTile[][] getObjectLayer ()
    {
	return objectTiles;
    }

    // documentation inherited
    public MisoTile getDefaultTile ()
    {
	return _deftile;
    }

    // documentation inherited
    public List getLocations ()
    {
        return locations;
    }

    // documentation inherited
    public List getClusters ()
    {
        return clusters;
    }

    // documentation inherited
    public List getPortals ()
    {
        return portals;
    }

    // documentation inherited
    public Portal getEntrance ()
    {
        return entrance;
    }

    // documentation inherited
    public void setId (int sceneId)
    {
        _sid = sceneId;
    }

    // documentation inherited
    public void setVersion (int version)
    {
        _version = version;
    }

    // documentation inherited
    public void setName (String name)
    {
        this.name = name;
    }

    // documentation inherited
    public void setEntrance (Portal entrance)
    {
	this.entrance = entrance;
    }

    /**
     * Update the specified location in the scene.  If the cluster
     * index number is -1, the location will be removed from any
     * cluster it may reside in.
     *
     * @param loc the location.
     * @param clusteridx the cluster index number.
     */
    public void updateLocation (Location loc, int clusteridx)
    {
	// add the location if it's not already present
	if (!locations.contains(loc)) {
	    locations.add(loc);
	}

	// update the cluster contents
	ClusterUtil.regroup(clusters, loc, clusteridx);
    }

    /**
     * Add the specified portal to the scene.  Adds the portal to the
     * location list as well if it's not already present and removes
     * it from any cluster it may reside in.
     *
     * @param portal the portal.
     */
    public void addPortal (Portal portal)
    {
	// make sure it's in the location list and absent from any cluster
	updateLocation(portal, -1);

	// don't allow adding a portal more than once
	if (portals.contains(portal)) {
	    Log.warning("Attempt to add already-existing portal " +
			"[portal=" + portal + "].");
	    return;
	}

	// add it to the list
	portals.add(portal);
    }

    /**
     * Remove the given location object from the location list, and from
     * any containing cluster.  If the location is a portal, it is removed
     * from the portal list as well.
     *
     * @param loc the location object.
     */
    public void removeLocation (Location loc)
    {
	// remove from the location list
	if (!locations.remove(loc)) {
	    // we didn't know about it, so it can't be in a cluster or
	    // the portal list
	    return;
	}

	// remove from any possible cluster
	ClusterUtil.remove(clusters, loc);

	// remove from any possible existence on the portal list
	portals.remove(loc);
    }

    /**
     * Return a string representation of this Miso scene object.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[name=").append(name);
        buf.append(", sid=").append(_sid);
        buf.append(", locations=").append(StringUtil.toString(locations));
        buf.append(", clusters=").append(StringUtil.toString(clusters));
        buf.append(", portals=").append(StringUtil.toString(portals));
        return buf.append("]").toString();
    }

    /**
     * Initialize the base tile layer with the default tile.
     */
    protected void initBaseTiles ()
    {
	for (int xx = 0; xx < _model.scenewid; xx++) {
	    for (int yy = 0; yy < _model.scenehei; yy++) {
		baseTiles[xx][yy] = _deftile;
	    }
	}
    }

    /**
     * Place shadow tiles in the footprint of all object tiles in the
     * scene.  This method should be called once the scene tiles are
     * fully populated, but before the scene is used in any other
     * meaningful capacity.
     */
    public void generateAllObjectShadows ()
    {
        for (int xx = 0; xx < _model.scenewid; xx++) {
            for (int yy = 0; yy < _model.scenehei; yy++) {
                if (objectTiles[xx][yy] != null) {
                    generateObjectShadow(xx, yy);
                }
            }
        }
    }

    /**
     * Place shadow tiles in the footprint of the object tile at the
     * given coordinates in the scene.  This method should be called
     * when an object tile is added to the scene.
     *
     * @param x the tile x-coordinate.
     * @param y the tile y-coordinate.
     */
    protected void generateObjectShadow (int x, int y)
    {
        setObjectTileFootprint(x, y, new ShadowTile(x, y));
    }

    /**
     * Remove shadow tiles from the footprint of the object tile at
     * the given coordinates in the scene.  This method should be
     * called when an object tile is removed from the scene.
     *
     * @param x the tile x-coordinate.
     * @param y the tile y-coordinate.
     */
    protected void removeObjectShadow (int x, int y)
    {
        setObjectTileFootprint(x, y, _deftile);
    }

    /**
     * Place the given tile in the footprint of the object tile at the
     * given coordinates in the scene.
     *
     * @param x the tile x-coordinate.
     * @param y the tile y-coordinate.
     * @param stamp the tile to place in the object footprint.
     */
    protected void setObjectTileFootprint (int x, int y, MisoTile stamp)
    {
        ObjectTile tile = objectTiles[x][y];

        int endx = Math.max(0, (x - tile.baseWidth + 1));
        int endy = Math.max(0, (y - tile.baseHeight + 1));

        for (int xx = x; xx >= endx; xx--) {
            for (int yy = y; yy >= endy; yy--) {
                baseTiles[xx][yy] = stamp;
            }
        }
    }

    /** The default scene name. */
    protected static final String DEF_SCENE_NAME = "Untitled Scene";

    /** The unique scene id. */
    protected int _sid = SID_INVALID;

    /** The scene version. */
    protected int _version;

    /** The default tile for the base layer in the scene. */
    protected MisoTile _deftile;

    /** The iso scene view data model. */
    protected IsoSceneViewModel _model;
}
