//
// $Id: DisplayMisoSceneImpl.java,v 1.38 2001/10/08 21:04:25 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.samskivert.util.StringUtil;

import com.threerings.media.tile.TileManager;
import com.threerings.whirled.data.Scene;

import com.threerings.miso.Log;
import com.threerings.miso.scene.util.ClusterUtil;
import com.threerings.miso.tile.MisoTile;

/**
 * A scene object represents the data model corresponding to a single
 * screen for game play. For instance, one scene might display a portion
 * of a street with several buildings scattered about on the periphery.
 */
public class MisoSceneImpl implements EditableMisoScene
{
    /**
     * Construct a new miso scene object. The base layer tiles are
     * initialized to contain tiles of the specified default tileset and
     * tile id.
     *
     * @param model the iso scene view model.
     * @param tilemgr the tile manager.
     * @param deftsid the default tileset id.
     * @param deftid the default tile id.
     */
    public MisoSceneImpl (IsoSceneViewModel model, TileManager tilemgr,
                          int deftsid, int deftid)
    {
	_model = model;
	_tilemgr = tilemgr;

	_sid = SID_INVALID;
	_name = DEF_SCENE_NAME;

        _locations = new ArrayList();
	_clusters = new ArrayList();
        _portals = new ArrayList();

	_tiles = new MisoTile[_model.scenewid][_model.scenehei][NUM_LAYERS];
	_deftile = (MisoTile)_tilemgr.getTile(deftsid, deftid);
	for (int xx = 0; xx < _model.scenewid; xx++) {
	    for (int yy = 0; yy < _model.scenehei; yy++) {
		for (int ii = 0; ii < NUM_LAYERS; ii++) {
		    if (ii == LAYER_BASE) {
			_tiles[xx][yy][ii] = _deftile;
		    }
		}
	    }
	}
    }

    /**
     * Construct a new Miso scene object with the given values.
     *
     * @param model the iso scene view model.
     * @param tilemgr the tile manager.
     * @param name the scene name.
     * @param locations the locations.
     * @param portals the portals.
     * @param tiles the tiles comprising the scene.
     */
    public MisoSceneImpl (IsoSceneViewModel model, TileManager tilemgr,
                          String name, ArrayList locations,
                          ArrayList clusters, ArrayList portals,
                          MisoTile[][][] tiles)
    {
	_model = model;
	_tilemgr = tilemgr;
        _sid = SID_INVALID;
        _name = name;
        _locations = locations;
	_clusters = clusters;
        _portals = portals;
        _tiles = tiles;
    }

    // documentation inherited
    public String getName ()
    {
        return _name;
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
    public MisoTile[][][] getTiles ()
    {
        return _tiles;
    }

    // documentation inherited
    public MisoTile getDefaultTile ()
    {
	return _deftile;
    }

    // documentation inherited
    public List getLocations ()
    {
        return _locations;
    }

    // documentation inherited
    public List getClusters ()
    {
        return _clusters;
    }

    // documentation inherited
    public List getPortals ()
    {
        return _portals;
    }

    // documentation inherited
    public Portal getEntrance ()
    {
        return _entrance;
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
        _name = name;
    }

    // documentation inherited
    public void setEntrance (Portal entrance)
    {
	_entrance = entrance;
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
	if (!_locations.contains(loc)) {
	    _locations.add(loc);
	}

	// update the cluster contents
	ClusterUtil.regroup(_clusters, loc, clusteridx);
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
	if (_portals.contains(portal)) {
	    Log.warning("Attempt to add already-existing portal " +
			"[portal=" + portal + "].");
	    return;
	}

	// add it to the list
	_portals.add(portal);
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
	if (!_locations.remove(loc)) {
	    // we didn't know about it, so it can't be in a cluster or
	    // the portal list
	    return;
	}

	// remove from any possible cluster
	ClusterUtil.remove(_clusters, loc);

	// remove from any possible existence on the portal list
	_portals.remove(loc);
    }

    /**
     * Return a string representation of this Miso scene object.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[name=").append(_name);
        buf.append(", sid=").append(_sid);
        buf.append(", locations=").append(StringUtil.toString(_locations));
        buf.append(", clusters=").append(StringUtil.toString(_clusters));
        buf.append(", portals=").append(StringUtil.toString(_portals));
        return buf.append("]").toString();
    }

    /** The default scene name. */
    protected static final String DEF_SCENE_NAME = "Untitled Scene";

    /** The scene name. */
    protected String _name;

    /** The unique scene id. */
    protected int _sid;

    /** The scene version. */
    protected int _version;

    /** The tiles comprising the scene. */
    public MisoTile[][][] _tiles;

    /** The default entrance portal. */
    protected Portal _entrance;

    /** The locations within the scene. */
    protected ArrayList _locations;

    /** The clusters within the scene. */
    protected ArrayList _clusters;

    /** The portals to different scenes. */
    protected ArrayList _portals;

    /** The default tile for the base layer in the scene. */
    protected MisoTile _deftile;

    /** The iso scene view data model. */
    protected IsoSceneViewModel _model;

    /** The tile manager. */
    protected TileManager _tilemgr;
}
