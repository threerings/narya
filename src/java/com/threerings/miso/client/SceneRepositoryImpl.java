//
// $Id: SceneRepositoryImpl.java,v 1.2 2001/08/09 00:01:58 shaper Exp $

package com.threerings.miso.scene;

import com.samskivert.util.Config;
import com.threerings.miso.tile.TileManager;

public abstract class SceneRepositoryImpl implements SceneRepository
{
    /**
     * Initialize the SceneRepository with the given config and tile
     * manager objects.  The root scene directory is read from the
     * given config object, and the tile manager is used to obtain
     * tiles when constructing scene objects from files.
     *
     * @param config the config object.
     * @param tilemgr the tile manager object.
     */
    public void init (Config config, TileManager tilemgr)
    {
        _config = config;
        _tilemgr = tilemgr;
    }

    /** The config object. */
    protected Config _config;

    /** The tile manager from which the scenes obtain their tiles. */
    protected TileManager _tilemgr;
}
