//
// $Id: SceneManager.java,v 1.7 2001/07/24 16:10:19 shaper Exp $

package com.threerings.miso.scene;

import com.samskivert.util.IntMap;

/**
 * The SceneManager provides a single access point for retrieving and
 * caching the various scenes that make up a game.
 */
public class SceneManager
{
    /**
     * Initialize the SceneManager with the given scene repository.
     *
     * @param repo the scene repository.
     */
    public SceneManager (SceneRepository repo)
    {
        _repo = repo;
    }

    /**
     * Return the Scene object for the specified scene id.
     *
     * @param sid the scene id.
     * @return the Scene object.
     */
    public Scene getScene (int sid)
    {
        // TBD
        return null;
    }

    public SceneRepository getSceneRepository ()
    {
        return _repo;
    }

    /**
     * Return a String array of all scene layer names ordered by
     * ascending layer id.
     *
     * @return the layer names.
     */
    public String[] getLayerNames ()
    {
	return Scene.XLATE_LAYERS;
    }

    /** The repository used to read and write scenes. */
    protected SceneRepository _repo;

    /** The currently cached scenes. */
    protected IntMap _scenes = new IntMap();
}
